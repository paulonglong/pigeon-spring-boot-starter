package com.yhy.http.pigeon.starter.register;

import com.yhy.http.pigeon.starter.EnablePigeonClient;
import com.yhy.http.pigeon.starter.annotation.PigeonClient;
import com.yhy.http.pigeon.starter.internal.VoidSSLHostnameVerifier;
import com.yhy.http.pigeon.starter.internal.VoidSSLSocketFactory;
import com.yhy.http.pigeon.starter.internal.VoidSSLX509TrustManager;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2020-08-27 16:16
 * version: 1.0.0
 * desc   : pigeon 自动扫描注册器
 */
@Slf4j
public class PigeonAutoRegister implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {
    private ResourceLoader resourceLoader;
    private Environment environment;

    private String baseURL;
    private Map<String, String> header = new HashMap<>();
    private List<Class<? extends Interceptor>> interceptorList = new ArrayList<>();
    private List<Class<? extends Interceptor>> netInterceptorList = new ArrayList<>();
    private long timeout;
    private Boolean logging;
    private Class<? extends SSLSocketFactory> sslSocketFactory;
    private Class<? extends X509TrustManager> sslTrustManager;
    private Class<? extends HostnameVerifier> sslHostnameVerifier;

    @Override
    public void setResourceLoader(@NotNull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void setEnvironment(@NotNull Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(@NotNull AnnotationMetadata metadata, @NotNull BeanDefinitionRegistry registry) {
        // 注册默认配置
        registerDefaultConfiguration(metadata, registry);
        // 注册 pigeon
        registerPigeons(metadata, registry);
    }

    private void registerDefaultConfiguration(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(EnablePigeonClient.class.getCanonicalName());
        log.info("Loading global configuration for @Pigeon from @EnablePigeon: {}", attributes);
        if (!CollectionUtils.isEmpty(attributes)) {
            attributes.forEach((name, value) -> {
                log.debug("Loaded global configuration for @Pigeon from @EnablePigeon {} = {}", name, value);
            });
            // 加载...
            AnnotationAttributes[] annotationAttributes = (AnnotationAttributes[]) attributes.get("interceptor");
            baseURL = getBaseURL(attributes);
            header = getHeader(attributes);
            interceptorList = getInterceptors(annotationAttributes, false);
            netInterceptorList = getInterceptors(annotationAttributes, true);
            timeout = getTimeout(attributes);
            logging = getLogging(attributes);
            sslSocketFactory = getSSLSocketFactory(attributes);
            sslTrustManager = getSSLTrustManager(attributes);
            sslHostnameVerifier = getSSLHostnameVerifier(attributes);
        }
        log.info("The global configuration for @Pigeon from @EnablePigeon loaded.");
    }

    private void registerPigeons(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        PigeonAutoScanner scanner = new PigeonAutoScanner(registry);
        scanner.setResourceLoader(resourceLoader);
        scanner.registerFilters();

        Set<String> basePackages = getBasePackages(metadata);
        for (String pkg : basePackages) {
            Set<BeanDefinition> candidates = scanner.findCandidateComponents(pkg);
            for (BeanDefinition candidate : candidates) {
                if (candidate instanceof AnnotatedBeanDefinition) {
                    AnnotatedBeanDefinition definition = (AnnotatedBeanDefinition) candidate;
                    AnnotationMetadata meta = definition.getMetadata();
                    Assert.isTrue(meta.isInterface(), "@Pigeon can only be specified on an interface.");
                    Map<String, Object> attrs = meta.getAnnotationAttributes(PigeonClient.class.getCanonicalName());
                    log.info("Scanning @Pigeon candidate [{}], attrs = {}", candidate.getBeanClassName(), attrs);

                    registerPigeon(registry, meta, attrs);
                }
            }
        }
    }

    private void registerPigeon(BeanDefinitionRegistry registry, AnnotationMetadata meta, Map<String, Object> attrs) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(PigeonFactoryBean.class);
        String className = meta.getClassName();
        String name = getName(attrs);
        String qualifier = getQualifier(attrs);
        AnnotationAttributes[] interceptors = (AnnotationAttributes[]) attrs.get("interceptor");

        builder.addPropertyValue("pigeonInterface", className);
        builder.addPropertyValue("baseURL", getBaseURL(attrs));
        builder.addPropertyValue("header", getHeader(attrs));
        builder.addPropertyValue("interceptors", getInterceptors(interceptors, false));
        builder.addPropertyValue("netInterceptors", getInterceptors(interceptors, true));
        builder.addPropertyValue("timeout", getTimeout(attrs));
        builder.addPropertyValue("logging", getLogging(attrs));
        builder.addPropertyValue("sslSocketFactory", getSSLSocketFactory(attrs));
        builder.addPropertyValue("sslTrustManager", getSSLTrustManager(attrs));
        builder.addPropertyValue("sslHostnameVerifier", getSSLHostnameVerifier(attrs));
        builder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        String alias = StringUtils.hasText(qualifier) ? qualifier : StringUtils.hasText(name) ? name : className + "PigeonClient";
        beanDefinition.setPrimary(true);

        //        // 注入 bean
        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, className, new String[]{alias});
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }

