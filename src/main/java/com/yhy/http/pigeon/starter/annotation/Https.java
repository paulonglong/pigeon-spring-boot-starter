package com.yhy.http.pigeon.starter.annotation;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.annotation.*;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2020-08-27 17:57
 * version: 1.0.0
 * desc   :
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Https {

    Class<? extends SSLSocketFactory> sslSocketFactory();

    Class<? extends X509TrustManager> sslTrustManager();

    Class<? extends HostnameVerifier> sslHostnameVerifier();
}
