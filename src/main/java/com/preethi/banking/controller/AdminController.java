package com.preethi.banking.controller;

import com.preethi.banking.dto.AdminUserResponse;
import com.preethi.banking.dto.UpdateRoleRequest;
import com.preethi.banking.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public List<AdminUserResponse> getAllUsers() {

        return adminService.getAllUsers();
    }

    @GetMapping("/users/{id}")
    public AdminUserResponse getUserById(
            @PathVariable Long id) {

        return adminService.getUserById(id);
    }

    @PutMapping("/users/{id}/role")
    public AdminUserResponse updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {

        return adminService.updateUserRole(
                id,
                request.getRole());
    }

    @PutMapping("/users/{id}/enable")
    public AdminUserResponse enableUser(
            @PathVariable Long id) {

        return adminService.enableUser(id);
    }

    @PutMapping("/users/{id}/disable")
    public AdminUserResponse disableUser(
            @PathVariable Long id) {

        return adminService.disableUser(id);
    }
}