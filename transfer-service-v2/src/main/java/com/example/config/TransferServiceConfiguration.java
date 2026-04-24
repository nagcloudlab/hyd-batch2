package com.example.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@ComponentScan(basePackages = "com.example")
@EnableAspectJAutoProxy
public class TransferServiceConfiguration {

    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/mydatabase}")
    private String jdbcUrl;
    @Value("${spring.datasource.username:postgres}")
    private String username;
    @Value("${spring.datasource.password:mysecretpassword}")
    private String password;
    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;
    @Value("${spring.datasource.maximum-pool-size:5}")
    private int maximumPoolSize;

    @Bean
    public DataSource dataSource() {
        // Configure and return the DataSource bean
        // For example, using HikariCP:
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        dataSource.setMaximumPoolSize(5);
        return dataSource;
    }

}
