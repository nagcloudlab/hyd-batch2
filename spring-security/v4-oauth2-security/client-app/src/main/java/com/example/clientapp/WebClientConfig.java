package com.example.clientapp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // WebClient that automatically attaches OAuth2 tokens to requests
    @Bean
    public WebClient webClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        var oauth2Filter = new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2Filter.setDefaultClientRegistrationId("my-auth-server");

        return WebClient.builder()
                .filter(oauth2Filter)
                .baseUrl("http://localhost:8081")   // Resource Server
                .build();
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepo,
            OAuth2AuthorizedClientRepository authorizedClientRepo) {

        var authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode()
                .refreshToken()
                .clientCredentials()
                .build();

        var clientManager = new DefaultOAuth2AuthorizedClientManager(
                clientRegistrationRepo, authorizedClientRepo);
        clientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return clientManager;
    }
}
