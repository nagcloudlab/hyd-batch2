package com.example.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private static final String TOPIC = "ftgo.order.events";

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void publish(OrderEvent event) {
        log.info("[KAFKA-PRODUCER] Publishing {} for order #{} to {}", event.getEventType(), event.getOrderId(), TOPIC);
        kafkaTemplate.send(TOPIC, "order-" + event.getOrderId(), event);
    }
}
