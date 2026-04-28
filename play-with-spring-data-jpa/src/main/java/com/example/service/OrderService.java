package com.example.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.CartLine;
import com.example.entity.Order;
import com.example.repository.OrderRepository;
import com.example.repository.ProductRepository;

import jakarta.annotation.PostConstruct;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository,
            ProductService productService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.productService = productService;
    }

    // @PostConstruct
    public void init() {
        // insert some products into the database for testing
        productRepository.saveAll(List.of(
                new com.example.entity.Product("Product A", 10.0, 100),
                new com.example.entity.Product("Product B", 20.0, 50),
                new com.example.entity.Product("Product C", 30.0, 25)));
    }

    // Unit Work Work.. ( transaction)
    // Must satift 4 properties of transaction
    // 1. Atomicity: All operations within the transaction must succeed or fail
    // together.
    // 2. Consistency: The transaction must bring the database from one valid state
    // to another, maintaining data integrity.
    // 3. Isolation: Transactions must be isolated from each other, preventing
    // concurrent transactions from interfering with each other.
    // 4. Durability: Once a transaction is committed, its changes must be
    // permanent, even in the case of a system failure.

    @Transactional(transactionManager = "transactionManager", rollbackFor = RuntimeException.class, noRollbackFor = IllegalArgumentException.class, timeout = 30, isolation = org.springframework.transaction.annotation.Isolation.SERIALIZABLE)
    public void placeOrder(List<CartLine> cart) {

        double totalAmount = 0.0;
        for (CartLine line : cart) {
            var productOpt = productRepository.findById(line.getProductId());
            if (productOpt.isEmpty()) {
                throw new RuntimeException("Product not found: " + line.getProductId());
            }
            var product = productOpt.get();
            totalAmount += product.getPrice() * line.getQuantity();
        }
        // Here you would create an Order entity, set its total amount, and save it
        // using orderRepository

        Order order = new Order();
        order.setTotalAmount(totalAmount);
        // You would also need to set the order items, which could be a list of product
        // IDs or a more complex structure
        // For simplicity, let's assume we just store the product IDs in the order
        List<Long> productIds = cart.stream().map(CartLine::getProductId).toList();
        order.setOrderItems(productIds);

        orderRepository.save(order);

        boolean simulateError = true; // Change to true to simulate an error and trigger rollback
        if (simulateError) {
            // throw new RuntimeException("Simulated error to trigger rollback"); //
            // un-checked exception, will trigger rollback
            // throw new IOException(); // checked exception, will NOT trigger rollback
        }

        // reduce stock of products, etc.
        for (CartLine line : cart) {
            productService.updateProductStock(line.getProductId(), line.getQuantity());
        }

    }

}
