package com.example.security.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    // PUBLIC - no token needed
    @GetMapping("/public/greeting")
    public Map<String, String> publicGreeting() {
        return Map.of("message", "Hello! This is a public API - no JWT needed");
    }

    // PRIVATE - requires valid JWT with USER or ADMIN role
    @GetMapping("/private/users")
    public Map<String, Object> privateUsers(Authentication auth) {
        return Map.of(
                "message", "You accessed private data with JWT!",
                "authenticatedUser", auth.getName(),
                "roles", auth.getAuthorities().toString(),
                "users", List.of(
                        Map.of("id", 1, "name", "Alice", "role", "ADMIN"),
                        Map.of("id", 2, "name", "Bob", "role", "USER"),
                        Map.of("id", 3, "name", "Charlie", "role", "USER")
                )
        );
    }

    // ADMIN ONLY - requires valid JWT with ADMIN role
    @GetMapping("/private/admin/settings")
    public Map<String, Object> adminSettings(Authentication auth) {
        return Map.of(
                "message", "Admin-only data accessed with JWT!",
                "authenticatedUser", auth.getName(),
                "dbPassword", "super-secret-123",
                "serverConfig", Map.of("maxConnections", 100, "timeout", 30)
        );
    }
}
