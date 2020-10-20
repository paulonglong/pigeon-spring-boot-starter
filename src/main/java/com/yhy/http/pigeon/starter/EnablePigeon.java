package com.yhy.http.pigeon.starter;

import com.yhy.http.pigeon.starter.annotation.PigeonHeader;
import com.yhy.http.pigeon.starter.annotation.PigeonInterceptor;
import com.yhy.http.pigeon.starter.internal.VoidSSLHostnameVerifier;
import com.yhy.http.pigeon.starter.internal.VoidSSLSocketFactory;
import com.yhy.http.pigeon.starter.internal.VoidSSLX509TrustManager;
import com.yhy.http.pigeon.starter.register.PigeonAutoRegister;
import org.springframework.context.annotation.Import;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.annotation.*;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2020-08-27 16:12
 * version: 1.0.0
 * desc   : 启动 pigeon 自动装配
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({PigeonAutoRegister.class})
public @interface EnablePigeon {

    String[] value() default "";

    String[] basePackages() default "";

    Class<?>[] basePackageClasses() default {};

    String baseURL() default "";

    PigeonHeader[] header() default {};

    PigeonInterceptor[] interceptor() default {};

    long timeout() default 6000L;

    boolean logging() default true;

    Class<? extends SSLSocketFactory> sslSocketFactory() default VoidSSLSocketFactory.class;

    Class<? extends X509TrustManager> sslTrustManager() default VoidSSLX509TrustManager.class;

    Class<? extends HostnameVerifier> sslHostnameVerifier() default VoidSSLHostnameVerifier.class;
}
