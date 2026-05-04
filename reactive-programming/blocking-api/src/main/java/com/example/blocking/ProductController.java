package com.example.blocking;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository repository;

    public ProductController(ProductRepository repository) {
        this.repository = repository;
    }

    // Each request blocks a thread for ~200ms (simulating real DB/network latency)
    @GetMapping
    public List<Product> getAllProducts() throws InterruptedException {
        Thread.sleep(200); // simulate slow DB query
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) throws InterruptedException {
        Thread.sleep(200); // simulate slow DB query
        return repository.findById(id).orElseThrow();
    }
}
