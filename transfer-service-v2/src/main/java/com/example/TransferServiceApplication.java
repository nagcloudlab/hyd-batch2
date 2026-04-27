package com.example;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.example.cache.ApplicationCache;
import com.example.config.TransferServiceConfiguration;
import com.example.repository.AccountRepository;
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
        // XML config alternative: new ClassPathXmlApplicationContext("beans.xml")
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(TransferServiceConfiguration.class);
        // Activate profile programmatically (alternative to
        // -Dspring.profiles.active=dev)
        // context.getEnvironment().setActiveProfiles("dev");
        context.refresh();
        logger.info("Transfer Service Application started successfully.");

        // =====================================================================
        // RUN PHASE — use beans from the container
        // =====================================================================
        logger.info("=".repeat(70));
        logger.info("RUN PHASE");
        logger.info("=".repeat(70));

        // Bean Scopes demo — singleton returns same instance, prototype returns new
        // each time
        // AccountRepository repo1 = context.getBean(AccountRepository.class);
        // AccountRepository repo2 = context.getBean(AccountRepository.class);
        // logger.info("Singleton — same instance? {} (r1={}, r2={})",
        // repo1 == repo2, repo1.hashCode(), repo2.hashCode());

        // @Lazy demo — ApplicationCache is created only when first accessed, not at
        // startup
        // ApplicationCache cache = context.getBean(ApplicationCache.class);

        // Lookup bean by name and type
        TransferService transferService = context.getBean("transferService", TransferService.class);

        // At runtime, this is a Spring AOP proxy, not the actual TransferServiceImpl
        // logger.info("Bean class: {}", transferService.getClass().getName());

        transferService.transfer(new BigDecimal("300.00"), "123", "456");
        logger.info("-".repeat(70));
        transferService.transfer(new BigDecimal("150.00"), "789", "012");

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
