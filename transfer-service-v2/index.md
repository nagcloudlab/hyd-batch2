# transfer-service-v2 — Spring Core (DI, AOP, Lifecycle)

Replace the manual assembler with Spring container. Same domain, zero boilerplate wiring.

---

## What This Project Teaches

- Spring IoC Container (`AnnotationConfigApplicationContext`)
- Java Configuration (`@Configuration`, `@Bean`, `@ComponentScan`)
- Dependency Injection (constructor, `@Qualifier`, `@Primary`)
- AOP — cross-cutting concerns without polluting business logic
- Bean Lifecycle (`@PostConstruct`, `@PreDestroy`, BFPP, BPP)
- Advanced: Events, SpEL, `@Conditional`, `FactoryBean`, `@Profile`, `@Lazy`, Scopes

---

## Spring Concepts Covered

| # | Concept | Key File(s) |
|---|---------|-------------|
| 1 | IoC Container | `TransferServiceApplication.java` |
| 2 | `@Configuration`, `@Bean` | `TransferServiceConfiguration.java` |
| 3 | `@ComponentScan`, Stereotypes | `@Service`, `@Repository` on impl classes |
| 4 | Constructor Injection | `TransferServiceImpl.java` |
| 5 | `@Qualifier`, `@Primary` | `JdbcAccountRepository`, `JpaAccountRepository` |
| 6 | `@Value`, `@PropertySource` | `TransferServiceConfiguration.java` |
| 7 | `Environment` | `TransferServiceConfiguration.java` (programmatic access) |
| 8 | `@Profile` | Ready to demo on repositories (dev/prod) |
| 9 | Bean Scopes | `@Scope("singleton")` on `JdbcAccountRepository` |
| 10 | `@Lazy` | `ApplicationCache.java` |
| 11 | `@PostConstruct`, `@PreDestroy` | `TransferServiceImpl.java` |
| 12 | BFPP | `BFPP.java` (modify bean definitions before creation) |
| 13 | BPP | `BPP.java` (inspect/wrap beans after creation) |
| 14 | Custom Annotation | `@NpciAnnotation` detected by BPP |
| 15 | AOP — `@Before` | `AuthAspect.java` |
| 16 | AOP — `@Around` | `TransactionAspect.java` |
| 17 | Proxy Pattern | `ProxyExample.java` (manual proxy vs Spring AOP) |
| 18 | Spring Events | `TransferCompletedEvent`, `TransferNotificationListener` |
| 19 | SpEL | `#{T(Runtime).getRuntime().availableProcessors()}` in config |
| 20 | `@Conditional` | `PostgresDriverCondition.java` (like `@ConditionalOnClass`) |
| 21 | `FactoryBean` | `DataSourceFactoryBean.java` (complex bean creation) |

---

## Project Structure

```
src/main/java/com/example/
    TransferServiceApplication.java          -- Spring container bootstrap
    config/
        TransferServiceConfiguration.java    -- @Configuration (beans, @Value, SpEL)
        PostgresDriverCondition.java         -- Custom @Conditional
        DataSourceFactoryBean.java           -- FactoryBean<DataSource>
        BFPP.java                            -- BeanFactoryPostProcessor
        BPP.java                             -- BeanPostProcessor
    model/
        Account.java                         -- Domain model
    repository/
        AccountRepository.java               -- Interface
        JdbcAccountRepository.java           -- @Repository, @Qualifier("jdbc")
        JpaAccountRepository.java            -- @Repository, @Primary
    service/
        TransferService.java                 -- Interface
        TransferServiceImpl.java             -- @Service, constructor DI, events
    aspect/
        AuthAspect.java                      -- @Before advice, @Order(1)
        TransactionAspect.java               -- @Around advice, @Order(2)
    annotation/
        NpciAnnotation.java                  -- Custom annotation
    event/
        TransferCompletedEvent.java          -- Custom ApplicationEvent
        TransferNotificationListener.java    -- @EventListener
    cache/
        ApplicationCache.java                -- @Lazy demo
    proxy/
        ProxyExample.java                    -- Manual proxy pattern demo
    exception/
        AccountNotFoundException.java
        InsufficientFundsException.java

src/main/resources/
    application-dev.properties               -- Dev profile (localhost)
    application-prod.properties              -- Prod profile (remote DB)
    simplelogger.properties                  -- SLF4J simple logger config
```

---

## How to Run

```bash
mvn clean compile exec:java -Dexec.mainClass="com.example.TransferServiceApplication"

# With profile:
mvn clean compile exec:java -Dexec.mainClass="com.example.TransferServiceApplication" -Dspring.profiles.active=prod
```

---

## v1 vs v2

| Aspect | v1 (Plain Java) | v2 (Spring Core) |
|--------|-----------------|------------------|
| Wiring | Manual in `main()` | `@ComponentScan` + `@Autowired` |
| Lifecycle | None | `@PostConstruct`, `@PreDestroy` |
| Cross-cutting | Manual proxy | `@Aspect` + `@Around` |
| Configuration | Hardcoded | `@Value` + `@PropertySource` |
| Bean creation | `new` keyword | Spring container |

---

## Key Takeaway

Spring automates everything we did manually in v1 — bean creation, wiring, lifecycle, cross-cutting concerns. But notice: we still manually configure `DataSource`, and repositories are stubs (no real DB). That's what v3 fixes.

---

## Prev: [transfer-service-v1](../transfer-service-v1/index.md) | Next: [transfer-service-v3](../transfer-service-v3/index.md)
