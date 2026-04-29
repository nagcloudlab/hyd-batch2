package com.example.dto;

public class CreateUserDto {
    private final String username;
    private final String email;
    private final String password;

    public CreateUserDto(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
