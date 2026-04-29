package com.example.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // enables Spring Security's web security support
public class SecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http

                                // 1. URL-based authorization rules
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/about", "/api/public/**").permitAll() // public
                                                .requestMatchers("/admin").hasRole("ADMIN") // admin only
                                                .requestMatchers("/dashboard", "/profile").hasAnyRole("USER", "ADMIN") // logged-in
                                                                                                                       // users
                                                .requestMatchers("/api/private/**").hasAnyRole("USER", "ADMIN") // logged-in
                                                                                                                // users
                                                .anyRequest().authenticated() // everything else
                                )

                                // 2. Form-based login (creates JSESSIONID cookie)
                                .formLogin(form -> form
                                                .loginPage("/login") // custom login page
                                                .defaultSuccessUrl("/dashboard", true)
                                                .permitAll())

                                // 3. Logout support
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .invalidateHttpSession(true) // destroy session
                                                .deleteCookies("JSESSIONID") // remove cookie
                                                .permitAll())

                                // 4. Access denied page
                                .exceptionHandling(ex -> ex
                                                .accessDeniedPage("/access-denied"));

                return http.build();
        }

        // In-memory users for demo
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
}
