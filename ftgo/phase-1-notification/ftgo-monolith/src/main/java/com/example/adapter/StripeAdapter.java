package com.example.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Slf4j
public class StripeAdapter {

    public String charge(BigDecimal amount, String description) {
        // Mock Stripe payment service
        String paymentId = "stripe_" + UUID.randomUUID().toString().substring(0, 8);
        log.info("[STRIPE-MOCK] Charging {} for '{}'. Payment ID: {}", amount, description, paymentId);
        return paymentId;
    }

    public boolean refund(String paymentId) {
        log.info("[STRIPE-MOCK] Refunding payment: {}", paymentId);
        return true;
    }
}
