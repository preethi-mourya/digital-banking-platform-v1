
package com.preethi.banking.service;

import com.preethi.banking.dto.AdminUserResponse;
import com.preethi.banking.entity.AuditAction;
import com.preethi.banking.entity.Role;
import com.preethi.banking.entity.User;
import com.preethi.banking.exception.ResourceNotFoundException;
import com.preethi.banking.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public AdminService(
            UserRepository userRepository,
            AuditLogService auditLogService) {

        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(Long id) {
        return toResponse(getUser(id));
    }

    @Transactional
    public AdminUserResponse updateUserRole(Long id, Role role) {

        if (role == null) {
            throw new IllegalArgumentException("Role is required");
        }

        User user = getUser(id);
        Role previousRole = user.getRole();

        user.setRole(role);

        User savedUser = userRepository.save(user);

        auditLogService.record(
                currentUsername(),
                AuditAction.ADMIN_ROLE_UPDATED,
                "USER",
                String.valueOf(savedUser.getId()),
                "User role changed from " + previousRole
                        + " to " + role
        );

        return toResponse(savedUser);
    }

    @Transactional
    public AdminUserResponse enableUser(Long id) {

        User user = getUser(id);

        if (!user.isEnabled()) {
            user.setEnabled(true);
            User savedUser = userRepository.save(user);

            auditLogService.record(
                    currentUsername(),
                    AuditAction.USER_ENABLED,
                    "USER",
                    String.valueOf(savedUser.getId()),
                    "User account enabled"
            );

            return toResponse(savedUser);
        }

        return toResponse(user);
    }

    @Transactional
    public AdminUserResponse disableUser(Long id) {

        User user = getUser(id);

        if (user.isEnabled()) {
            user.setEnabled(false);
            User savedUser = userRepository.save(user);

            auditLogService.record(
                    currentUsername(),
                    AuditAction.USER_DISABLED,
                    "USER",
                    String.valueOf(savedUser.getId()),
                    "User account disabled"
            );

            return toResponse(savedUser);
        }

        return toResponse(user);
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id));
    }

    private String currentUsername() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new IllegalStateException(
                    "Authenticated user is required for audit logging");
        }

        return authentication.getName();
    }

    private AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled()
        );
    }
}
