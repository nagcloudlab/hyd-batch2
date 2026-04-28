package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.model.Account;

// ISP — Interface Segregation Principle: small, focused interface with only the operations needed
// DIP — Dependency Inversion Principle: high-level modules (service layer) depend on this abstraction
public interface AccountRepository extends JpaRepository<Account, String> {

}
