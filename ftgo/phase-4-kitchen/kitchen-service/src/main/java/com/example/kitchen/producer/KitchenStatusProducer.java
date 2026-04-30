package com.example.kitchen.producer;

import com.example.kitchen.event.KitchenEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor @Slf4j
public class KitchenStatusProducer {
    private static final String TOPIC = "ftgo.kitchen.events";
    private final KafkaTemplate<String, KitchenEvent> kafkaTemplate;

    public void publishStatusUpdate(Long orderId, String newStatus) {
        KitchenEvent event = KitchenEvent.builder()
                .eventType("TICKET_STATUS_UPDATED")
                .orderId(orderId)
                .items(newStatus)   // reuse items field to carry the new order status
                .build();
        log.info("[KAFKA-PRODUCER] Publishing TICKET_STATUS_UPDATED → {} for order #{}", newStatus, orderId);
        kafkaTemplate.send(TOPIC, "order-" + orderId, event);
    }
}
