package com.example.config;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

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
@Configuration
@ComponentScan(basePackages = "com.example")
@EnableAspectJAutoProxy
@PropertySource("classpath:application-${spring.profiles.active:dev}.properties") // Load properties from
// application.properties
public class TransferServiceConfiguration {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
            .getLogger(TransferServiceConfiguration.class);

    // @Value — injects externalized properties with default values after ':'
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

    @Value("#{T(java.lang.Runtime).getRuntime().availableProcessors() ?: 4}") // Custom property with default value of 4
    private int numberOfCpus;

    @Value("#{systemProperties['user.home']}")
    private String userHome;

    @Value("#{${spring.datasource.maximum-pool-size} * 2}")
    private int doublePoolSize;

    @Value("#{fooComponent.foo ?: 'unknown'}") // Accessing property from another bean with default value 'unknown'
    private String fooCompProp;

    @Autowired
    private Environment env; // Alternative way to access properties, not used in this example

    @PostConstruct
    public void init() {
        logger.info("-".repeat(30));
        logger.info("jdbcUrl: {}", env.getProperty("spring.datasource.url"));
        logger.info("username: {}", env.getProperty("spring.datasource.username"));
        logger.info("password: {}", env.getProperty("spring.datasource.password"));
        logger.info("driverClassName: {}", env.getProperty("spring.datasource.driver-class-name"));
        logger.info("maximumPoolSize: {}", env.getProperty("spring.datasource.maximum-pool-size"));
        logger.info("Active profiles: {}", String.join(", ", env.getActiveProfiles()));
        logger.info("numberOfCpus: {}", numberOfCpus);
        logger.info("userHome: {}", userHome);
        logger.info("doublePoolSize: {}", doublePoolSize);
        logger.info("fooCompProp: {}", fooCompProp);
        logger.info("-".repeat(30));
    }

    // @Bean — method-level annotation, Spring calls this method and manages the
    // returned object as a bean
    // Bean name defaults to method name: "dataSource"
    // @Bean
    // @Conditional(PostgresDriverCondition.class) // Only create this bean if
    // // Postgres driver is present
    // public DataSource dataSource() {
    // HikariDataSource dataSource = new HikariDataSource();
    // dataSource.setJdbcUrl(jdbcUrl);
    // dataSource.setUsername(username);
    // dataSource.setPassword(password);
    // dataSource.setDriverClassName(driverClassName);
    // dataSource.setMaximumPoolSize(maximumPoolSize);
    // return dataSource;
    // }

    @Bean
    public DataSourceFactoryBean dataSource() {
        return new DataSourceFactoryBean(jdbcUrl, username, password);
    }

}
