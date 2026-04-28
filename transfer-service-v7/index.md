# transfer-service-v6 — Spring Web (Thymeleaf MVC)

Server-side rendered web app with form handling, validation, and error handling.

---

## What This Project Teaches

- Spring MVC request flow (DispatcherServlet -> Controller -> ViewResolver -> Template)
- `@Controller` returning view names (vs `@RestController` returning data)
- Form handling with `@ModelAttribute` + Bean Validation (`@Valid`)
- PRG pattern (Post-Redirect-Get) with flash attributes
- `@PathVariable` for dynamic URLs
- `@ControllerAdvice` for centralized error handling
- Thymeleaf template engine expressions

---

## Spring Web Concepts

| # | Concept | Where |
|---|---------|-------|
| 1 | `@Controller` | `TransferController.java` — returns view names |
| 2 | `@GetMapping` / `@PostMapping` | `TransferController.java` — shorthand for `@RequestMapping` |
| 3 | `Model` | Controller -> template data passing |
| 4 | `@ModelAttribute` | Binds `TransferRequest` form object |
| 5 | `@Valid` + `BindingResult` | Bean Validation with error display |
| 6 | `@PathVariable` | `/accounts/{accountNumber}` URL extraction |
| 7 | PRG + `redirect:` | Redirect after POST to prevent duplicate submit |
| 8 | `RedirectAttributes` | Flash attributes survive the redirect |
| 9 | `@ControllerAdvice` | `GlobalExceptionHandler.java` — centralized errors |
| 10 | `@ExceptionHandler` | Per-exception-type error handling |

## Thymeleaf Concepts

| # | Expression | Purpose | Example |
|---|-----------|---------|---------|
| 1 | `th:text` | Render text safely | `th:text="${account.number}"` |
| 2 | `th:each` | Loop over collection | `th:each="txn : ${history}"` |
| 3 | `th:if` | Conditional render | `th:if="${#lists.isEmpty(accounts)}"` |
| 4 | `th:object` | Bind form to object | `th:object="${transferRequest}"` |
| 5 | `th:field` | Bind input to field | `th:field="*{amount}"` |
| 6 | `th:errors` | Show validation errors | `th:errors="*{fromAccount}"` |
| 7 | `th:action` | Form action URL | `th:action="@{/transfer}"` |
| 8 | `th:href` | Dynamic link | `th:href="@{/accounts/{id}(id=${account.number})}"` |
| 9 | `th:classappend` | Conditional CSS class | `th:classappend="${...} ? 'is-invalid'"` |
| 10 | `\|...\|` | Literal substitution | `th:text="\|$${account.balance}\|"` |

---

## Pages

| URL | Method | Description |
|-----|--------|-------------|
| `/` | GET | Landing page (static HTML) |
| `/transfer` | GET | Transfer form (Thymeleaf) |
| `/transfer` | POST | Process transfer (@Valid, PRG) |
| `/transfer-success` | GET | Success page (flash attributes) |
| `/accounts` | GET | Account list (th:each) |
| `/accounts/{id}` | GET | Account detail (@PathVariable) |
| `/transfer-history` | GET | Transaction history (th:each) |

---

## Project Structure

```
src/main/java/com/example/
    TransferServiceApplication.java          -- @SpringBootApplication
    web/
        TransferController.java              -- @Controller (all Spring Web concepts)
        TransferRequest.java                 -- Form DTO with @NotBlank, @DecimalMin
        GlobalExceptionHandler.java          -- @ControllerAdvice + @ExceptionHandler
    model/
        Account.java                         -- @Entity
        TxnHistory.java                      -- @Entity, @ManyToOne
        TransferType.java                    -- Enum (DEBIT, CREDIT)
    repository/
        AccountRepository.java               -- JpaRepository
        TxnHistoryRepository.java            -- JpaRepository
    service/
        TransferService.java                 -- Interface
        TransferServiceImpl.java             -- @Service, @Transactional
    exception/
        AccountNotFoundException.java
        InsufficientFundsException.java

src/main/resources/
    static/
        index.html                           -- Landing page (plain HTML, no Thymeleaf)
    templates/
        transfer-form.html                   -- th:object, th:field, th:errors
        transfer-success.html                -- Flash attributes, th:text
        account-list.html                    -- th:each, th:if, th:href
        account-detail.html                  -- @PathVariable data
        transfer-history.html                -- th:each, th:classappend
        error.html                           -- @ExceptionHandler error page
    application.properties
```

---

## How to Run

```bash
# Start Postgres (see v3 Notes.txt for Docker setup)
mvn spring-boot:run
# Open: http://localhost:8080
```

---

## Prev: transfer-service-v5 | Next: transfer-service-v7 (Spring REST API)
