package com.yhy.http.pigeon.starter.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2020-08-27 16:53
 * version: 1.0.0
 * desc   :
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Pigeon {

    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";

    String qualifier() default "";

    String baseURL() default "";

    Header[] header() default {};

    Interceptor[] interceptor() default {};

    long timeout() default 6000L;

    boolean logging() default true;
}
