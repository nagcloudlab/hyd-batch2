package com.example;

import org.slf4j.Logger;

import com.example.service.TransferServiceImpl;

public class TransferServiceApplication {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger("txr-service");

    public static void main(String[] args) {

        // -----------------------
        // Init/boot phase
        // -----------------------
        logger.info("-".repeat(70));
        // create & wire up the components based on the configuration
        // (e.g. using Spring Boot, or manually)
        TransferServiceImpl transferService = new TransferServiceImpl();
        logger.info("Transfer Service Application started successfully.");
        logger.info("-".repeat(70));
        // -----------------------
        // Run phase
        // -----------------------
        transferService.transfer(300.00, "123", "456");
        logger.info("-".repeat(30));
        transferService.transfer(150.00, "789", "012");

        logger.info("-".repeat(70));
        // -----------------------
        // Shutdown phase
        // -----------------------
        logger.info("-".repeat(70));
        // ... perform any necessary cleanup, resource release, etc.
        logger.info("Transfer Service Application is shutting down.");
        logger.info("-".repeat(70));

    }
}
