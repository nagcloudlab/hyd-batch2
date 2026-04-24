package com.example.config;

import org.springframework.stereotype.Component;

@Component
public class BPP implements org.springframework.beans.factory.config.BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("BPP - Before Initialization: " + beanName);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("BPP - After Initialization: " + beanName);
        return bean;
    }

}
