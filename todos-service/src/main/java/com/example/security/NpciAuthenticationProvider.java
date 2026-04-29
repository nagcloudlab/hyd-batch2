package com.example.security;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class NpciAuthenticationProvider implements org.springframework.security.authentication.AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public NpciAuthenticationProvider(UserDetailsService userDetailsService,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public org.springframework.security.core.Authentication authenticate(
            org.springframework.security.core.Authentication authentication)
            throws org.springframework.security.core.AuthenticationException {

        // Extract username and password also is user active or not
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        org.springframework.security.core.userdetails.UserDetails userDetails = userDetailsService
                .loadUserByUsername(username);

        if (userDetails == null || !passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new org.springframework.security.authentication.BadCredentialsException(
                    "Invalid username or password");
        }

        boolean isActive = userDetails.isEnabled();
        if (!isActive) {
            throw new org.springframework.security.authentication.DisabledException("User account is disabled");
        }

        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userDetails.getUsername(),
                null,
                userDetails.getAuthorities());

    }

    @Override
    public boolean supports(Class<?> authentication) {
        // Specify the type of Authentication object this provider supports
        return authentication
                .equals(org.springframework.security.authentication.UsernamePasswordAuthenticationToken.class);
    }

}
