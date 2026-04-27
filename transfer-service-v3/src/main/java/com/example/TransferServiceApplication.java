package com.example;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.example.config.TransferServiceConfiguration;
import com.example.service.TransferService;

// In v1, we manually created and wired dependencies (acted as the assembler)
// In v2, Spring container does it for us — we just provide configuration
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
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(TransferServiceConfiguration.class);
        context.refresh();
        logger.info("Transfer Service Application started successfully.");

        // =====================================================================
        // RUN PHASE — use beans from the container
        // =====================================================================
        logger.info("=".repeat(70));
        logger.info("RUN PHASE");
        logger.info("=".repeat(70));

        //
        // Lookup bean by name and type
        TransferService transferService = context.getBean("transferService",
                TransferService.class);

        // At runtime, this is a Spring AOP proxy, not the actual TransferServiceImpl
        // logger.info("Bean class: {}", transferService.getClass().getName());

        new Thread(() -> {
            try {
                Thread.sleep(2000); // Simulate some delay before running the transfer
                logger.info("Initiating transfer operation...");
                transferService.transfer(new BigDecimal("100.00"), "123", "456");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(3000); // Simulate some delay before running the transfer
                logger.info("Initiating another transfer operation...");
                transferService.transfer(new BigDecimal("200.00"), "789", "012");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

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
