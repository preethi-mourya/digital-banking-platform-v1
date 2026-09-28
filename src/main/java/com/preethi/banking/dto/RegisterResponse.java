package com.preethi.banking.dto;

public class RegisterResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
    private String message;

    public RegisterResponse() {
    }

    public RegisterResponse(
            Long id,
            String username,
            String email,
            String role,
            String message) {

        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getMessage() {
        return message;
    }
}