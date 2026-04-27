package com.example.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

// Custom Condition — checks if Postgres driver is on classpath before creating a bean
// This is what Spring Boot does automatically with @ConditionalOnClass
public class PostgresDriverCondition implements Condition {

    private static final Logger logger = LoggerFactory.getLogger(PostgresDriverCondition.class);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        try {
            Class.forName("org.postgresql.Driver");
            logger.info("Postgres driver found — condition TRUE");
            return true;
        } catch (ClassNotFoundException e) {
            logger.info("Postgres driver NOT found — condition FALSE");
            return false;
        }
    }

}
