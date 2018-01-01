package com.keyboard3.mobclickinject;

/**
 * 注射代码接口
 *
 * @param <T>
 */
public interface AbstractInjector<T> {

    void inject(Finder finder, T target, Object source);
}
