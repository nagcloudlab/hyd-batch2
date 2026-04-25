package com.example;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.example.config.TransferServiceConfiguration;
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
        ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(
                TransferServiceConfiguration.class);
        logger.info("Transfer Service Application started successfully.");

        // -----------------------
        // Run phase
        // -----------------------
        logger.info("=".repeat(70));
        logger.info("RUN PHASE");
        logger.info("=".repeat(70));

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
