package com.example;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.repository.AccountRepository;
import com.example.repository.AccountRepositoryFactory;
import com.example.service.TransferService;
import com.example.service.TransferServiceImpl;

// This class acts as the manual assembler / container
// It creates dependencies, wires them together, and runs the app
// In v2, Spring framework will automate all of this for us
public class TransferServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(TransferServiceApplication.class);

    public static void main(String[] args) {

        // -----------------------
        // Init/boot phase
        // -----------------------
        // Manually wiring dependencies — this is what Spring framework automates for us
        logger.info("=".repeat(70));
        logger.info("INIT/BOOT PHASE");
        logger.info("=".repeat(70));
        AccountRepository accountRepository = AccountRepositoryFactory.createAccountRepository("jdbc");
        TransferService transferService = new TransferServiceImpl(accountRepository); // Constructor DI
        logger.info("Transfer Service Application started successfully.");

        // -----------------------
        // Run phase
        // -----------------------
        // Use new BigDecimal("300.00") not BigDecimal.valueOf(300.00) — string constructor avoids double precision issues
        logger.info("=".repeat(70));
        logger.info("RUN PHASE");
        logger.info("=".repeat(70));
        transferService.transfer(new BigDecimal("300.00"), "123", "456");
        logger.info("-".repeat(70));
        transferService.transfer(new BigDecimal("150.00"), "789", "012");

        // -----------------------
        // Shutdown phase
        // -----------------------
        logger.info("=".repeat(70));
        logger.info("SHUTDOWN PHASE");
        logger.info("=".repeat(70));
        logger.info("Transfer Service Application is shutting down.");

    }
}
