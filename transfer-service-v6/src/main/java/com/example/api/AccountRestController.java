package com.example.api;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.exception.AccountNotFoundException;
import com.example.model.Account;
import com.example.repository.AccountRepository;

import jakarta.validation.Valid;

// @RestController = @Controller + @ResponseBody
// Every method returns data (JSON) directly, not a view name
// Compare with @Controller in TransferController (returns Thymeleaf view names)
//
// @RequestMapping("/api/v1/accounts") — base path for all endpoints in this controller
// API versioning via URL path (/v1/) — most common strategy
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountRestController {

    private static final Logger logger = LoggerFactory.getLogger(AccountRestController.class);

    private final AccountRepository accountRepository;

    public AccountRestController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // =========================================================================
    // GET /api/v1/accounts — List all accounts
    // @RequestParam — optional query parameter for filtering
    // ResponseEntity<T> — full control over status code, headers, and body
    // Returns: 200 OK + JSON array
    // =========================================================================
    @GetMapping
    public ResponseEntity<List<AccountDto>> getAllAccounts(
            @RequestParam(required = false) String sort) {
        logger.info("GET /api/v1/accounts (sort={})", sort);

        List<AccountDto> accounts = accountRepository.findAll().stream()
                .map(a -> new AccountDto(a.getNumber(), a.getBalance()))
                .toList();

        return ResponseEntity.ok(accounts);
    }

    // =========================================================================
    // GET /api/v1/accounts/{number} — Get single account
    // @PathVariable — extracts {number} from URL path
    // Returns: 200 OK + JSON object, or 404 Not Found
    // =========================================================================
    @GetMapping("/{number}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable String number) {
        logger.info("GET /api/v1/accounts/{}", number);

        Account account = accountRepository.findById(number)
                .orElseThrow(() -> new AccountNotFoundException(number));

        return ResponseEntity.ok(new AccountDto(account.getNumber(), account.getBalance()));
    }

    // =========================================================================
    // POST /api/v1/accounts — Create new account
    // @RequestBody — deserializes JSON body into AccountDto
    // @Valid — triggers Bean Validation (@NotBlank, @NotNull, @DecimalMin)
    // Returns: 201 Created + Location header + JSON body
    // =========================================================================
    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody AccountDto dto) {
        logger.info("POST /api/v1/accounts — creating {}", dto.getNumber());

        if (accountRepository.existsById(dto.getNumber())) {
            throw new IllegalArgumentException("Account already exists: " + dto.getNumber());
        }

        Account account = new Account(dto.getNumber(), dto.getBalance());
        accountRepository.save(account);

        // 201 Created with Location header pointing to the new resource
        // Location: /api/v1/accounts/123
        return ResponseEntity
                .created(URI.create("/api/v1/accounts/" + account.getNumber()))
                .body(new AccountDto(account.getNumber(), account.getBalance()));
    }

    // =========================================================================
    // PUT /api/v1/accounts/{number} — Full replacement update
    // PUT is idempotent — calling it N times produces the same result
    // Requires ALL fields in the body (replaces entire resource)
    // Returns: 200 OK + updated JSON
    // =========================================================================
    @PutMapping("/{number}")
    public ResponseEntity<AccountDto> updateAccount(
            @PathVariable String number,
            @Valid @RequestBody AccountDto dto) {
        logger.info("PUT /api/v1/accounts/{}", number);

        Account account = accountRepository.findById(number)
                .orElseThrow(() -> new AccountNotFoundException(number));

        account.setBalance(dto.getBalance());
        accountRepository.save(account);

        return ResponseEntity.ok(new AccountDto(account.getNumber(), account.getBalance()));
    }

    // =========================================================================
    // PATCH /api/v1/accounts/{number} — Partial update
    // Only updates fields that are provided in the body (non-null fields)
    // Difference from PUT: PATCH sends only changed fields, PUT sends everything
    // Returns: 200 OK + updated JSON
    // =========================================================================
    @PatchMapping("/{number}")
    public ResponseEntity<AccountDto> patchAccount(
            @PathVariable String number,
            @RequestBody AccountDto dto) {
        logger.info("PATCH /api/v1/accounts/{}", number);

        Account account = accountRepository.findById(number)
                .orElseThrow(() -> new AccountNotFoundException(number));

        // Only update fields that are provided (not null)
        if (dto.getBalance() != null) {
            account.setBalance(dto.getBalance());
        }
        accountRepository.save(account);

        return ResponseEntity.ok(new AccountDto(account.getNumber(), account.getBalance()));
    }

    // =========================================================================
    // DELETE /api/v1/accounts/{number} — Delete account
    // DELETE is idempotent — deleting an already-deleted resource is still "success"
    // Returns: 204 No Content (success, but no body to return)
    // =========================================================================
    @DeleteMapping("/{number}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String number) {
        logger.info("DELETE /api/v1/accounts/{}", number);

        if (!accountRepository.existsById(number)) {
            throw new AccountNotFoundException(number);
        }

        accountRepository.deleteById(number);

        // 204 No Content — standard response for successful DELETE
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // HEAD /api/v1/accounts/{number} — Check if account exists
    // Same as GET but returns only headers (no body)
    // Useful for checking existence without downloading data
    // Returns: 200 OK (exists) or 404 Not Found
    // =========================================================================
    // Spring automatically supports HEAD for any @GetMapping

}
