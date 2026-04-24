package com.example.repository;

import com.example.model.Account;

public interface AccountRepository {
    Account findByNumber(String accountNumber);

    Account save(Account account);
}
