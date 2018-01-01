package com.keyboard3.apt;


import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class MobclickProcessor extends AbstractProcessor {

    private Messager messager;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //获取proxyMap
        Map<String, ProxyInfo> proxyMap = getProxyMap(roundEnv);

        //遍历proxyMap，并生成代码
        for (String key : proxyMap.keySet()) {
            ProxyInfo proxyInfo = proxyMap.get(key);
            writeCode(proxyInfo);
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(Mobclick.class.getCanonicalName());
        return types;
    }

    private Map<String, ProxyInfo> getProxyMap(RoundEnvironment roundEnv) {
        Map<String, ProxyInfo> proxyMap = new HashMap<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(Mobclick.class)) {
            //target相同只能强转。不同使用getEnclosingElement
            ExecutableElement executableElement = (ExecutableElement) element;
            TypeElement classElement = (TypeElement) element
                    .getEnclosingElement();

            PackageElement packageElement = elementUtils.getPackageOf(classElement);


            String fullClassName = classElement.getQualifiedName().toString();
            String className = classElement.getSimpleName().toString();
            String packageName = packageElement.getQualifiedName().toString();
            String methodName = executableElement.getSimpleName().toString();
            int viewId = executableElement.getAnnotation(Mobclick.class).value();
            String type = executableElement.getAnnotation(Mobclick.class).type();

            print("fullClassName: " + fullClassName
                    + ",  methodName: " + methodName
                    + ",  viewId: " + viewId
                    + ",  type: " + type
            );

            MobclickMethod onceMethod = new MobclickMethod(viewId, type, methodName, getMethodParameterTypes(executableElement));

            ProxyInfo proxyInfo = proxyMap.get(fullClassName);
            if (proxyInfo != null) {
                proxyInfo.addMethod(onceMethod);
            } else {
                proxyInfo = new ProxyInfo(packageName, className);
                proxyInfo.setTypeElement(classElement);
                proxyInfo.addMethod(onceMethod);
                proxyMap.put(fullClassName, proxyInfo);
            }
        }
        return proxyMap;
    }

    /**
     * 取得方法参数类型列表
     */
    private List<String> getMethodParameterTypes(ExecutableElement executableElement) {
        List<? extends VariableElement> methodParameters = executableElement.getParameters();
        if (methodParameters.size() == 0) {
            return null;
        }
        List<String> types = new ArrayList<>();
        for (VariableElement variableElement : methodParameters) {
            TypeMirror methodParameterType = variableElement.asType();
            if (methodParameterType instanceof TypeVariable) {
                TypeVariable typeVariable = (TypeVariable) methodParameterType;
                methodParameterType = typeVariable.getUpperBound();
            }
            types.add(methodParameterType.toString());
        }
        return types;
    }

    private void writeCode(ProxyInfo proxyInfo) {
        try {
            JavaFileObject jfo = processingEnv.getFiler().createSourceFile(
                    proxyInfo.getProxyClassFullName(),
                    proxyInfo.getTypeElement());
            Writer writer = jfo.openWriter();
            writer.write(proxyInfo.generateJavaCode());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            error(proxyInfo.getTypeElement(),
                    "Unable to write injector for type %s: %s",
                    proxyInfo.getTypeElement(), e.getMessage());
        } catch (IllegalArgumentException e) {
            error(proxyInfo.getTypeElement(),
                    "The use of irregular %s: %s",
                    proxyInfo.getTypeElement(), e.getMessage());
        }
    }

    private void print(String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, message);
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

}