    private String getBaseURL(Map<String, Object> attrs) {
        String url = resolve((String) attrs.get("baseURL"));
        if (!StringUtils.isEmpty(url)) {
            return url;
        }
        return baseURL;
    }

    private String getName(Map<String, Object> attrs) {
        String name = resolve((String) attrs.get("name"));
        if (StringUtils.isEmpty(name)) {
            name = resolve((String) attrs.get("value"));
        }
        return name;
    }

    private String getQualifier(Map<String, Object> attrs) {
        return (String) attrs.get("qualifier");
    }

    private Map<String, String> getHeader(Map<String, Object> attrs) {
        AnnotationAttributes[] headers = (AnnotationAttributes[]) attrs.get("header");
        if (null != headers && headers.length > 0) {
            // key 重复时以后者为准
            Map<String, String> temp = Stream.of(headers).collect(Collectors.toMap(attr -> attr.getString("name"), attr -> attr.getString("value"), (k1, k2) -> k2));
            if (CollectionUtils.isEmpty(temp)) {
                temp = header;
            } else if (!CollectionUtils.isEmpty(header)) {
                header.forEach(temp::put);
            }
            return temp;
        }
        return header;
    }

    private List<Class<? extends Interceptor>> getInterceptors(AnnotationAttributes[] interceptors, boolean net) {
        if (null != interceptors && interceptors.length > 0) {
            List<Class<? extends Interceptor>> temp = Stream.of(interceptors).filter(it -> net && it.getBoolean("net") || !net && !it.getBoolean("net")).map(it -> it.<Interceptor>getClass("value")).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(temp)) {
                temp = net ? netInterceptorList : interceptorList;
            } else if (!CollectionUtils.isEmpty(interceptorList)) {
                // 全局拦截器优先加载
                List<Class<? extends Interceptor>> buf = new ArrayList<>(net ? netInterceptorList : interceptorList);
                buf.addAll(temp);
                temp = buf;
            }
            return temp;
        }
        return net ? netInterceptorList : interceptorList;
    }

    private long getTimeout(Map<String, Object> attrs) {
        // 优先使用 @Pigeon 作用域
        long temp = (Long) attrs.get("timeout");
        return temp > 0 ? temp : timeout;
    }

    private Boolean getLogging(Map<String, Object> attrs) {
        // logging == null 时为初始状态，第一次回去全局配置
        return (null == logging || logging) && (Boolean) attrs.get("logging");
    }

    @SuppressWarnings("unchecked")
    private Class<? extends SSLSocketFactory> getSSLSocketFactory(Map<String, Object> attrs) {
        // 优先使用 @Pigeon 作用域
        Class<? extends SSLSocketFactory> temp = (Class<? extends SSLSocketFactory>) attrs.get("sslSocketFactory");
        return null != temp && temp != VoidSSLSocketFactory.class ? temp : sslSocketFactory;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends X509TrustManager> getSSLTrustManager(Map<String, Object> attrs) {
        // 优先使用 @Pigeon 作用域
        Class<? extends X509TrustManager> temp = (Class<? extends X509TrustManager>) attrs.get("sslTrustManager");
        return null != temp && temp != VoidSSLX509TrustManager.class ? temp : sslTrustManager;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends HostnameVerifier> getSSLHostnameVerifier(Map<String, Object> attrs) {
        // 优先使用 @Pigeon 作用域
        Class<? extends HostnameVerifier> temp = (Class<? extends HostnameVerifier>) attrs.get("sslHostnameVerifier");
        return null != temp && temp != VoidSSLHostnameVerifier.class ? temp : sslHostnameVerifier;
    }

    private String resolve(String value) {
        if (StringUtils.hasText(value)) {
            return this.environment.resolvePlaceholders(value);
        }
        return value;
    }

    protected Set<String> getBasePackages(AnnotationMetadata metadata) {
        Set<String> basePackages = new HashSet<>();
        Map<String, Object> attributes = metadata.getAnnotationAttributes(EnablePigeonClient.class.getCanonicalName());
        if (null != attributes) {
            Object value = attributes.get("value");
            if (null != value) {
                for (String pkg : (String[]) value) {
                    if (StringUtils.hasText(pkg)) {
                        basePackages.add(pkg);
                    }
                }
            }
            value = attributes.get("basePackages");
            if (null != value) {
                for (String pkg : (String[]) attributes.get("basePackages")) {
                    if (StringUtils.hasText(pkg)) {
                        basePackages.add(pkg);
                    }
                }
            }
            for (Class<?> clazz : (Class[]) attributes.get("basePackageClasses")) {
                basePackages.add(ClassUtils.getPackageName(clazz));
            }
            if (basePackages.isEmpty()) {
                basePackages.add(ClassUtils.getPackageName(metadata.getClassName()));
            }
        }
        return basePackages;
    }
}
