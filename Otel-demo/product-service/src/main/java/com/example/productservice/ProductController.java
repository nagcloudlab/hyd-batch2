package com.example.productservice;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final Tracer tracer;

    private final Map<Long, Product> products = new ConcurrentHashMap<>(Map.of(
            1L, new Product(1L, "Laptop", 999.99, 50),
            2L, new Product(2L, "Phone", 699.99, 100),
            3L, new Product(3L, "Tablet", 449.99, 75)
    ));

    public ProductController(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer("product-service");
    }

    @GetMapping
    public List<Product> getAllProducts() {
        log.info("Fetching all products");
        return List.copyOf(products.values());
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        log.info("Fetching product with id: {}", id);

        Span span = tracer.spanBuilder("product-db-lookup").startSpan();
        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("product.id", id);
            simulateDbCall();
            Product product = products.get(id);
            if (product == null) {
                span.setAttribute("product.found", false);
                throw new RuntimeException("Product not found: " + id);
            }
            span.setAttribute("product.found", true);
            span.setAttribute("product.name", product.getName());
            return product;
        } finally {
            span.end();
        }
    }

    @GetMapping("/{id}/check-stock")
    public Map<String, Object> checkStock(@PathVariable Long id) {
        log.info("Checking stock for product: {}", id);

        Span span = tracer.spanBuilder("stock-db-check").startSpan();
        try (Scope scope = span.makeCurrent()) {
            simulateDbCall();
            Product product = products.get(id);
            if (product == null) {
                throw new RuntimeException("Product not found: " + id);
            }
            span.setAttribute("product.id", id);
            span.setAttribute("stock.quantity", product.getStock());
            return Map.of(
                    "productId", id,
                    "productName", product.getName(),
                    "stock", product.getStock(),
                    "available", product.getStock() > 0
            );
        } finally {
            span.end();
        }
    }

    private void simulateDbCall() {
        try {
            Thread.sleep(50); // simulate DB latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
