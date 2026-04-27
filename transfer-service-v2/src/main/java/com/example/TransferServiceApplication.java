package com.example;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.example.cache.ApplicationCache;
import com.example.config.TransferServiceConfiguration;
import com.example.repository.AccountRepository;
import com.example.service.TransferService;

// In v1, we manually created and wired dependencies (acted as the assembler)
// In v2, Spring container does it for us — we just provide configuration
public class TransferServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(TransferServiceApplication.class);

    public static void main(String[] args) {

        // -----------------------
        // Init/boot phase
        // -----------------------
        // Spring container creates all beans, wires dependencies, and calls lifecycle
        // callbacks
        logger.info("=".repeat(70));
        logger.info("INIT/BOOT PHASE");
        logger.info("=".repeat(70));

        // XML config: new ClassPathXmlApplicationContext("transfer-service.xml");
        // Java config: AnnotationConfigApplicationContext with @Configuration class
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(TransferServiceConfiguration.class);
        // context.getEnvironment().setActiveProfiles("dev");
        context.refresh(); // Triggers bean creation and wiring
        logger.info("Transfer Service Application started successfully.");

        // -----------------------
        // Run phase
        // -----------------------
        logger.info("=".repeat(70));
        logger.info("RUN PHASE");
        logger.info("=".repeat(70));

        AccountRepository accountRepository1 = context.getBean(AccountRepository.class);
        AccountRepository accountRepository2 = context.getBean(AccountRepository.class);
        logger.info("AccountRepository instance 1: {}", accountRepository1.hashCode());
        logger.info("AccountRepository instance 2: {}", accountRepository2.hashCode());
        logger.info("Are both AccountRepository instances the same? {}", accountRepository1 == accountRepository2);

        ApplicationCache cache = context.getBean(ApplicationCache.class); // Triggers lazy initialization of
                                                                          // ApplicationCache

        // Lookup bean from container by name and type
        TransferService transferService = context.getBean("transferService", TransferService.class);

        // At runtime, this is a Spring AOP proxy, not the actual TransferServiceImpl
        logger.info("Bean class: {}", transferService.getClass().getName());

        transferService.transfer(new BigDecimal("300.00"), "123", "456");
        logger.info("-".repeat(70));
        transferService.transfer(new BigDecimal("150.00"), "789", "012");

        // -----------------------
        // Shutdown phase
        // -----------------------
        logger.info("=".repeat(70));
        logger.info("SHUTDOWN PHASE");
        logger.info("=".repeat(70));

        // Triggers @PreDestroy callbacks and releases resources
        context.close();
        logger.info("Transfer Service Application is shutting down.");

    }
}
