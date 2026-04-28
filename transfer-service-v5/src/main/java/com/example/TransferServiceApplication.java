package com.example;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.example.service.TransferService;

@Configuration
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = "com.example.repository")
@ComponentScan(basePackages = "com.example") // Scan for components in com.example package
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

        String npciBean1 = context.getBean("npciBean1", String.class);
        logger.info("Retrieved bean from NPCI config: {}", npciBean1);

        TransferService transferService = context.getBean("transferService", TransferService.class);

        // Two concurrent transfers — demos Isolation property of ACID
        // Both threads access the same accounts; @Transactional ensures they don't
        // interfere
        Thread t1 = new Thread(() -> {
            try {
                Thread.sleep(1000);
                logger.info("Thread-1: Initiating transfer...");
                transferService.transfer(new BigDecimal("100.00"), "456", "123");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                Thread.sleep(2000);
                logger.info("Thread-2: Initiating transfer...");
                transferService.transfer(new BigDecimal("10.00"), "456", "123");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        t1.start();
        // t2.start();

        // Wait for both threads to complete before shutting down
        t1.join();
        // t2.join();

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
