# Trainer Reference — Spring Core Live Demo (v2)

Guide to demonstrate 8 remaining Spring Core concepts using transfer-service-v2.
All demos are incremental — build on existing code, revert after each demo.

---

## 1. @PropertySource + Environment (~15 min)

### Setup

Create `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mydatabase
spring.datasource.username=postgres
spring.datasource.password=mysecretpassword
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.maximum-pool-size=5
```

### Demo

Add on `TransferServiceConfiguration`:

```java
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan(basePackages = "com.example")
@EnableAspectJAutoProxy
@PropertySource("classpath:application.properties")
public class TransferServiceConfiguration {
```

Add Environment injection + log method:

```java
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

private static final Logger logger = LoggerFactory.getLogger(TransferServiceConfiguration.class);

@Autowired
private Environment env;

@PostConstruct
public void logConfig() {
    logger.info("Active profiles: {}", java.util.Arrays.toString(env.getActiveProfiles()));
    logger.info("DataSource URL: {}", env.getProperty("spring.datasource.url"));
    logger.info("Max pool size: {}", env.getProperty("spring.datasource.maximum-pool-size"));
}
```

### Key Points

- `@Value("${key:default}")` — declarative, injected at startup
- `Environment.getProperty()` — programmatic, useful for conditional logic
- `@PropertySource` loads file into Spring Environment
- In Spring Boot, `application.properties` is auto-loaded — no @PropertySource needed

### Revert

- Remove `@PropertySource`, `Environment` field, `logConfig()` method
- Keep `application.properties` (used by next demo)

---

## 2. @Profile (~15 min)

### Setup

Create `src/main/resources/application-dev.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver
```

### Demo

Add `@Profile` on repositories:

```java
// JdbcAccountRepository.java
import org.springframework.context.annotation.Profile;

@Profile("prod")
@Qualifier("jdbc")
@Repository("jdbcAccountRepository")
public class JdbcAccountRepository implements AccountRepository {
```

```java
// JpaAccountRepository.java
import org.springframework.context.annotation.Profile;

@Profile("dev")
@Primary
@Qualifier("jpa")
@Repository("jpaAccountRepository")
public class JpaAccountRepository implements AccountRepository {
```

Run with profile:

```bash
# prod — JdbcAccountRepository gets injected
mvn compile exec:java -Dexec.mainClass="com.example.TransferServiceApplication" -Dspring.profiles.active=prod

# dev — JpaAccountRepository gets injected
mvn compile exec:java -Dexec.mainClass="com.example.TransferServiceApplication" -Dspring.profiles.active=dev
```

Programmatic alternative (in main, before context creation):

```java
AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
context.getEnvironment().setActiveProfiles("dev");
context.register(TransferServiceConfiguration.class);
context.refresh();
```

### Key Points

- Same code, different behavior per environment — no if/else
- Activate via: `-Dspring.profiles.active=dev` or programmatically
- Spring Boot adds: `application-{profile}.properties` auto-loading
- Real use cases: different databases, mock services, feature flags

### Revert

- Remove `@Profile` from both repositories
- Delete `application-dev.properties`

---

## 3. Bean Scopes (~10 min)

### Demo

Add in Run phase of `TransferServiceApplication.java`:

```java
// Bean Scopes demo
AccountRepository r1 = context.getBean("jdbcAccountRepository", AccountRepository.class);
AccountRepository r2 = context.getBean("jdbcAccountRepository", AccountRepository.class);
logger.info("Singleton — same instance? {} (r1={}, r2={})", r1 == r2, r1.hashCode(), r2.hashCode());
```

Run — shows `true`, same hashCode.

Now add `@Scope("prototype")` on `JdbcAccountRepository`:

```java
import org.springframework.context.annotation.Scope;

@Scope("prototype")
@Qualifier("jdbc")
@Repository("jdbcAccountRepository")
public class JdbcAccountRepository implements AccountRepository {
```

Run again — shows `false`, different hashCodes. Also notice constructor log prints multiple times.

### Key Points

- `singleton` (default) — one instance per container, shared everywhere
- `prototype` — new instance on every `getBean()` or injection
- Web scopes: `request`, `session` — covered with Spring Web later
- Singleton is preferred for stateless beans (services, repos)
- Prototype is rare — use for stateful beans that shouldn't be shared

### Revert

