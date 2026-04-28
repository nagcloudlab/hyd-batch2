package com.example;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
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
        SpringApplication.run(TransferServiceApplication.class, args);
        logger.info("Transfer Service Application started successfully.");

    }

}
