package com.yhy.http.pigeon.starter.register;

import com.yhy.http.pigeon.starter.annotation.Pigeon;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Set;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2020-08-30 00:19
 * version: 1.0.0
 * desc   : 扫描器
 */
public class PigeonAutoScanner extends ClassPathBeanDefinitionScanner {

    public PigeonAutoScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
    }

    public void registerFilters() {
        addIncludeFilter(new AnnotationTypeFilter(Pigeon.class));
    }

    @Override
    protected boolean isCandidateComponent(@NotNull AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent() && !beanDefinition.getMetadata().isAnnotation();
    }

    @Override
    protected @NotNull Set<BeanDefinitionHolder> doScan(String @NotNull ... basePackages) {
        return super.doScan(basePackages);
    }
}
