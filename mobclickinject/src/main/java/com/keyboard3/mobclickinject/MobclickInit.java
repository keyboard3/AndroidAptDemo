package com.keyboard3.mobclickinject;


import android.app.Activity;
import android.view.View;


import java.util.LinkedHashMap;
import java.util.Map;

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
