package com.example.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor @Slf4j
public class AccountingEventProducer {
    private static final String TOPIC = "ftgo.accounting.events";
    private final KafkaTemplate<String, AccountingEvent> kafkaTemplate;

    public void publish(AccountingEvent event) {
        log.info("[KAFKA-PRODUCER] Publishing {} for order #{}", event.getEventType(), event.getOrderId());
        kafkaTemplate.send(TOPIC, "order-" + event.getOrderId(), event);
    }
}
