package com.example.service;

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

        public void transfer(double amount, String fromAccountNumber, String toAccountNumber) {
                logger.info("Initiating transfer of ${} from account {} to account {}.", amount, fromAccountNumber,
                                toAccountNumber);

                // Don't create repository instances here
                // JdbcAccountRepository accountRepository = new JdbcAccountRepository();
                // Don't create by factory, inject it from outside
                // AccountRepository accountRepository =
                // AccountRepositoryFactory.createAccountRepository("jpa");

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
                accountRepository.save(toAccount);

                logger.info("Transfer of ${} from account {} to account {} completed successfully.", amount,
                                fromAccountNumber,
                                toAccountNumber);
        }

}
