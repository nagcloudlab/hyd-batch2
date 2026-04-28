package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
// Auto-detects: @Controller, @Service, @Repository, @Entity, JpaRepository in com.example.*
// No manual @EnableJpaRepositories needed — Spring Boot auto-configures it
@SpringBootApplication
public class TransferServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransferServiceApplication.class, args);
    }

}
