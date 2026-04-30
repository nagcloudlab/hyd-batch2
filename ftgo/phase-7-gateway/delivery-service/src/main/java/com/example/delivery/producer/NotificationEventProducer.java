package com.example.delivery.producer;

import com.example.delivery.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventProducer {

    private static final String TOPIC = "ftgo.notification.events";

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    public void publish(NotificationEvent event) {
        log.info("[KAFKA-PRODUCER] Publishing {} for order #{} to {}", event.getEventType(), event.getOrderId(), TOPIC);
        kafkaTemplate.send(TOPIC, "order-" + event.getOrderId(), event);
    }
}
