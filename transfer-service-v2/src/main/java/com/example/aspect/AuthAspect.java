package com.example.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(1) // Set order to ensure this aspect runs before TransactionAspect
@Component
@Aspect
public class AuthAspect {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger("txr-service");

    @Before("execution(void com.example.service.TransferServiceImpl.transfer(..))")
    public void checkAuthentication() {
        // Add authentication logic here
        logger.info(">>> Authentication check passed.");
    }

}
