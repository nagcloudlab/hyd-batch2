package com.example.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalErrorHandler {

    @ExceptionHandler(RuntimeException.class)
    public String handleServiceUnavailable(RuntimeException ex, Model model) {
        log.error("[ERROR-HANDLER] {}", ex.getMessage());
        model.addAttribute("errorTitle", "Service Unavailable");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }
}
