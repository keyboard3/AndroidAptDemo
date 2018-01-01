package com.keyboard3.apt;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.TypeElement;

/**
 * Created by lizhaoxuan on 16/5/24.
 * 负责存储用于代码生成的信息
 */
public class ProxyInfo {
    private String packageName;
    private String targetClassName;
    private String proxyClassName;
    private TypeElement typeElement;

    private List<MobclickMethod> methods;

    public static final String PROXY = "PROXY";

    ProxyInfo(String packageName, String className) {
        this.packageName = packageName;
        this.targetClassName = className;
        this.proxyClassName = className + "$$" + PROXY;
    }

    String getProxyClassFullName() {
        return packageName + "." + proxyClassName;
    }

    String generateJavaCode() throws IllegalArgumentException {

        StringBuilder builder = new StringBuilder();
        builder.append("// Generated code from OnceClick. Do not modify!\n");
        builder.append("package ").append(packageName).append(";\n\n");

        builder.append("import android.view.View;\n");
        builder.append("import com.keyboard3.mobclickinject.Finder;\n");
        builder.append("import com.keyboard3.mobclickinject.AbstractInjector;\n");
        builder.append('\n');

        builder.append("public class ").append(proxyClassName);
        builder.append("<T extends ").append(getTargetClassName()).append(">");
        builder.append(" implements AbstractInjector<T>");
        builder.append(" {\n");

        generateInjectMethod(builder);
        builder.append('\n');

        builder.append("}\n");
        return builder.toString();

    }

    private String getTargetClassName() {
        return targetClassName.replace("$", ".");
    }

    private void generateInjectMethod(StringBuilder builder) throws IllegalArgumentException {

        builder.append("  @Override ")
                .append("public void inject(final Finder finder, final T target, Object source) {\n");

        for (MobclickMethod method : getMethods()) {
            builder.append("View    view = ")
                    .append("finder.findViewById(source, ")
                    .append(method.getId())
                    .append(");\n");
            builder.append("if(view != null){")
                    .append("view.setOnClickListener(new View.OnClickListener() {\n");
            builder.append("@Override\n")
                    .append("public void onClick(View v) {");
            if (method.getMethodParametersSize() == 1) {
                if (method.getMethodParameters().get(0).equals("android.view.View")) {
                    builder.append("System.out.println(\"会去调用mobclick(context,type) type:" + method.getType() + "\");");
                    builder.append("target.").append(method.getMethodName()).append("(v);");
                } else {
                    throw new IllegalArgumentException("Parameters must be android.view.View");
                }
            } else if (method.getMethodParametersSize() == 0) {
                builder.append("target.").append(method.getMethodName()).append("();");
            } else {
                throw new IllegalArgumentException("Does not support more than one parameter");
            }
            builder.append("\n}")
                    .append("        });\n}");
        }

        builder.append("  }\n");
    }

    TypeElement getTypeElement() {
        return typeElement;
    }

    void setTypeElement(TypeElement typeElement) {
        this.typeElement = typeElement;
    }

    List<MobclickMethod> getMethods() {
        return methods == null ? new ArrayList<MobclickMethod>() : methods;
    }

    void addMethod(MobclickMethod onceMethod) {
        if (methods == null) {
            methods = new ArrayList<>();
        }
        methods.add(onceMethod);
    }
}
