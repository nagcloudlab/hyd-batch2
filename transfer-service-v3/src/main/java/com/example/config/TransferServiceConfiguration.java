package com.example.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;

// @Configuration — marks this class as a source of bean definitions (replaces XML config)
// @ComponentScan — tells Spring to scan 'com.example' package for @Component, @Service, @Repository beans
// @EnableAspectJAutoProxy — enables AOP proxy creation for @Aspect beans
// @PropertySource — loads profile-specific properties into Spring Environment
@Configuration
@ComponentScan(basePackages = "com.example")
@EnableAspectJAutoProxy
@EnableTransactionManagement
@PropertySource("classpath:application.properties")
public class TransferServiceConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(TransferServiceConfiguration.class);

    // @Value — injects externalized properties; uses ${key} placeholder syntax
    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.maximum-pool-size}")
    private int maximumPoolSize;

    // @Bean — method-level annotation, Spring calls this and manages the returned
    // object
    // @Conditional — bean created only if PostgresDriverCondition.matches() returns
    // true
    @Bean
    public DataSource dataSource() {
        logger.info("Creating DataSource bean with URL: {}", jdbcUrl);
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        dataSource.setMaximumPoolSize(maximumPoolSize);
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new org.springframework.jdbc.datasource.DataSourceTransactionManager(dataSource);
    }

}
