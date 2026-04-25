# Spring Core Concepts — v2 (Student Handout)

This document covers all Spring Core concepts demonstrated in `transfer-service-v2`, in incremental order.

---

## 1. Why Spring? (Recap from v1)

- v1 showed tight coupling, factory pattern limitations, and manual DI
- Spring framework automates dependency injection and lifecycle management
- We move from manual assembler (v1 main class) to Spring container (v2)

---

## 2. Spring IoC Container

- `ApplicationContext` is the Spring container — creates, wires, and manages beans
- XML config: `ClassPathXmlApplicationContext("beans.xml")`
- Java config: `AnnotationConfigApplicationContext(AppConfig.class)`
- Container phases: Load bean definitions -> Create beans -> Wire dependencies -> Lifecycle callbacks

---

## 3. Java Configuration

- `@Configuration` — marks a class as source of bean definitions (replaces XML)
- `@Bean` — method-level, Spring calls it and manages the returned object
- `@ComponentScan(basePackages = "com.example")` — auto-detects @Component classes in given package

---

## 4. Stereotype Annotations

- `@Component` — generic Spring-managed bean
- `@Service` — specialization for business logic layer
- `@Repository` — specialization for data access layer (adds exception translation)
- All three register the class as a bean, but convey intent

---

## 5. Dependency Injection

### Constructor Injection (Recommended)
- Dependencies passed via constructor
- Spring auto-detects single constructor, `@Autowired` is optional
- Use `final` fields — ensures dependency is set and cannot be reassigned

### Setter Injection
- Dependencies set via setter method
- Annotate with `@Autowired`
- Use when dependency is optional

### Disambiguation
- `@Qualifier("jdbc")` — pick a specific bean when multiple candidates exist
- `@Primary` — mark one bean as the default choice

---

## 6. Externalized Configuration

### @Value
- `@Value("${key:default}")` — injects property value, uses default if key not found
- Used for datasource URL, username, password, pool size, etc.

### @PropertySource
- `@PropertySource("classpath:application.properties")` — loads external property file
- Add on `@Configuration` class
- Properties become available to `@Value` and `Environment`

### Environment Abstraction
- Inject `Environment` bean into any class
- `env.getProperty("spring.datasource.url")` — programmatic access to properties
- `env.getActiveProfiles()` — check which profiles are active
- Alternative to `@Value` when you need conditional logic around config

---

## 7. Bean Scopes

- `@Scope("singleton")` — (default) one instance per container, shared across all requests
- `@Scope("prototype")` — new instance every time bean is requested
- Demo: call `context.getBean()` twice, print `hashCode()` — same for singleton, different for prototype
- Web scopes (request, session) — covered later with Spring Web

---

## 8. @Lazy Initialization

- By default, Spring creates all singleton beans at startup (eager)
- `@Lazy` — delays bean creation until first access
- Demo: add `@Lazy` on a repository, show its constructor log does NOT appear at startup
- Useful for heavy beans that may not always be needed

---

## 9. @Profile

- `@Profile("dev")` — bean is only created when "dev" profile is active
- `@Profile("prod")` — bean is only created when "prod" profile is active
- Activate via: `-Dspring.profiles.active=dev` or `context.getEnvironment().setActiveProfiles("dev")`
- Demo: put `@Profile("dev")` on `JpaAccountRepository`, `@Profile("prod")` on `JdbcAccountRepository` — switch profiles to show different repo gets injected

---

## 10. Bean Lifecycle

### Callbacks
- `@PostConstruct` — called after DI is complete, use for initialization logic
- `@PreDestroy` — called before bean is removed, use for cleanup/resource release

### Container Extension Points
- `BeanFactoryPostProcessor (BFPP)` — runs BEFORE any bean is created, modifies bean definitions (metadata)
- `BeanPostProcessor (BPP)` — runs AFTER each bean is created, inspects or wraps beans

### Full Lifecycle Order
```
BFPP runs (modify bean definitions)
    -> Bean instantiated (constructor)
        -> Dependencies injected
            -> BPP.postProcessBeforeInitialization()
                -> @PostConstruct
                    -> BPP.postProcessAfterInitialization()
                        -> Bean is ready
                            ...
                        -> @PreDestroy
                            -> Bean destroyed
```

---

## 11. Custom Annotations

- Define with `@interface`, set `@Retention(RUNTIME)` for reflection access
- `BeanPostProcessor` can scan beans for custom annotations and add behavior
- Demo: `@NpciAnnotation` on `transfer()` method, detected by BPP during initialization
- Foundation for understanding how Spring's own annotations work internally

---

## 12. AOP (Aspect-Oriented Programming)

### Problem
- Cross-cutting concerns (auth, logging, transactions) get mixed into business logic
- Violates SRP — business methods shouldn't manage transactions

### Proxy Pattern (Manual)
- `ProxyExample` shows wrapping target with auth + logging manually
- Works but tedious — every method needs manual proxy code

### Spring AOP (Automatic)
- `@EnableAspectJAutoProxy` — tells Spring to create proxies automatically
- `@Aspect` — marks a class as an aspect
- `@Component` — aspect must also be a Spring bean

### Advice Types
- `@Before` — runs before method
- `@AfterReturning` — runs after successful return
- `@AfterThrowing` — runs after exception
- `@After` — runs always (like finally)
- `@Around` — wraps the method, controls execution (most powerful)

### Pointcut Expressions
- `execution(void com.example.service.TransferServiceImpl.transfer(..))` — match specific method
- `@Order(1)` — control aspect execution order (lower = first)

### How it Works
- At runtime, Spring creates a proxy around the target bean
- `context.getBean("transferService")` returns the proxy, not the actual object
- Proxy adds aspect behavior before/after delegating to the real method

---

## 13. Spring Events (Light Demo)

- `ApplicationEvent` — base class for custom events
- `ApplicationEventPublisher` — inject and call `publishEvent()` from any bean
- `@EventListener` — method-level annotation, listens for specific event type
- Demo: publish `TransferCompletedEvent` after transfer, listener logs "SMS notification sent"
- Achieves decoupled communication — publisher doesn't know about listeners

---

## 14. SpEL — Spring Expression Language (Light Demo)

- `@Value("#{2 * 5}")` — computed values
- `@Value("#{systemProperties['user.home']}")` — system properties
- `@Value("#{T(java.lang.Math).random()}")` — call static methods
- Used inside `@Value`, `@ConditionalOnExpression`, and SpEL templates

---

## 15. @Conditional (Light Demo)

- `@Conditional(SomeCondition.class)` — bean created only if condition is true
- `SomeCondition implements Condition` — override `matches()` method
- Demo: check if Postgres driver is on classpath before creating `dataSource` bean
- This is the foundation of Spring Boot auto-configuration (`@ConditionalOnClass`, `@ConditionalOnProperty`)

---

## 16. FactoryBean (Light Demo)

- `FactoryBean<T>` — interface for creating complex beans
- Implement `getObject()`, `getObjectType()`, `isSingleton()`
- Spring calls `getObject()` to produce the actual bean
- Demo: `DataSourceFactoryBean` that builds and returns a configured `HikariDataSource`
- Used internally by Spring for creating proxies, JPA EntityManagers, etc.

---

## Next: transfer-service-v3 (Spring JDBC)

After mastering these core concepts, we move to v3 where we:
- Use `JdbcTemplate` for real database operations
- Experience the pain of manual Spring configuration
- Set the stage for Spring Boot auto-configuration
