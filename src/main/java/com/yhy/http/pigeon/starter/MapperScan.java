package com.yhy.http.pigeon.starter;

import com.yhy.http.pigeon.starter.annotation.HeaderClient;
import com.yhy.http.pigeon.starter.annotation.InterceptorClient;
import com.yhy.http.pigeon.starter.internal.VoidSSLHostnameVerifier;
import com.yhy.http.pigeon.starter.internal.VoidSSLSocketFactory;
import com.yhy.http.pigeon.starter.internal.VoidSSLX509TrustManager;
import com.yhy.http.pigeon.starter.mapper.MapperScannerRegister;
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
@Import({MapperScannerRegister.class})
public @interface MapperScan {

    String[] value() default "";

    String[] basePackages() default "";

    Class<?>[] basePackageClasses() default {};

    String baseURL() default "";

    HeaderClient[] header() default {};

    InterceptorClient[] interceptor() default {};

    long timeout() default 6000L;

    boolean logging() default true;

    Class<? extends SSLSocketFactory> sslSocketFactory() default VoidSSLSocketFactory.class;

    Class<? extends X509TrustManager> sslTrustManager() default VoidSSLX509TrustManager.class;

    Class<? extends HostnameVerifier> sslHostnameVerifier() default VoidSSLHostnameVerifier.class;
}
