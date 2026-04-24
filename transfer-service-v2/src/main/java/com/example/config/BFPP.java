package com.example.config;

import org.slf4j.Logger;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class BFPP implements BeanFactoryPostProcessor {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger("txr-service");

    @Override
    public void postProcessBeanFactory(
            org.springframework.beans.factory.config.ConfigurableListableBeanFactory beanFactory)
            throws org.springframework.beans.BeansException {
        logger.info("BFPP");
    }

}
