# AndroidAptDemo
Android 注解处理器 构建demo<br>
本 demo 实现类似 ButterKnife 的 click 注入功能并在其中插入了友盟事件上传

# 原理
- 定义需要自动生成的外部类

这个类为 view 设置点击事件，调用 Activity 中指定得注解方法<br>
[]中的参数表示需要注解处理器传参，找 view 的viewId 以及友盟 mobclick 方法需要的 type 字符串

```java
public class MainActivity$$PROXY<T extends MainActivity> implements AbstractInjector<T> {
    public MainActivity$$PROXY() {
    }

    public void inject(Finder finder, final T target, Object source) {
        View view = finder.findViewById(source, [2131165219]);
        if(view != null) {
            view.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    System.out.println("会去调用mobclick(context,type) type:[click_baidu]");
                    target.mobclick(v);
                }
            });
        }

    }
}
```
- 注解处理器遍历抽象语法树获取注解方法的信息，拼接类成字符串转化为 JavaFileObject ，然后将类对象写成文件
```java
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
....
    }
//....
 JavaFileObject jfo = processingEnv.getFiler().createSourceFile(
                     proxyInfo.getProxyClassFullName(),
                     proxyInfo.getTypeElement());
Writer writer = jfo.openWriter();
writer.write(proxyInfo.generateJavaCode());
```
- PROXY 类的内部实现 AbstractInjector

以便在 mobclickinject 可以直接调用动态生成类的实现方法<br>
Finder只是一个抽出来的简单实现 getView 的
```java
public interface AbstractInjector<T> {

    void inject(Finder finder, T target, Object source);
}
public class MobclickInit {
    private static final Map<Class<?>, AbstractInjector<Object>> INJECTORS = new LinkedHashMap<Class<?>, AbstractInjector<Object>>();

    public static void inject(Activity activity) {
        AbstractInjector<Object> injector = findInjector(activity);
        injector.inject(Finder.ACTIVITY, activity, activity);
    }

    public static void inject(View view) {
        AbstractInjector<Object> injector = findInjector(view);
        injector.inject(Finder.VIEW, view, view);
    }


    private static AbstractInjector<Object> findInjector(Object activity) {
        Class<?> clazz = activity.getClass();
        AbstractInjector<Object> injector = INJECTORS.get(clazz);
        if (injector == null) {
            try {
                Class injectorClazz = Class.forName(clazz.getName() + "$$"
                        + "PROXY");
                injector = (AbstractInjector<Object>) injectorClazz
                        .newInstance();
                INJECTORS.put(clazz, injector);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return injector;
    }
}
```
- 运行时调用 mobclickinject 的注入方法，对生成类文件进行反射调用代理 view 点击方法
```java
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobclickInit.inject(this);
    }

    @Mobclick(value = R.id.btn_ok, type = "click_baidu")
    public void mobclick(View view) {
        Toast.makeText(this, "查看日志输出", Toast.LENGTH_SHORT).show();
    }
}

```