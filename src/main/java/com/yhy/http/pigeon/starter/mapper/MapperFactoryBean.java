package com.yhy.http.pigeon.starter.mapper;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2020-08-30 17:58
 * version: 1.0.0
 * desc   :
 */
@Slf4j
public class MapperFactoryBean<T> implements FactoryBean<T>, InitializingBean, ApplicationContextAware {
    private ApplicationContext applicationContext;
    private Class<T> type;

    public MapperFactoryBean() {
    }

    public MapperFactoryBean(Class<T> type) {
        this.type = type;
    }

    @Override
    public T getObject() throws Exception {
        log.info("实例化 {}", type);
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                log.info("Mapper方法执行了");
//                return method.invoke(args);
                return "啊哈哈哈";
            }
        });
    }

    @Override
    public Class<T> getObjectType() {
        return type;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("设置完成");
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
