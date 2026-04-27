package com.example.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;

// @Configuration — marks this class as a source of bean definitions (replaces XML config)
// @ComponentScan — scans 'com.example' for @Component, @Service, @Repository beans
// @EnableTransactionManagement — enables @Transactional annotation processing (creates AOP proxies)
// @PropertySource — loads properties into Spring Environment
//
// Pain point: ALL of this manual config is eliminated by Spring Boot auto-configuration (v4)
@Configuration
@ComponentScan(basePackages = "com.example")
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

    // Manual bean: DataSource (HikariCP connection pool)
    // HikariCP is the fastest Java connection pool — also used by Spring Boot by default
    // In Spring Boot, this is auto-configured from application.properties
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

    // Manual bean: JdbcTemplate (Spring's JDBC helper)
    // In Spring Boot, this is auto-configured when spring-jdbc is on classpath
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    // Manual bean: TransactionManager (needed for @Transactional)
    // In Spring Boot, this is auto-configured when spring-jdbc is on classpath
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}
