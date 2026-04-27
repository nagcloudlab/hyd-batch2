package com.example.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

// @Lazy — delays bean creation until first access (default is eager — created at startup)
// Useful for heavy beans (external service clients, large caches)
@Component
@Lazy
public class ApplicationCache {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationCache.class);

    public ApplicationCache() {
        logger.info("ApplicationCache initialized.");
    }

}
