package com.yhy.http.pigeon.starter.annotation;

import java.lang.annotation.*;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2020-08-27 17:53
 * version: 1.0.0
 * desc   :
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Interceptor {

    Class<? extends okhttp3.Interceptor> clazz();

    boolean net() default false;
}