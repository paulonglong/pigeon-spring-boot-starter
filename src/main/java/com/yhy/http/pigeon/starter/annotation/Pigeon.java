package com.yhy.http.pigeon.starter.annotation;

import com.yhy.http.pigeon.starter.internal.VoidSSLHostnameVerifier;
import com.yhy.http.pigeon.starter.internal.VoidSSLSocketFactory;
import com.yhy.http.pigeon.starter.internal.VoidSSLX509TrustManager;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Repository;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.annotation.*;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2020-08-27 16:53
 * version: 1.0.0
 * desc   : @Repository 用于将数据某些类标识为 Spring Bean
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Repository
public @interface Pigeon {

    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";

    String qualifier() default "";

    String baseURL() default "";

    PigeonHeader[] header() default {};

    PigeonInterceptor[] interceptor() default {};

    long timeout() default 6000L;

    boolean logging() default true;

    boolean primary() default true;

    Class<? extends SSLSocketFactory> sslSocketFactory() default VoidSSLSocketFactory.class;

    Class<? extends X509TrustManager> sslTrustManager() default VoidSSLX509TrustManager.class;

    Class<? extends HostnameVerifier> sslHostnameVerifier() default VoidSSLHostnameVerifier.class;
}
