package com.example.reactive;

import org.springframework.boot.CommandLineRunner;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final DatabaseClient db;

    public DataLoader(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public void run(String... args) {
        // Create table
        db.sql("CREATE TABLE IF NOT EXISTS products (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), price DOUBLE)")
                .then()
                .block();

        // Insert 100 products
        for (int i = 1; i <= 100; i++) {
            db.sql("INSERT INTO products (name, price) VALUES (:name, :price)")
                    .bind("name", "Product-" + i)
                    .bind("price", i * 10.0)
                    .then()
                    .block();
        }
        System.out.println("Loaded 100 products into DB");
    }
}
