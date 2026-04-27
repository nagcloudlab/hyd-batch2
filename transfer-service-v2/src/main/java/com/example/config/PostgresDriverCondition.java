package com.example.config;

public class PostgresDriverCondition implements org.springframework.context.annotation.Condition {

    @Override
    public boolean matches(org.springframework.context.annotation.ConditionContext context,
            org.springframework.core.type.AnnotatedTypeMetadata metadata) {
        try {
            Class.forName("org.postgresql.Driver");
            return true; // Driver is present, condition matches
        } catch (ClassNotFoundException e) {
            return false; // Driver not found, condition does not match
        }

        // return false; // For demonstration, we assume the driver is always present.
        // In a real
        // application, you would check for the actual driver class.
    }

}
