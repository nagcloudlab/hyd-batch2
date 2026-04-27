package com.example.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TransferNotificationListener {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
            .getLogger(TransferNotificationListener.class);

    @EventListener
    public void handleTransferCompleted(TransferCompletedEvent event) {
        // In a real application, you might send an email or push notification here
        logger.info("TransferNotificationListener > Transfer completed: {} from {} to {}",
                event.getAmount(), event.getFromAccount(), event.getToAccount());
    }

}
