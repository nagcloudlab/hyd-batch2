package com.example.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

// OCP — Open/Closed Principle
// Uses a registry (Map) instead of if/else chain
// New repository types can be added via register() without modifying this class
public class AccountRepositoryFactory {

    private static final Map<String, Supplier<AccountRepository>> registry = new HashMap<>();

    // Pre-register known implementations
    static {
        registry.put("jdbc", JdbcAccountRepository::new);
        registry.put("jpa", JpaAccountRepository::new);
    }

    // Open for extension — register new types at runtime without changing existing code
    public static void register(String type, Supplier<AccountRepository> supplier) {
        registry.put(type.toLowerCase(), supplier);
    }

    public static AccountRepository createAccountRepository(String type) {
        Supplier<AccountRepository> supplier = registry.get(type.toLowerCase());
        if (supplier == null) {
            throw new IllegalArgumentException("Unknown repository type: " + type);
        }
        return supplier.get();
    }

}
