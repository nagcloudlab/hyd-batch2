package com.example.demo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Repository {
    private ExecutorService ioThreadPool = Executors.newFixedThreadPool(5);

    public CompletableFuture<String> getProductById(String id) {
        return CompletableFuture.supplyAsync(() -> {
            // Simulate a long-running operation
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " - Fetching product from database...");
            return "Product with ID: " + id;
        }, ioThreadPool);
    }
}

class Service {

    private ExecutorService computeThreadPool = Executors.newFixedThreadPool(5);

    private Repository repository;

    public Service(Repository repository) {
        this.repository = repository;
    }

    public void getProductById(String id) {
        int i = 10;
        System.out.println(Thread.currentThread().getName() + " - Requesting product...");
        CompletableFuture<String> future = repository.getProductById(id);
        future.thenAcceptAsync(result -> {
            System.out.println(Thread.currentThread().getName() + " - " + result + " - i: " + i);
        }, computeThreadPool);
    }
}

public class Example {

    public static void main(String[] args) {

        Repository repository = new Repository();
        Service service = new Service(repository);

        // simulate multiple requests
        for (int i = 0; i < 5; i++) {
            final String productId = "ID_" + i;
            service.getProductById(productId);
        }

    }

}
