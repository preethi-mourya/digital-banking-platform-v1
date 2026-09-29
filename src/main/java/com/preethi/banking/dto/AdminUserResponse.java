package com.preethi.banking.dto;

import com.preethi.banking.entity.Role;

public class AdminUserResponse {

    private Long id;
    private String username;
    private String email;
    private Role role;
    private boolean enabled;

    public AdminUserResponse() {
    }

    public AdminUserResponse(
            Long id,
            String username,
            String email,
            Role role,
            boolean enabled) {

        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.enabled = enabled;
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

    public Role getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }
}