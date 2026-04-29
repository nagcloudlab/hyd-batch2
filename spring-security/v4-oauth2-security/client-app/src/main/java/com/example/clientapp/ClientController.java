package com.example.clientapp;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Controller
public class ClientController {

    private final WebClient webClient;

    public ClientController(WebClient webClient) {
        this.webClient = webClient;
    }

    // ===== PUBLIC =====
    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    // ===== PROTECTED - requires OAuth2 login =====
    @GetMapping("/dashboard")
    public String dashboard(Model model,
            @AuthenticationPrincipal OidcUser oidcUser,
            @RegisteredOAuth2AuthorizedClient("my-auth-server") OAuth2AuthorizedClient authorizedClient) {

        // Show user info from the OAuth2 token
        model.addAttribute("username", oidcUser.getPreferredUsername() != null
                ? oidcUser.getPreferredUsername()
                : oidcUser.getSubject());
        model.addAttribute("claims", oidcUser.getClaims());
        model.addAttribute("accessToken", authorizedClient.getAccessToken().getTokenValue());

        return "dashboard";
    }

    // Call Resource Server's protected API using the OAuth2 token
    @GetMapping("/orders")
    public String orders(Model model, @AuthenticationPrincipal OidcUser oidcUser) {
        // WebClient automatically attaches the OAuth2 Bearer token
        Map<String, Object> response = webClient.get()
                .uri("/api/private/orders")
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        model.addAttribute("username", oidcUser.getSubject());
        model.addAttribute("orders", response);

        return "orders";
    }

    // Show token details from Resource Server
    @GetMapping("/token-info")
    public String tokenInfo(Model model) {
        Map<String, Object> response = webClient.get()
                .uri("/api/private/me")
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        model.addAttribute("tokenInfo", response);

        return "token-info";
    }
}
