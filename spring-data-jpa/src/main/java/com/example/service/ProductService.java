package com.example.service;

import org.springframework.transaction.annotation.Transactional;

import com.example.entity.Product;
import com.example.repository.ProductRepository;

public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, rollbackFor = RuntimeException.class, noRollbackFor = IllegalArgumentException.class, timeout = 30, isolation = org.springframework.transaction.annotation.Isolation.SERIALIZABLE)
    public void updateProductStock(Long productId, int quantity) {
        var product = getProductById(productId);
        int newStock = product.getStock() - quantity;
        if (newStock < 0) {
            throw new RuntimeException("Insufficient stock for product: " + productId);
        }
        product.setStock(newStock);
        productRepository.save(product);
    }

    @Transactional
    public Product getProductById(Long productId) {
        var productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            throw new RuntimeException("Product not found: " + productId);
        }
        var product = productOpt.get();
        return product;
    }

}
