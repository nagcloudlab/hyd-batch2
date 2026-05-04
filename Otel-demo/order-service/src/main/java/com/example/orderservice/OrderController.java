package com.example.orderservice;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private static final String PRODUCT_SERVICE_URL = "http://localhost:8081/api/products";

    private final RestTemplate restTemplate;
    private final Tracer tracer;
    private final List<Order> orders = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public OrderController(RestTemplate restTemplate, OpenTelemetry openTelemetry) {
        this.restTemplate = restTemplate;
        this.tracer = openTelemetry.getTracer("order-service");
    }

    // POST /api/orders  { "productId": 1, "quantity": 2 }
    // Creates a distributed trace: order-service -> product-service
    @PostMapping
    public Order createOrder(@RequestBody OrderRequest request) {
        log.info("Creating order for productId: {}, quantity: {}", request.getProductId(), request.getQuantity());

        // Step 1: Check stock from product-service
        Span stockSpan = tracer.spanBuilder("validate-stock").startSpan();
        Map stockResponse;
        try (Scope scope = stockSpan.makeCurrent()) {
            stockSpan.setAttribute("product.id", request.getProductId());
            String stockUrl = PRODUCT_SERVICE_URL + "/" + request.getProductId() + "/check-stock";
            stockResponse = restTemplate.getForObject(stockUrl, Map.class);
            boolean available = (boolean) stockResponse.get("available");
            stockSpan.setAttribute("stock.available", available);
            if (!available) {
                stockSpan.setAttribute("error", true);
                throw new RuntimeException("Product out of stock!");
            }
        } finally {
            stockSpan.end();
        }

        // Step 2: Get product details
        Span productSpan = tracer.spanBuilder("fetch-product-details").startSpan();
        Map productResponse;
        try (Scope scope = productSpan.makeCurrent()) {
            String productUrl = PRODUCT_SERVICE_URL + "/" + request.getProductId();
            productResponse = restTemplate.getForObject(productUrl, Map.class);
            productSpan.setAttribute("product.name", (String) productResponse.get("name"));
        } finally {
            productSpan.end();
        }

        // Step 3: Create order (local processing)
        Span createSpan = tracer.spanBuilder("persist-order").startSpan();
        try (Scope scope = createSpan.makeCurrent()) {
            simulateDbWrite();
            String productName = (String) productResponse.get("name");
            double price = ((Number) productResponse.get("price")).doubleValue();
            double total = price * request.getQuantity();

            Order order = new Order(
                    idCounter.getAndIncrement(),
                    request.getProductId(),
                    productName,
                    request.getQuantity(),
                    total,
                    "CONFIRMED"
            );
            orders.add(order);
            createSpan.setAttribute("order.id", order.getId());
            createSpan.setAttribute("order.total", total);
            log.info("Order created: id={}, total={}", order.getId(), total);
            return order;
        } finally {
            createSpan.end();
        }
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orders;
    }

    private void simulateDbWrite() {
        try {
            Thread.sleep(30); // simulate DB write latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
