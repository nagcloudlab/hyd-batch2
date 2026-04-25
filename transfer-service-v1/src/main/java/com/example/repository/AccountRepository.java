package com.example.repository;

import com.example.model.Account;

// ISP — Interface Segregation Principle
// Small, focused interface with only the operations needed
// DIP — Dependency Inversion Principle
// High-level modules (service layer) depend on this abstraction, not on concrete implementations
public interface AccountRepository {
    Account findByNumber(String accountNumber);

    Account save(Account account);
}
