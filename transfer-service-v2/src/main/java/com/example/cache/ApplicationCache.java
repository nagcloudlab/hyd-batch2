package com.example.cache;

import org.slf4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class ApplicationCache {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ApplicationCache.class);

    public ApplicationCache() {
        logger.info("ApplicationCache initialized");
    }
}
