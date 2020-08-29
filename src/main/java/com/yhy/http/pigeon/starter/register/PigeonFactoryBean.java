package com.yhy.http.pigeon.starter.register;

import com.yhy.http.pigeon.Pigeon;
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
//@EqualsAndHashCode
public class PigeonFactoryBean implements FactoryBean<Object>, InitializingBean, ApplicationContextAware {
    private ApplicationContext applicationContext;

    private Class<?> pigeonInterface;
    private String baseURL;
    private Map<String, Object> header;
    private List<Class<? extends Interceptor>> interceptors;
    private List<Class<? extends Interceptor>> netInterceptors;
    private long timeout;
    private boolean logging;

    public PigeonFactoryBean() {
    }

    public PigeonFactoryBean(Class<?> pigeonInterface) {
        this.pigeonInterface = pigeonInterface;
    }

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
        Assert.hasText(this.baseURL, "Pigeon [baseURL] id must be set");
        log.debug("@Pigeon properties set complete.");
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        }
//        if (o == null || getClass() != o.getClass()) {
//            return false;
//        }
//        PigeonFactoryBean that = (PigeonFactoryBean) o;
//        return Objects.equals(this.applicationContext, that.applicationContext)
//                && Objects.equals(this.baseURL, that.baseURL)
//                && Objects.equals(this.pigeonInterface, that.pigeonInterface);
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.applicationContext, this.baseURL, this.pigeonInterface);
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
        return (T) builder.build().create(pigeonInterface);
    }

    private <B> B getOrInstantiate(Class<B> tClass) {
        try {
            return this.applicationContext.getBean(tClass);
        } catch (NoSuchBeanDefinitionException e) {
            return BeanUtils.instantiateClass(tClass);
        }
    }
}
