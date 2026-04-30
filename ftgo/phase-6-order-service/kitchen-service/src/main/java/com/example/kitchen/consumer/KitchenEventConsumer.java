package com.example.kitchen.consumer;

import com.example.kitchen.event.KitchenEvent;
import com.example.kitchen.service.KitchenTicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor @Slf4j
public class KitchenEventConsumer {

    private final KitchenTicketService kitchenTicketService;

    @KafkaListener(topics = "ftgo.kitchen.events", groupId = "kitchen-service")
    public void handleKitchenEvent(KitchenEvent event) {
        log.info("[KAFKA-CONSUMER] Received {} for order #{}", event.getEventType(), event.getOrderId());

        if ("ORDER_PLACED".equals(event.getEventType())) {
            kitchenTicketService.createTicket(event);
        } else {
            log.warn("Unknown kitchen event type: {}", event.getEventType());
        }
    }
}
