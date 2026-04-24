package com.example.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(2) // Set order to ensure this aspect runs after AuthAspect
@Component
@Aspect
public class TransactionAspect {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger("txr-service");

    // @Before("execution(void
    // com.example.service.TransferServiceImpl.transfer(..))")
    // public void beginTransaction() {
    // logger.info(">>> Transaction started.");
    // }

    // @AfterReturning("execution(void
    // com.example.service.TransferServiceImpl.transfer(..))")
    // public void commitTransaction() {
    // logger.info(">>> Transaction committed.");
    // }

    // @AfterThrowing("execution(void
    // com.example.service.TransferServiceImpl.transfer(..))")
    // public void rollbackTransaction() {
    // logger.info(">>> Transaction rolled back due to an error.");
    // }

    // @After("execution(void
    // com.example.service.TransferServiceImpl.transfer(..))")
    // public void endTransaction() {
    // logger.info(">>> Transaction ended.");
    // }

    @Around("execution(void com.example.service.TransferServiceImpl.transfer(..))")
    public void manageTransaction(ProceedingJoinPoint pjp) throws Throwable {
        logger.info(">>> Transaction started.");
        try {
            pjp.proceed(); // Proceed with the original method execution
            logger.info(">>> Transaction committed.");
        } catch (Throwable ex) {
            logger.info(">>> Transaction rolled back due to an error: {}", ex.getMessage());
            throw ex; // Rethrow the exception to propagate it up the call stack
        } finally {
            logger.info(">>> Transaction ended.");
        }
    }

}
