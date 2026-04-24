package com.example;

import org.slf4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.example.config.TransferServiceConfiguration;
import com.example.service.TransferService;

public class TransferServiceApplication {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger("txr-service");

    public static void main(String[] args) {

        // -----------------------
        // Init/boot phase
        // -----------------------
        logger.info("-".repeat(70));
        // create & wire up the components based on the configuration
        // (e.g. using Spring Framework, manual wiring, etc.)
        ConfigurableApplicationContext context = null;
        // context = new ClassPathXmlApplicationContext("transfer-service.xml");
        context = new AnnotationConfigApplicationContext(TransferServiceConfiguration.class);
        logger.info("-".repeat(70));
        // -----------------------
        // Run phase
        // -----------------------
        logger.info("-".repeat(70));
        TransferService transferService = context.getBean("transferService", TransferService.class);
        System.out.println(transferService.getClass());
        transferService.transfer(300.00, "123", "456");
        logger.info("-".repeat(30));
        // transferService.transfer(150.00, "789", "012");

        logger.info("-".repeat(70));
        // -----------------------
        // Shutdown phase
        // -----------------------
        logger.info("-".repeat(70));
        // ... perform any necessary cleanup, resource release, etc.
        context.close();
        logger.info("Transfer Service Application is shutting down.");
        logger.info("-".repeat(70));

    }
}
