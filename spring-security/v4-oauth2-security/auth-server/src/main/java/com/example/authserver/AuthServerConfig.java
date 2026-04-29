package com.example.authserver;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

@Configuration
public class AuthServerConfig {

        // ===== Filter chain for OAuth2 Authorization Server endpoints =====
        @Bean
        @Order(1)
        public SecurityFilterChain authServerFilterChain(HttpSecurity http) throws Exception {
                OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
                http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                                .oidc(Customizer.withDefaults()); // Enable OpenID Connect
                http.exceptionHandling(ex -> ex
                                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
                        // Accept Bearer tokens for /userinfo endpoint
                        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
                return http.build();
        }

        // ===== Filter chain for login page =====
        @Bean
        @Order(2)
        public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(auth -> auth
                                                .anyRequest().authenticated())
                                .formLogin(Customizer.withDefaults());

                return http.build();
        }

        // ===== Registered OAuth2 Clients =====
        @Bean
        public RegisteredClientRepository registeredClientRepository(PasswordEncoder encoder) {

                // Client 1: Authorization Code flow (for browser-based client-app)
                RegisteredClient webClient = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("client-app")
                                .clientSecret(encoder.encode("secret"))
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                                .redirectUri("http://localhost:8080/login/oauth2/code/my-auth-server")
                                .scope(OidcScopes.OPENID)
                                .scope(OidcScopes.PROFILE)
                                .scope("read")
                                .scope("write")
                                .clientSettings(ClientSettings.builder()
                                                .requireAuthorizationConsent(true) // show consent screen
                                                .build())
                                .tokenSettings(TokenSettings.builder()
                                                .accessTokenTimeToLive(Duration.ofMinutes(30))
                                                .refreshTokenTimeToLive(Duration.ofHours(8))
                                                .build())
                                .build();

                // Client 2: Client Credentials flow (for service-to-service)
                RegisteredClient serviceClient = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("service-client")
                                .clientSecret(encoder.encode("service-secret"))
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                .scope("read")
                                .scope("write")
                                .tokenSettings(TokenSettings.builder()
                                                .accessTokenTimeToLive(Duration.ofMinutes(15))
                                                .build())
                                .build();

                return new InMemoryRegisteredClientRepository(webClient, serviceClient);
        }

        // ===== Users who can login at the Auth Server =====
        @Bean
        public UserDetailsService userDetailsService(PasswordEncoder encoder) {
                var user = User.builder()
                                .username("user")
                                .password(encoder.encode("password"))
                                .roles("USER")
                                .build();

                var admin = User.builder()
                                .username("admin")
                                .password(encoder.encode("password"))
                                .roles("ADMIN")
                                .build();

                return new InMemoryUserDetailsManager(user, admin);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        // ===== RSA Key Pair for signing JWT tokens =====
        @Bean
        public JWKSource<SecurityContext> jwkSource() {
                KeyPair keyPair = generateRsaKey();
                RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
                RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

                RSAKey rsaKey = new RSAKey.Builder(publicKey)
                                .privateKey(privateKey)
                                .keyID(UUID.randomUUID().toString())
                                .build();

                return new ImmutableJWKSet<>(new JWKSet(rsaKey));
        }

        private static KeyPair generateRsaKey() {
                try {
                        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                        generator.initialize(2048);
                        return generator.generateKeyPair();
                } catch (Exception e) {
                        throw new IllegalStateException(e);
                }
        }

        @Bean
        public AuthorizationServerSettings authorizationServerSettings() {
                return AuthorizationServerSettings.builder()
                                .issuer("http://localhost:9000")
                                .build();
        }
}
