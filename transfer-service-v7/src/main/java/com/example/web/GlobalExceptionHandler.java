package com.example.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.example.exception.AccountNotFoundException;
import com.example.exception.InsufficientFundsException;

// @ControllerAdvice — applies to ALL @Controller classes (centralized error handling)
// Each @ExceptionHandler method handles a specific exception type
// Returns a view name — Thymeleaf renders the error page with the error details
// @ResponseStatus — sets the HTTP status code on the response
//
// Without this, Spring Boot shows a generic Whitelabel Error Page
@ControllerAdvice(basePackages = "com.example.web")
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handles AccountNotFoundException — account not found in database
    // HTTP 404 — resource does not exist
    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleAccountNotFound(AccountNotFoundException ex, Model model) {
        logger.error("Account not found: {}", ex.getMessage());
        model.addAttribute("errorTitle", "Account Not Found");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorStatus", "404");
        return "error";
    }

    // Handles InsufficientFundsException — not enough balance for transfer
    // HTTP 400 — client made an invalid request (business rule violation)
    @ExceptionHandler(InsufficientFundsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInsufficientFunds(InsufficientFundsException ex, Model model) {
        logger.error("Insufficient funds: {}", ex.getMessage());
        model.addAttribute("errorTitle", "Insufficient Funds");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorStatus", "400");
        return "error";
    }

    // Handles IllegalArgumentException — invalid input that passed validation
    // HTTP 400 — bad request
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model) {
        logger.error("Invalid input: {}", ex.getMessage());
        model.addAttribute("errorTitle", "Invalid Request");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorStatus", "400");
        return "error";
    }

    // Catch-all for any other unexpected exceptions
    // HTTP 500 — server error
    // Never expose raw exception messages to users — log the detail, show a friendly message
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        model.addAttribute("errorTitle", "Something Went Wrong");
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again or contact support.");
        model.addAttribute("errorStatus", "500");
        return "error";
    }

}
