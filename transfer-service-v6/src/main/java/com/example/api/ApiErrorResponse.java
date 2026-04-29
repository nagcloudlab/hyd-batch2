package com.example.api;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Structured error response — consistent JSON format for all API errors
// Clients can parse this reliably instead of dealing with random error formats
@Getter
@AllArgsConstructor
public class ApiErrorResponse {

    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
    private List<String> details;

    public ApiErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.details = null;
    }

}
