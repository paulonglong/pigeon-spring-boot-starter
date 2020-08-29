package com.yhy.http.pigeon.starter.internal;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2020-08-29 19:14
 * version: 1.0.0
 * desc   :
 */
public class VoidSSLHostnameVerifier implements HostnameVerifier {
    @Override
    public boolean verify(String hostname, SSLSession session) {
        return false;
    }
}
