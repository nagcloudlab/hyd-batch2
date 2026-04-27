package com.example.repository;

import com.example.model.Account;

// ISP — Interface Segregation Principle: small, focused interface with only the operations needed
// DIP — Dependency Inversion Principle: high-level modules (service layer) depend on this abstraction
public interface AccountRepository {

    // Returns the account, or null if not found
    Account findByNumber(String accountNumber);

    // Persists the account (insert or update)
    Account save(Account account);
}
