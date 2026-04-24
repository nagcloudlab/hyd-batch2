package com.example.repository;

public class AccountRepositoryFactory {

    public static AccountRepository createAccountRepository(String type) {
        if (type.equalsIgnoreCase("jdbc")) {
            return new JdbcAccountRepository();
        } else if (type.equalsIgnoreCase("jpa")) {
            return new JpaAccountRepository();
        } else {
            throw new IllegalArgumentException("Unknown repository type: " + type);
        }
    }

}
