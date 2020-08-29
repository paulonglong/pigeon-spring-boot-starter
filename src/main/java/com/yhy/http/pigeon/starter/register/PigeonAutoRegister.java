package com.yhy.http.pigeon.starter.register;

import com.yhy.http.pigeon.starter.EnablePigeon;
import com.yhy.http.pigeon.starter.annotation.Pigeon;
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
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @Override
    public void setResourceLoader(@NotNull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
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
//        Map<String, Object> attributes = metadata.getAnnotationAttributes(EnablePigeon.class.getName(), true);
//        log.info("===={}", attributes);
        log.info("搞点默认配置");
    }

    private void registerPigeons(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Pigeon.class));

        Set<String> basePackages = getBasePackages(metadata);
        Set<BeanDefinition> candidates;
        for (String pkg : basePackages) {
            candidates = scanner.findCandidateComponents(pkg);
            for (BeanDefinition candidate : candidates) {
                AnnotatedBeanDefinition definition = (AnnotatedBeanDefinition) candidate;
                AnnotationMetadata meta = definition.getMetadata();
                Assert.isTrue(meta.isInterface(), "@Pigeon can only be specified on an interface.");
                Map<String, Object> attrs = meta.getAnnotationAttributes(Pigeon.class.getCanonicalName());
                log.info("Scanning @Pigeon candidate [{}], attrs = {}", candidate.getBeanClassName(), attrs);

                registerPigeon(registry, meta, attrs);
            }
        }
    }

    private void registerPigeon(BeanDefinitionRegistry registry, AnnotationMetadata meta, Map<String, Object> attrs) {
        BeanDefinitionBuilder definition = BeanDefinitionBuilder.genericBeanDefinition(PigeonFactoryBean.class);
        String className = meta.getClassName();
        String name = getName(attrs);
        String qualifier = getQualifier(attrs);
        AnnotationAttributes[] interceptors = (AnnotationAttributes[]) attrs.get("interceptor");

        definition.addPropertyValue("pigeonInterface", className);
        definition.addPropertyValue("baseURL", getBaseURL(attrs));
        definition.addPropertyValue("header", getHeader(attrs));
        definition.addPropertyValue("interceptors", getInterceptors(interceptors));
        definition.addPropertyValue("netInterceptors", getNetInterceptors(interceptors));
        definition.addPropertyValue("timeout", getTimeout(attrs));
        definition.addPropertyValue("logging", getLogging(attrs));
        definition.setLazyInit(true);

        // 按类型注入
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

        String alias = StringUtils.hasText(qualifier) ? qualifier : StringUtils.hasText(name) ? name : "pigeonInterface";
        AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();
        beanDefinition.setPrimary(true);

        // 注入 bean
        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, className, new String[]{alias});
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }

    private String getBaseURL(Map<String, Object> attrs) {
        return resolve((String) attrs.get("baseURL"));
    }

    private String getName(Map<String, Object> attrs) {
        return resolve((String) attrs.get("name"));
    }

    private String getQualifier(Map<String, Object> client) {
        if (client == null) {
            return null;
        }
        String qualifier = (String) client.get("qualifier");
        if (StringUtils.hasText(qualifier)) {
            return qualifier;
        }
        return null;
    }

    private Map<String, Object> getHeader(Map<String, Object> attrs) {
        AnnotationAttributes[] headers = (AnnotationAttributes[]) attrs.get("header");
        if (null != headers && headers.length > 0) {
            return Stream.of(headers).collect(Collectors.toMap(key -> key.getString("name"), value -> value.getString("value")));
        }
        return null;
    }

    private List<Class<? extends Interceptor>> getInterceptors(AnnotationAttributes[] interceptors) {
        if (null != interceptors && interceptors.length > 0) {
            return Stream.of(interceptors).filter(it -> !it.getBoolean("net")).map(it -> it.<Interceptor>getClass("clazz")).collect(Collectors.toList());
        }
        return null;
    }

    private List<Class<? extends Interceptor>> getNetInterceptors(AnnotationAttributes[] interceptors) {
        if (null != interceptors && interceptors.length > 0) {
            return Stream.of(interceptors).filter(it -> it.getBoolean("net")).map(it -> it.<Interceptor>getClass("clazz")).collect(Collectors.toList());
        }
        return null;
    }

    private long getTimeout(Map<String, Object> attrs) {
        return (Long) attrs.get("timeout");
    }

    private boolean getLogging(Map<String, Object> attrs) {
        return (Boolean) attrs.get("logging");
    }

    private String resolve(String value) {
        if (StringUtils.hasText(value)) {
            return this.environment.resolvePlaceholders(value);
        }
        return value;
    }

    @SuppressWarnings("rawtypes")
    protected Set<String> getBasePackages(AnnotationMetadata metadata) {
        Set<String> basePackages = new HashSet<>();
        Map<String, Object> attributes = metadata.getAnnotationAttributes(EnablePigeon.class.getCanonicalName());
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
            value = attributes.get("basePackageClasses");
            if (null != value) {
                for (Class<?> clazz : (Class[]) attributes.get("basePackageClasses")) {
                    basePackages.add(ClassUtils.getPackageName(clazz));
                }
            }

            if (basePackages.isEmpty()) {
                basePackages.add(ClassUtils.getPackageName(metadata.getClassName()));
            }
        }
        return basePackages;
    }

    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(@NotNull AnnotatedBeanDefinition beanDefinition) {
                return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent() && !beanDefinition.getMetadata().isAnnotation();
            }
        };
    }
}
