package com.example.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// @EventListener — method-level, Spring routes events by type
// Publisher doesn't know about listeners — fully decoupled communication
@Component
public class TransferNotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(TransferNotificationListener.class);

    @EventListener
    public void onTransferCompleted(TransferCompletedEvent event) {
        logger.info(">>> SMS notification: ${} transferred from {} to {}",
                event.getAmount(), event.getFromAccount(), event.getToAccount());
    }

}
