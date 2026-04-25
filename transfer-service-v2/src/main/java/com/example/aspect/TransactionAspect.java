package com.example.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// @Around advice — most powerful advice type, controls whether the target method executes
// @Order(2) — runs after AuthAspect (@Order(1))
// This simulates what @Transactional does internally in Spring
@Order(2)
@Component
@Aspect
public class TransactionAspect {

    private static final Logger logger = LoggerFactory.getLogger(TransactionAspect.class);

    // Individual advice types for reference:
    // @Before — runs before method
    // @AfterReturning — runs after successful return
    // @AfterThrowing — runs after exception
    // @After — runs always (like finally)
    // @Around — wraps the method, controls execution (combines all of the above)

    @Around("execution(void com.example.service.TransferServiceImpl.transfer(..))")
    public void manageTransaction(ProceedingJoinPoint pjp) throws Throwable {
        logger.info(">>> begin transaction.");
        try {
            pjp.proceed();
            logger.info(">>> Transaction committed.");
        } catch (Throwable ex) {
            logger.info(">>> Transaction rolled back due to exception: {}", ex.getMessage());
            throw ex;
        } finally {
            logger.info(">>> end transaction.");
        }
    }

}
