package com.example.api;

import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.AuthRequest;
import com.example.dto.AuthResponse;
import com.example.dto.CreateUserDto;
import com.example.service.UserService;

@RestController
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final com.example.util.JwtUtil jwtUtil;

    public UserController(UserService userService, AuthenticationManager authenticationManager,
            com.example.util.JwtUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    // Add endpoints for user registration and login here

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public void registerUser(@RequestBody CreateUserDto createUserDto) {
        userService.register(createUserDto);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public AuthResponse loginUser(@RequestBody AuthRequest authRequest) {
        // Implement login logic here
        String username = authRequest.getUsername();
        String password = authRequest.getPassword();
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);
        try {
            authToken = (UsernamePasswordAuthenticationToken) authenticationManager.authenticate(authToken);
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            throw new RuntimeException("Invalid username or password");
        }
        List<String> roles = authToken.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .toList();
        String jwtToken = jwtUtil.generateToken(username, roles);
        return new AuthResponse(jwtToken);
    }

}
