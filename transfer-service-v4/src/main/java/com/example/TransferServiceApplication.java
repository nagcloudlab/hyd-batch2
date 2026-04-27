package com.example;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.example.service.TransferService;

@SpringBootApplication
public class TransferServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(TransferServiceApplication.class);

    public static void main(String[] args) throws Exception {

        // =====================================================================
        // INIT/BOOT PHASE — Spring creates beans, wires dependencies, lifecycle
        // =====================================================================
        logger.info("=".repeat(70));
        logger.info("INIT/BOOT PHASE");
        logger.info("=".repeat(70));

        ConfigurableApplicationContext context = SpringApplication.run(TransferServiceApplication.class, args);
        logger.info("Transfer Service Application started successfully.");

        // =====================================================================
        // RUN PHASE — concurrent transfers to demo ACID Isolation
        // =====================================================================
        logger.info("=".repeat(70));
        logger.info("RUN PHASE");
        logger.info("=".repeat(70));

        TransferService transferService = context.getBean("transferService", TransferService.class);

        // Two concurrent transfers — demos Isolation property of ACID
        // Both threads access the same accounts; @Transactional ensures they don't
        // interfere
        Thread t1 = new Thread(() -> {
            try {
                Thread.sleep(1000);
                logger.info("Thread-1: Initiating transfer...");
                transferService.transfer(new BigDecimal("100.00"), "123", "456");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                Thread.sleep(2000);
                logger.info("Thread-2: Initiating transfer...");
                transferService.transfer(new BigDecimal("200.00"), "123", "456");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        t1.start();
        t2.start();

        // Wait for both threads to complete before shutting down
        t1.join();
        t2.join();

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