- Remove `@Scope("prototype")` from JdbcAccountRepository
- Remove scope demo code from main

---

## 4. @Lazy (~5 min)

### Demo

Add `@Lazy` on `JpaAccountRepository`:

```java
import org.springframework.context.annotation.Lazy;

@Lazy
@Primary
@Qualifier("jpa")
@Repository("jpaAccountRepository")
public class JpaAccountRepository implements AccountRepository {
```

Run — notice `"JpaAccountRepository instance created."` is MISSING from startup logs.

Add in Run phase of main:

```java
// @Lazy demo
logger.info("About to access JpaAccountRepository...");
AccountRepository lazyRepo = context.getBean("jpaAccountRepository", AccountRepository.class);
logger.info("JpaAccountRepository accessed: {}", lazyRepo.hashCode());
```

Run again — constructor log appears only when `getBean()` is called.

### Key Points

- Default is eager �� all singleton beans created at startup
- `@Lazy` — delays creation until first access
- Useful for heavy beans (external service clients, large caches)
- Spring Boot uses `@Lazy` internally for faster startup
- Can apply on class, method, or constructor parameter level

### Revert

- Remove `@Lazy` from JpaAccountRepository
- Remove lazy demo code from main

---

## 5. Spring Events (~15 min)

### Setup

Create 2 new files.

`src/main/java/com/example/event/TransferCompletedEvent.java`:

```java
package com.example.event;

import java.math.BigDecimal;
import org.springframework.context.ApplicationEvent;

public class TransferCompletedEvent extends ApplicationEvent {

    private final BigDecimal amount;
    private final String fromAccount;
    private final String toAccount;

    public TransferCompletedEvent(Object source, BigDecimal amount, String fromAccount, String toAccount) {
        super(source);
        this.amount = amount;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
    }

    public BigDecimal getAmount() { return amount; }
    public String getFromAccount() { return fromAccount; }
    public String getToAccount() { return toAccount; }
}
```

`src/main/java/com/example/event/NotificationListener.java`:

```java
package com.example.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificationListener.class);

    @EventListener
    public void onTransferCompleted(TransferCompletedEvent event) {
        logger.info(">>> SMS notification: ${} transferred from {} to {}",
                event.getAmount(), event.getFromAccount(), event.getToAccount());
    }
}
```

### Demo

In `TransferServiceImpl.java`, add publisher:

```java
import org.springframework.context.ApplicationEventPublisher;
import com.example.event.TransferCompletedEvent;

private final ApplicationEventPublisher eventPublisher;

// Update constructor
public TransferServiceImpl(@Qualifier("jdbc") AccountRepository accountRepository,
                           ApplicationEventPublisher eventPublisher) {
    this.accountRepository = accountRepository;
    this.eventPublisher = eventPublisher;
    ...
}
```

At end of `transfer()`, before success log:

```java
// Publish event — listeners react independently (decoupled)
eventPublisher.publishEvent(new TransferCompletedEvent(this, amount, fromAccountNumber, toAccountNumber));
```

Run — after each transfer:

```
Transfer of $300.00 from account 123 to account 456 completed successfully.
>>> SMS notification: $300.00 transferred from 123 to 456
```

### Key Points

- Publisher doesn't know about listeners — fully decoupled
- Add email listener, audit listener — no changes to service
- `@EventListener` — method-level, Spring routes by event type
- Alternative: implement `ApplicationListener<T>` interface
- Spring Boot uses events internally: `ApplicationReadyEvent`, `ContextRefreshedEvent`

### Revert

- Remove publisher field, constructor param, and publishEvent() from TransferServiceImpl
- Delete `event/` package (TransferCompletedEvent, NotificationListener)

---

## 6. SpEL — Spring Expression Language (~5 min)

### Demo

Add temporary fields in `TransferServiceConfiguration`:

```java
// SpEL — prefix is # not $
@Value("#{T(java.lang.Runtime).getRuntime().availableProcessors()}")
private int cpuCores;

@Value("#{systemProperties['user.home']}")
private String userHome;

@Value("#{${spring.datasource.maximum-pool-size} * 2}")
private int doublePoolSize;
```

Log them (add to `logConfig()` or a new `@PostConstruct` method):

```java
logger.info("SpEL — CPU cores: {}", cpuCores);
logger.info("SpEL — User home: {}", userHome);
logger.info("SpEL — Double pool size: {}", doublePoolSize);
```

