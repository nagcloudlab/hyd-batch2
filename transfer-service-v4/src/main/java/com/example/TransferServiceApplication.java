package com.example;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.example.service.TransferService;

// In v1, we manually created and wired dependencies (acted as the assembler)
// In v2, Spring container does it for us — we just provide configuration
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.example")
@EnableAspectJAutoProxy
@EnableTransactionManagement
public class TransferServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(TransferServiceApplication.class);

    public static void main(String[] args) throws Exception {

        // =====================================================================
        // INIT/BOOT PHASE — Spring creates beans, wires dependencies, lifecycle
        // =====================================================================
        logger.info("=".repeat(70));
        logger.info("INIT/BOOT PHASE");
        logger.info("=".repeat(70));

        // Java config: AnnotationConfigApplicationContext with @Configuration class
        ConfigurableApplicationContext context = SpringApplication.run(TransferServiceApplication.class, args);
        logger.info("Transfer Service Application started successfully.");

        // =====================================================================
        // RUN PHASE — use beans from the container
        // =====================================================================
        logger.info("=".repeat(70));
        logger.info("RUN PHASE");
        logger.info("=".repeat(70));
        // Lookup bean by name and type
        TransferService transferService = context.getBean("transferService",
                TransferService.class);

        // At runtime, this is a Spring AOP proxy, not the actual TransferServiceImpl
        // logger.info("Bean class: {}", transferService.getClass().getName());

        logger.info("Initiating transfer operation...");
        transferService.transfer(new BigDecimal("100.00"), "456", "123");

        // =====================================================================
        // SHUTDOWN PHASE — @PreDestroy callbacks and resource cleanup
        // =====================================================================
        logger.info("=".repeat(70));
        logger.info("SHUTDOWN PHASE");
        logger.info("=".repeat(70));

        context.close();
        logger.info("Transfer Service Application shut down.");
    }

}
