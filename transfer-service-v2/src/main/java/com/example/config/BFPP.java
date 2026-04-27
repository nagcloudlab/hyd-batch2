package com.example.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

// BeanFactoryPostProcessor — runs BEFORE any bean is created
// Use case: modify bean definitions (metadata) before instantiation
// Example: PropertySourcesPlaceholderConfigurer (resolves @Value placeholders) is a BFPP
@Component
public class BFPP implements BeanFactoryPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(BFPP.class);

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        logger.info("BeanFactoryPostProcessor — bean definitions loaded, beans not yet created.");
        logger.info("Total bean definitions: {}", beanFactory.getBeanDefinitionCount());
        logger.info("-".repeat(10));
    }

}
