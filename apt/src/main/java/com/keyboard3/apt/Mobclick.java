package com.keyboard3.apt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author keyboard3 on 2018/1/1
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Mobclick {
    int value();

    String type() default "";
}
