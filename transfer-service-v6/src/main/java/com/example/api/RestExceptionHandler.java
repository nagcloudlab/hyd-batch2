package com.example.api;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.exception.AccountNotFoundException;
import com.example.exception.InsufficientFundsException;

// @RestControllerAdvice = @ControllerAdvice + @ResponseBody
// Returns JSON error responses (not Thymeleaf views)
// Scoped to api package — does NOT interfere with MVC controllers or Swagger
//
// Compare with GlobalExceptionHandler (@ControllerAdvice) which returns view names
@RestControllerAdvice(basePackages = "com.example.api")
public class RestExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

    // 400 Bad Request — Bean Validation failed (@Valid on @RequestBody)
    // MethodArgumentNotValidException is thrown when @Valid finds errors
    // Extracts all field error messages into a structured response
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        logger.warn("Validation failed: {}", errors);

        ApiErrorResponse response = new ApiErrorResponse(
                400, "Bad Request", "Validation failed",
                java.time.LocalDateTime.now(), errors);

        return ResponseEntity.badRequest().body(response);
    }

    // 404 Not Found — account does not exist in database
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountNotFound(AccountNotFoundException ex) {
        logger.error("Account not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(404, "Not Found", ex.getMessage()));
    }

    // 400 Bad Request — insufficient funds for transfer
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficientFunds(InsufficientFundsException ex) {
        logger.error("Insufficient funds: {}", ex.getMessage());

        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(400, "Bad Request", ex.getMessage()));
    }

    // 400 Bad Request — invalid arguments (same account, exceeds limit, etc.)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        logger.error("Invalid request: {}", ex.getMessage());

        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(400, "Bad Request", ex.getMessage()));
    }

    // 500 Internal Server Error — catch-all for unexpected exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(500, "Internal Server Error",
                        "An unexpected error occurred"));
    }

}
