package com.example.accounting.service;

import com.example.accounting.adapter.StripeAdapter;
import com.example.accounting.entity.*;
import com.example.accounting.event.AccountingEvent;
import com.example.accounting.producer.AccountingEventProducer;
import com.example.accounting.repository.BillRepository;
import com.example.accounting.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service @RequiredArgsConstructor @Transactional @Slf4j
public class AccountingService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final StripeAdapter stripeAdapter;
    private final AccountingEventProducer accountingEventProducer;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.05");       // 5% GST
    private static final BigDecimal DELIVERY_FEE = new BigDecimal("30.00");  // Rs.30

    public void processPaymentAndBill(AccountingEvent event) {
        // Idempotency
        if (paymentRepository.findByOrderId(event.getOrderId()).isPresent()) {
            log.warn("Payment already exists for order #{}, skipping", event.getOrderId());
            return;
        }

        try {
            // Process payment via Stripe
            String stripePaymentId = stripeAdapter.charge(event.getAmount(),
                    "Order #" + event.getOrderId() + " from " + event.getRestaurantName());

            Payment payment = Payment.builder()
                    .orderId(event.getOrderId())
                    .amount(event.getAmount())
                    .status(PaymentStatus.COMPLETED)
                    .stripePaymentId(stripePaymentId)
                    .paymentTime(LocalDateTime.now())
                    .build();
            paymentRepository.save(payment);

            // Generate bill
            BigDecimal tax = event.getAmount().multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
            BigDecimal total = event.getAmount().add(tax).add(DELIVERY_FEE);

            Bill bill = Bill.builder()
                    .orderId(event.getOrderId())
                    .amount(event.getAmount())
                    .tax(tax)
                    .deliveryFee(DELIVERY_FEE)
                    .totalAmount(total)
                    .generatedTime(LocalDateTime.now())
                    .build();
            billRepository.save(bill);

            log.info("Payment + Bill created for order #{}, total: Rs.{}", event.getOrderId(), total);

            // Publish PAYMENT_COMPLETED back to monolith
            accountingEventProducer.publish(AccountingEvent.builder()
                    .eventType("PAYMENT_COMPLETED")
                    .orderId(event.getOrderId())
                    .paymentStatus("COMPLETED")
                    .stripePaymentId(stripePaymentId)
                    .billTotal(total)
                    .build());

        } catch (Exception e) {
            log.error("Payment failed for order #{}: {}", event.getOrderId(), e.getMessage());
            accountingEventProducer.publish(AccountingEvent.builder()
                    .eventType("PAYMENT_FAILED")
                    .orderId(event.getOrderId())
                    .paymentStatus("FAILED")
                    .build());
        }
    }

    public Payment getPaymentByOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
    }

    public Bill getBillByOrder(Long orderId) {
        return billRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Bill not found for order: " + orderId));
    }
}
