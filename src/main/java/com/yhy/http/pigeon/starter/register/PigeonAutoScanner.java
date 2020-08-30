package com.yhy.http.pigeon.starter.register;

import com.yhy.http.pigeon.starter.annotation.PigeonClient;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2020-08-30 00:19
 * version: 1.0.0
 * desc   : 扫描器
 */
@Slf4j
public class PigeonAutoScanner extends ClassPathBeanDefinitionScanner {

    public PigeonAutoScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
    }

    public void registerFilters() {
        addIncludeFilter(new AnnotationTypeFilter(PigeonClient.class));
    }

    @Override
    protected boolean isCandidateComponent(@NotNull AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent() && !beanDefinition.getMetadata().isAnnotation();
    }
}
