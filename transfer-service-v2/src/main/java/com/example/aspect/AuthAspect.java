package com.example.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// @Aspect — marks this class as an AOP aspect (cross-cutting concern)
// @Order(1) — runs before TransactionAspect (@Order(2)) when multiple aspects match
// @Component — registers this aspect as a Spring bean
@Order(1)
@Component
@Aspect
public class AuthAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuthAspect.class);

    // @Before — advice runs before the matched method execution
    // Pointcut expression: matches transfer(..) method in TransferServiceImpl
    @Before("execution(void com.example.service.TransferServiceImpl.transfer(..))")
    public void checkAuthentication() {
        logger.info(">>> Authentication check.");
    }

}
