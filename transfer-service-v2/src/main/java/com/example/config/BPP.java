package com.example.config;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import com.example.annotation.NpciAnnotation;

// BeanPostProcessor — runs AFTER each bean is created but BEFORE/AFTER initialization (@PostConstruct)
// Use case: inspect or wrap beans (e.g., AOP proxies are created by a BPP internally)
// Lifecycle: Constructor -> BPP.before -> @PostConstruct -> BPP.after
// @Component
public class BPP implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(BPP.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        logger.info("BPP — Before Initialization: {}", beanName);
        // Scan for custom @NpciAnnotation on bean methods
        for (Method method : bean.getClass().getMethods()) {
            if (method.isAnnotationPresent(NpciAnnotation.class)) {
                logger.info("Found @NpciAnnotation on method: {} of bean: {}", method.getName(), beanName);
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        logger.info("BPP — After Initialization: {}", beanName);
        logger.info("-".repeat(10));
        return bean;
    }

}
