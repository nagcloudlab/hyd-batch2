package com.example.reactive;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Duration;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository repository;

    public ProductController(ProductRepository repository) {
        this.repository = repository;
    }

    // Same 200ms delay, but NO thread is blocked — it's released back to the pool
    @GetMapping
    public Flux<Product> getAllProducts() {
        return Mono.delay(Duration.ofMillis(200)) // simulate slow DB query
                .flatMapMany(tick -> repository.findAll());
    }

    @GetMapping("/{id}")
    public Mono<Product> getProduct(@PathVariable Long id) {
        return Mono.delay(Duration.ofMillis(200)) // simulate slow DB query
                .flatMap(tick -> repository.findById(id));
    }
}
