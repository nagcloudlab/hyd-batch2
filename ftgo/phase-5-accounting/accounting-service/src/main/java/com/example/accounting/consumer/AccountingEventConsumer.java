package com.example.accounting.consumer;

import com.example.accounting.event.AccountingEvent;
import com.example.accounting.service.AccountingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor @Slf4j
public class AccountingEventConsumer {

    private final AccountingService accountingService;

    @KafkaListener(topics = "ftgo.accounting.events", groupId = "accounting-service")
    public void handleAccountingEvent(AccountingEvent event) {
        log.info("[KAFKA-CONSUMER] Received {} for order #{}", event.getEventType(), event.getOrderId());

        if ("PAYMENT_REQUESTED".equals(event.getEventType())) {
            accountingService.processPaymentAndBill(event);
        }
        // Ignore PAYMENT_COMPLETED/FAILED (those are our own outgoing events)
    }
}
