package com.example.resourceserver;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ResourceController {

    // PUBLIC - no token needed
    @GetMapping("/public/status")
    public Map<String, String> status() {
        return Map.of(
                "service", "Resource Server",
                "status", "UP",
                "message", "This endpoint is public - no OAuth2 token needed"
        );
    }

    // PRIVATE - requires valid OAuth2 token with 'read' scope
    @GetMapping("/private/orders")
    public Map<String, Object> getOrders(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "message", "You accessed protected data via OAuth2!",
                "tokenSubject", jwt.getSubject(),
                "tokenScopes", jwt.getClaimAsString("scope"),
                "issuedBy", jwt.getIssuer().toString(),
                "orders", List.of(
                        Map.of("id", 101, "item", "Laptop", "amount", 75000),
                        Map.of("id", 102, "item", "Phone", "amount", 25000),
                        Map.of("id", 103, "item", "Headphones", "amount", 3000)
                )
        );
    }

    // PRIVATE - user info from token
    @GetMapping("/private/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "subject", jwt.getSubject(),
                "scopes", jwt.getClaimAsString("scope"),
                "issuer", jwt.getIssuer().toString(),
                "issuedAt", jwt.getIssuedAt().toString(),
                "expiresAt", jwt.getExpiresAt().toString(),
                "allClaims", jwt.getClaims()
        );
    }
}
