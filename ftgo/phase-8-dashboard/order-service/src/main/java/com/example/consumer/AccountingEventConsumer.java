package com.example.consumer;

import com.example.config.OrderStatusBroadcaster;
import com.example.entity.FoodOrder;
import com.example.event.AccountingEvent;
import com.example.repository.FoodOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component @RequiredArgsConstructor @Slf4j
public class AccountingEventConsumer {

    private final FoodOrderRepository foodOrderRepository;
    private final OrderStatusBroadcaster broadcaster;

    @KafkaListener(topics = "ftgo.accounting.events", groupId = "ftgo-monolith")
    @Transactional
    public void handleAccountingEvent(AccountingEvent event) {
        if ("PAYMENT_REQUESTED".equals(event.getEventType())) return;
        log.info("[KAFKA-CONSUMER] Received {} for order #{}", event.getEventType(), event.getOrderId());

        FoodOrder order = foodOrderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null) return;

        switch (event.getEventType()) {
            case "PAYMENT_COMPLETED" -> order.setPaymentStatus("COMPLETED");
            case "PAYMENT_FAILED" -> order.setPaymentStatus("FAILED");
        }
        foodOrderRepository.save(order);
        broadcaster.broadcast(event.getOrderId(), "PAYMENT_" + order.getPaymentStatus(), "accounting");
    }
}
