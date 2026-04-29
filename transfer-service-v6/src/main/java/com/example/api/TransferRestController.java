package com.example.api;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.TxnHistory;
import com.example.repository.TxnHistoryRepository;
import com.example.service.TransferService;

import jakarta.validation.Valid;

// @RestController — returns JSON directly (no Thymeleaf)
// @RequestMapping("/api/v1/transfers") — RESTful resource naming (plural noun)
@RestController
@RequestMapping("/api/v1/transfers")
public class TransferRestController {

    private static final Logger logger = LoggerFactory.getLogger(TransferRestController.class);

    private final TransferService transferService;
    private final TxnHistoryRepository txnHistoryRepository;

    public TransferRestController(TransferService transferService,
                                  TxnHistoryRepository txnHistoryRepository) {
        this.transferService = transferService;
        this.txnHistoryRepository = txnHistoryRepository;
    }

    // =========================================================================
    // POST /api/v1/transfers — Execute a fund transfer
    // @RequestBody — deserializes JSON into TransferRequestDto
    // @Valid — triggers Bean Validation before method runs
    // Returns: 201 Created + TransferResponseDto
    //
    // POST is NOT idempotent — each call creates a new transfer!
    // Real-world APIs use X-Idempotency-Key header to prevent duplicates
    // =========================================================================
    @PostMapping
    public ResponseEntity<TransferResponseDto> executeTransfer(
            @Valid @RequestBody TransferRequestDto request) {
        logger.info("POST /api/v1/transfers — {} from {} to {}",
                request.getAmount(), request.getFromAccount(), request.getToAccount());

        transferService.transfer(
                request.getAmount(),
                request.getFromAccount(),
                request.getToAccount());

        TransferResponseDto response = new TransferResponseDto(
                "SUCCESS",
                request.getAmount(),
                request.getFromAccount(),
                request.getToAccount(),
                LocalDateTime.now());

        // 201 Created — new transfer resource created
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =========================================================================
    // GET /api/v1/transfers — List transfer history
    // @RequestParam — optional filtering by account number
    // Returns: 200 OK + JSON array of TxnHistoryDto
    // =========================================================================
    @GetMapping
    public ResponseEntity<List<TxnHistoryDto>> getTransferHistory(
            @RequestParam(required = false) String accountNumber) {
        logger.info("GET /api/v1/transfers (accountNumber={})", accountNumber);

        List<TxnHistory> history;
        if (accountNumber != null && !accountNumber.isBlank()) {
            history = txnHistoryRepository.findByAccountNumber(accountNumber);
        } else {
            history = txnHistoryRepository.findAll();
        }

        List<TxnHistoryDto> dtos = history.stream()
                .map(t -> new TxnHistoryDto(
                        t.getId(),
                        t.getAmount(),
                        t.getTransferType().name(),
                        t.getAccount().getNumber(),
                        t.getTimestamp()))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    // =========================================================================
    // GET /api/v1/transfers/{id} — Get specific transaction by ID
    // @PathVariable — extracts {id} from URL path
    // Returns: 200 OK + single TxnHistoryDto, or 404 Not Found
    // =========================================================================
    @GetMapping("/{id}")
    public ResponseEntity<TxnHistoryDto> getTransaction(@PathVariable Long id) {
        logger.info("GET /api/v1/transfers/{}", id);

        TxnHistory txn = txnHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));

        TxnHistoryDto dto = new TxnHistoryDto(
                txn.getId(),
                txn.getAmount(),
                txn.getTransferType().name(),
                txn.getAccount().getNumber(),
                txn.getTimestamp());

        return ResponseEntity.ok(dto);
    }

}
