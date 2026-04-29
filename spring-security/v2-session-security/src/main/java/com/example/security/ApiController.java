package com.example.security;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    // PUBLIC API - no auth needed
    @GetMapping("/public/greeting")
    public Map<String, String> publicGreeting() {
        return Map.of("message", "Hello! This is a public API", "status", "open");
    }

    // PRIVATE API - requires authentication via session
    @GetMapping("/private/users")
    public Map<String, Object> privateUsers(Authentication auth) {
        return Map.of(
                "message", "Private data - you are authenticated!",
                "authenticatedUser", auth.getName(),
                "roles", auth.getAuthorities().toString(),
                "users", java.util.List.of(
                        Map.of("id", 1, "name", "Alice", "role", "ADMIN"),
                        Map.of("id", 2, "name", "Bob", "role", "USER"),
                        Map.of("id", 3, "name", "Charlie", "role", "USER")
                )
        );
    }
}
