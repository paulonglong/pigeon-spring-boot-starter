package com.yhy.http.pigeon.starter.register;

import com.yhy.http.pigeon.Pigeon;
import com.yhy.http.pigeon.starter.internal.VoidSSLHostnameVerifier;
import com.yhy.http.pigeon.starter.internal.VoidSSLSocketFactory;
import com.yhy.http.pigeon.starter.internal.VoidSSLX509TrustManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2020-08-27 18:11
 * version: 1.0.0
 * desc   :
 */
@Data
@Slf4j
public class PigeonFactoryBean implements FactoryBean<Object>, InitializingBean, ApplicationContextAware {
    private ApplicationContext applicationContext;

    private Class<?> pigeonInterface;
    private String baseURL;
    private Map<String, String> header;
    private List<Class<? extends Interceptor>> interceptors;
    private List<Class<? extends Interceptor>> netInterceptors;
    private long timeout;
    private boolean logging;
    private Class<? extends SSLSocketFactory> sslSocketFactory;
    private Class<? extends X509TrustManager> sslTrustManager;
    private Class<? extends HostnameVerifier> sslHostnameVerifier;

    @Override
    public Object getObject() throws Exception {
        return getTarget();
    }

    @Override
    public Class<?> getObjectType() {
        return pigeonInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasText(this.baseURL, "Pigeon [baseURL] must be set.");
        log.info("@Pigeon properties for [{}] set complete.", pigeonInterface);
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @SuppressWarnings("unchecked")
    <T> T getTarget() {
        Pigeon.Builder builder = new Pigeon.Builder().baseURL(baseURL).logging(logging);
        if (!CollectionUtils.isEmpty(header)) {
            header.forEach(builder::header);
        }
        if (!CollectionUtils.isEmpty(interceptors)) {
            interceptors.forEach(item -> builder.interceptor(getOrInstantiate(item)));
        }
        if (!CollectionUtils.isEmpty(netInterceptors)) {
            netInterceptors.forEach(item -> builder.interceptor(getOrInstantiate(item)));
        }
        if (timeout > 0) {
            builder.timeout(timeout);
        }
        if (sslSocketFactory != null && sslSocketFactory != VoidSSLSocketFactory.class && sslTrustManager != null && sslTrustManager != VoidSSLX509TrustManager.class && sslHostnameVerifier != null && sslHostnameVerifier != VoidSSLHostnameVerifier.class) {
            builder.https(getOrInstantiate(sslSocketFactory), getOrInstantiate(sslTrustManager), getOrInstantiate(sslHostnameVerifier));
        }
        return (T) builder.build().create(pigeonInterface);
    }

    private <B> B getOrInstantiate(Class<B> tClass) {
        try {
            return this.applicationContext.getBean(tClass);
        } catch (NoSuchBeanDefinitionException e) {
            return BeanUtils.instantiateClass(tClass);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PigeonFactoryBean that = (PigeonFactoryBean) o;
        return Objects.equals(applicationContext, that.applicationContext) &&
                Objects.equals(pigeonInterface, that.pigeonInterface) &&
                Objects.equals(baseURL, that.baseURL);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationContext, pigeonInterface, baseURL);
    }
}
