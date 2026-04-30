package com.example.service;

import com.example.adapter.StripeAdapter;
import com.example.entity.FoodOrder;
import com.example.entity.Payment;
import com.example.entity.PaymentStatus;
import com.example.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final StripeAdapter stripeAdapter;

    public Payment processPayment(FoodOrder order) {
        String stripePaymentId = stripeAdapter.charge(
                order.getTotalAmount(),
                "Order #" + order.getId() + " from " + order.getRestaurant().getName()
        );

        Payment payment = Payment.builder()
                .foodOrder(order)
                .amount(order.getTotalAmount())
                .status(PaymentStatus.COMPLETED)
                .stripePaymentId(stripePaymentId)
                .paymentTime(LocalDateTime.now())
                .build();

        return paymentRepository.save(payment);
    }

    public Payment getPaymentByOrder(Long orderId) {
        return paymentRepository.findByFoodOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
    }
}
