package com.npci.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "npci", name = "location", havingValue = "hyderabad")
// @ConditionalOnWebApplication
public class NpciHydConfiguration {

    @Bean
    public String npciBean1() {
        return "This is NPCI Hyderabad Configuration Bean 1";
    }

}
