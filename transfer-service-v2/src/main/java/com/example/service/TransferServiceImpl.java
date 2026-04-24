package com.example.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.example.repository.AccountRepository;

// @Component("transferService")
@Service("transferService")
public class TransferServiceImpl implements TransferService {

        private AccountRepository accountRepository;

        private static final Logger logger = org.slf4j.LoggerFactory.getLogger("txr-service");

        // constructor DI
        // @Autowired
        public TransferServiceImpl(@Qualifier("jdbc") AccountRepository accountRepository) {
                this.accountRepository = accountRepository;
                logger.info("TransferServiceImpl initialized with {} repository.",
                                accountRepository.getClass().getSimpleName());
        }

        // // setter DI
        // @Autowired(required = true)
        // public void setAccountRepository(AccountRepository accountRepository) {
        // this.accountRepository = accountRepository;
        // logger.info("AccountRepository set to {}.",
        // accountRepository.getClass().getSimpleName());
        // }

        @PostConstruct
        public void init() {
                logger.info("TransferServiceImpl bean is initialized and ready to use.");
        }

        @PreDestroy
        public void destroy() {
                logger.info("TransferServiceImpl bean is being destroyed.");
        }

        public void transfer(double amount, String fromAccountNumber, String toAccountNumber) {

                logger.info("Initiating transfer of ${} from account {} to account {}.", amount, fromAccountNumber,
                                toAccountNumber);
                // step-1: Load 'from' account
                var fromAccount = accountRepository.findByNumber(fromAccountNumber);

                if (fromAccount.getBalance() < amount) {
                        logger.error("Insufficient funds in account {}. Available balance: ${}. Transfer aborted.",
                                        fromAccountNumber, fromAccount.getBalance());
                        return;
                }

                // step-2: Load 'to' account
                var toAccount = accountRepository.findByNumber(toAccountNumber);
                // step-3: Debit 'from' account
                fromAccount.setBalance(fromAccount.getBalance() - amount);
                logger.info("Debited ${} from account {}. New balance: ${}.", amount, fromAccountNumber,
                                fromAccount.getBalance());
                // step-4: Credit 'to' account
                toAccount.setBalance(toAccount.getBalance() + amount);
                logger.info("Credited ${} to account {}. New balance: ${}.", amount, toAccountNumber,
                                toAccount.getBalance());
                // step-5: Save updated accounts
                accountRepository.save(fromAccount);

                boolean simulateError = false; // Set to true to test transaction rollback
                if (simulateError) {
                        logger.warn("Simulating an error after debiting 'from' account but before saving 'to' account.");
                        throw new RuntimeException("Simulated error to test transaction rollback.");
                }

                accountRepository.save(toAccount);

                logger.info("Transfer of ${} from account {} to account {} completed successfully.", amount,
                                fromAccountNumber,
                                toAccountNumber);

        }

}
