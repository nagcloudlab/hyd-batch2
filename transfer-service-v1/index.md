# transfer-service-v1 — Plain Java + SOLID Principles

No frameworks. No Spring. Just Java, interfaces, and good design.

---

## What This Project Teaches

- Why tight coupling is a problem (and how to feel it)
- Factory Pattern as a naive solution (and why it's not enough)
- Dependency Injection as the real solution (constructor injection)
- SOLID principles applied to a real domain (bank transfer)

---

## SOLID in Code

| Principle | Where |
|-----------|-------|
| **S** — Single Responsibility | `TransferServiceImpl` handles only business logic, `JdbcAccountRepository` handles only persistence |
| **O** — Open/Closed | `AccountRepositoryFactory` uses a registry map — add new types via `register()` without modifying existing code |
| **L** — Liskov Substitution | Swap `JdbcAccountRepository` with `JpaAccountRepository` — behavior stays correct |
| **I** — Interface Segregation | `AccountRepository` has only 2 methods: `findByNumber()`, `save()` — no bloat |
| **D** — Dependency Inversion | `TransferServiceImpl` depends on `AccountRepository` interface, not on any concrete class |

---

## Project Structure

```
src/main/java/com/example/
    TransferServiceApplication.java      -- Manual assembler (acts as container)
    model/
        Account.java                     -- Domain model (BigDecimal for money)
    repository/
        AccountRepository.java           -- Interface (DIP abstraction)
        JdbcAccountRepository.java       -- Stub impl using JDBC
        JpaAccountRepository.java        -- Stub impl using JPA
        AccountRepositoryFactory.java    -- Factory with registry pattern (OCP)
    service/
        TransferService.java             -- Interface (ISP)
        TransferServiceImpl.java         -- Business logic (SRP, DIP, constructor DI)
    exception/
        AccountNotFoundException.java    -- Custom unchecked exception
        InsufficientFundsException.java  -- Custom unchecked exception
```

---

## How to Run

```bash
mvn clean compile exec:java -Dexec.mainClass="com.example.TransferServiceApplication"
```

---

## Key Takeaway

The `main()` method is the assembler — it manually creates dependencies, wires them, and runs the app. This works for small apps, but becomes painful as the app grows. That's why we need a framework (Spring) to automate this.

---

## Next: [transfer-service-v2](../transfer-service-v2/index.md) (Spring Core)
