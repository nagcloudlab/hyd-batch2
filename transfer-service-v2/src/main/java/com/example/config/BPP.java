package com.example.config;

import java.lang.reflect.Method;

import org.springframework.stereotype.Component;

@Component
public class BPP implements org.springframework.beans.factory.config.BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("BPP - Before Initialization: " + beanName);

        // ---------------------------------------------------

        Class<?> clazz = bean.getClass();
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(com.example.annotation.NpciAnnotation.class)) {
                System.out.println("Found @NpciAnnotation on method: " + method.getName() + " of bean: " + beanName);
            }
        }

        // ---------------------------------------------------

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("BPP - After Initialization: " + beanName);
        return bean;
    }

}
