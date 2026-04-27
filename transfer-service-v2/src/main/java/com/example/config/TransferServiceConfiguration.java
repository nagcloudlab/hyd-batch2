package com.example.config;

import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import com.zaxxer.hikari.HikariDataSource;

// @Configuration — marks this class as a source of bean definitions (replaces XML config)
// @ComponentScan — tells Spring to scan 'com.example' package for @Component, @Service, @Repository beans
// @EnableAspectJAutoProxy — enables AOP proxy creation for @Aspect beans
// @PropertySource — loads profile-specific properties into Spring Environment
@Configuration
@ComponentScan(basePackages = "com.example")
@EnableAspectJAutoProxy
@PropertySource("classpath:application-${spring.profiles.active:dev}.properties")
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

    // SpEL — #{...} expressions compute values at runtime (different from ${...}
    // property placeholders)
    @Value("#{T(java.lang.Runtime).getRuntime().availableProcessors()}")
    private int cpuCores;

    @Value("#{systemProperties['user.home']}")
    private String userHome;

    // Mix: read property with ${}, then apply SpEL with #{}
    @Value("#{${spring.datasource.maximum-pool-size} * 2}")
    private int doublePoolSize;

    // Environment — programmatic access to properties (alternative to @Value)
    @Autowired
    private Environment env;

    // @PostConstruct — called after DI is complete; logs resolved config for
    // verification
    @PostConstruct
    public void init() {
        logger.info("-".repeat(30));
        logger.info("Active profiles: {}", Arrays.toString(env.getActiveProfiles()));
        logger.info("DataSource URL: {}", env.getProperty("spring.datasource.url"));
        logger.info("Username: {}", env.getProperty("spring.datasource.username"));
        logger.info("Driver: {}", env.getProperty("spring.datasource.driver-class-name"));
        logger.info("Max pool size: {}", env.getProperty("spring.datasource.maximum-pool-size"));
        logger.info("SpEL — CPU cores: {}", cpuCores);
        logger.info("SpEL — User home: {}", userHome);
        logger.info("SpEL — Double pool size: {}", doublePoolSize);
        logger.info("-".repeat(30));
    }

    // @Bean — method-level annotation, Spring calls this and manages the returned
    // object
    // @Conditional — bean created only if PostgresDriverCondition.matches() returns
    // true
    @Bean
    @Conditional(PostgresDriverCondition.class)
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        dataSource.setMaximumPoolSize(maximumPoolSize);
        return dataSource;
    }

    // FactoryBean alternative — uncomment to demo FactoryBean producing the
    // DataSource
    // @Bean
    // public DataSourceFactoryBean dataSource() {
    // return new DataSourceFactoryBean(jdbcUrl, username, password);
    // }

}