### Key Points

- `${...}` — property placeholder (reads from properties file)
- `#{...}` — SpEL expression (computes values, calls methods)
- `#{${key} * 2}` — mix: read property, then apply SpEL
- `T(ClassName)` — access static methods/fields
- Used internally in: `@ConditionalOnExpression`, Spring Security expressions, Thymeleaf

### Revert

- Remove SpEL fields and log lines

---

## 7. @Conditional (~10 min)

### Setup

Create `src/main/java/com/example/config/PostgresCondition.java`:

```java
package com.example.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class PostgresCondition implements Condition {

    private static final Logger logger = LoggerFactory.getLogger(PostgresCondition.class);

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
```

### Demo

Add `@Conditional` on `dataSource()` in `TransferServiceConfiguration`:

```java
import org.springframework.context.annotation.Conditional;

@Bean
@Conditional(PostgresCondition.class)
public DataSource dataSource() {
```

Run — works, logs "condition TRUE".

Comment out Postgres dependency in `pom.xml`:

```xml
<!-- <dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.3</version>
</dependency> -->
```

Run — condition FALSE, bean not created, app fails.

### Key Points

- We just built what Spring Boot does automatically
- `@ConditionalOnClass` = checks if class is on classpath (our demo)
- `@ConditionalOnProperty` = checks if property is set
- `@ConditionalOnMissingBean` = creates only if no existing bean
- Hundreds of these conditions power Spring Boot auto-configuration
- This is the "magic" — now students understand it

### Revert

- Uncomment Postgres dependency in `pom.xml`
- Remove `@Conditional` from `dataSource()`
- Delete `PostgresCondition.java`

---

## 8. FactoryBean (~10 min)

### Setup

Create `src/main/java/com/example/config/DataSourceFactoryBean.java`:

```java
package com.example.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import com.zaxxer.hikari.HikariDataSource;

public class DataSourceFactoryBean implements FactoryBean<DataSource> {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceFactoryBean.class);

    private String url;
    private String username;
    private String password;

    public DataSourceFactoryBean(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public DataSource getObject() throws Exception {
        logger.info("FactoryBean — creating DataSource");
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setMaximumPoolSize(5);
        return ds;
    }

    @Override
    public Class<?> getObjectType() {
        return DataSource.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
```

### Demo

Comment out existing `dataSource()` bean in `TransferServiceConfiguration` and add:

```java
@Bean
public DataSourceFactoryBean dataSource() {
    return new DataSourceFactoryBean(jdbcUrl, username, password);
}
```

Add in Run phase of main:

```java
// FactoryBean demo — & prefix returns factory, without prefix returns product
Object factory = context.getBean("&dataSource");
Object product = context.getBean("dataSource");
logger.info("Factory class: {}", factory.getClass().getName());
logger.info("Product class: {}", product.getClass().getName());
```

Output:

```
FactoryBean — creating DataSource
Factory class: com.example.config.DataSourceFactoryBean
Product class: com.zaxxer.hikari.HikariDataSource
```

### Key Points

- Return a FactoryBean from @Bean, Spring gives consumers the product
- `getBean("name")` — returns the product (DataSource)
- `getBean("&name")` — `&` prefix returns the factory itself
- You rarely write FactoryBeans — but Spring uses them everywhere:
  - `LocalEntityManagerFactoryBean` (JPA)
  - `ProxyFactoryBean` (AOP)
  - `SqlSessionFactoryBean` (MyBatis)
- Understanding this explains "how Spring creates complex objects"

### Revert

- Restore original `dataSource()` @Bean method
- Remove FactoryBean demo code from main
- Delete `DataSourceFactoryBean.java`

---

## Demo Checklist

| # | Topic | Time | Revert After? |
|---|-------|------|---------------|
| 1 | @PropertySource + Environment | 15 min | Yes (keep application.properties) |
| 2 | @Profile | 15 min | Yes |
| 3 | Bean Scopes | 10 min | Yes |
| 4 | @Lazy | 5 min | Yes |
| 5 | Spring Events | 15 min | Yes |
| 6 | SpEL | 5 min | Yes |
| 7 | @Conditional | 10 min | Yes |
| 8 | FactoryBean | 10 min | Yes |

**Total: ~85 min**

After all demos, code should be back to its original state (last commit).
