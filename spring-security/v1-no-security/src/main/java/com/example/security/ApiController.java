package com.example.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    // PUBLIC API
    @GetMapping("/public/greeting")
    public Map<String, String> publicGreeting() {
        return Map.of("message", "Hello! This is a public API", "status", "open");
    }

    // PRIVATE API - should be protected but NO security yet!
    @GetMapping("/private/users")
    public Map<String, Object> privateUsers() {
        return Map.of(
                "message", "This is private user data - anyone can see it!",
                "users", java.util.List.of(
                        Map.of("id", 1, "name", "Alice", "role", "ADMIN"),
                        Map.of("id", 2, "name", "Bob", "role", "USER"),
                        Map.of("id", 3, "name", "Charlie", "role", "USER")
                )
        );
    }
}
