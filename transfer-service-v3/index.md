# transfer-service-v3 — Spring JDBC (Manual Config, Transactions, ACID)

Real database operations with JdbcTemplate. Full ACID demo with concurrent transfers.

---

## What This Project Teaches

- Spring `JdbcTemplate` for real database CRUD operations
- `@Transactional` for declarative transaction management
- ACID properties demonstrated with real Postgres
- The pain of manual Spring configuration (DataSource, JdbcTemplate, TransactionManager)
- Why Spring Boot auto-configuration exists (motivation for v4)

---

## Key Concepts

| Concept | Where |
|---------|-------|
| `JdbcTemplate` | `JdbcAccountRepository.java` — `queryForObject()`, `update()` |
| `@Transactional` | `TransferServiceImpl.java` — rollback, isolation level |
| `DataSource` bean | `TransferServiceConfiguration.java` — manual HikariCP setup |
| `JdbcTemplate` bean | `TransferServiceConfiguration.java` — manual bean |
| `TransactionManager` bean | `TransferServiceConfiguration.java` — manual bean |
| `@EnableTransactionManagement` | `TransferServiceConfiguration.java` |
| Concurrent transfers | `TransferServiceApplication.java` — two threads |

---

## ACID Demo

| Property | How to Demo |
|----------|-------------|
| **Atomicity** | Set `simulateError = true` in `TransferServiceImpl` — debit happens, credit fails, entire transaction rolls back |
| **Consistency** | Transfer validates balance before debit — `InsufficientFundsException` prevents negative balances |
| **Isolation** | Two threads transfer concurrently — `READ_COMMITTED` isolation prevents dirty reads |
| **Durability** | Run a transfer, restart Postgres container, query the table — data persists |

---

## Project Structure

```
src/main/java/com/example/
    TransferServiceApplication.java          -- Bootstrap + concurrent transfer demo
    config/
        TransferServiceConfiguration.java    -- Manual config (DataSource, JdbcTemplate, TxManager)
    model/
        Account.java                         -- Domain model
    repository/
        AccountRepository.java               -- Interface
        JdbcAccountRepository.java           -- Real DB operations with JdbcTemplate
    service/
        TransferService.java                 -- Interface
        TransferServiceImpl.java             -- @Transactional, ACID, simulateError flag
    exception/
        AccountNotFoundException.java
        InsufficientFundsException.java

src/main/resources/
    application.properties                   -- DB connection config
    simplelogger.properties                  -- Logger config
```

---

## How to Run

### 1. Start Postgres

```bash
docker run \
  --name postgres-container \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=mysecretpassword \
  -e POSTGRES_DB=mydatabase \
  -p 5432:5432 \
  -d postgres:latest
```

### 2. Create Table and Seed Data

```bash
docker exec -it postgres-container psql -U postgres -d mydatabase
```

```sql
CREATE TABLE accounts (
    number VARCHAR(255) PRIMARY KEY,
    balance DECIMAL(10, 2) NOT NULL
);

INSERT INTO accounts (number, balance) VALUES ('123', 1000.00);
INSERT INTO accounts (number, balance) VALUES ('456', 500.00);
INSERT INTO accounts (number, balance) VALUES ('789', 2000.00);
INSERT INTO accounts (number, balance) VALUES ('012', 750.00);
```

### 3. Run Application

```bash
mvn clean compile exec:java -Dexec.mainClass="com.example.TransferServiceApplication"
```

---

## v2 vs v3

| Aspect | v2 (Spring Core) | v3 (Spring JDBC) |
|--------|------------------|-------------------|
| Repository | Stub (returns hardcoded data) | Real DB with `JdbcTemplate` |
| Transactions | Simulated by `TransactionAspect` | Real `@Transactional` with `DataSourceTransactionManager` |
| DataSource | Used only for demo (connection test) | Real HikariCP connection pool |
| Config | `@PropertySource`, `@Value` | Same + `JdbcTemplate` bean + `TransactionManager` bean |
| Concurrency | Single-threaded | Multi-threaded ACID demo |

---

## Pain Points (Why Spring Boot?)

```java
// 3 manual beans just to talk to a database:
@Bean public DataSource dataSource() { ... }
@Bean public JdbcTemplate jdbcTemplate() { ... }
@Bean public PlatformTransactionManager transactionManager() { ... }

// Plus these annotations on config class:
@EnableTransactionManagement
@PropertySource("classpath:application.properties")
```

In Spring Boot (v4), ALL of this is auto-configured. You just add `spring-boot-starter-jdbc` and `application.properties`.

---

## Prev: [transfer-service-v2](../transfer-service-v2/index.md) | Next: transfer-service-v4 (Spring Boot)
