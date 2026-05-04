package com.example.blocking;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final ProductRepository repository;

    public DataLoader(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        for (int i = 1; i <= 100; i++) {
            repository.save(new Product("Product-" + i, i * 10.0));
        }
        System.out.println("Loaded 100 products into DB");
    }
}
