package com.preethi.banking.service;

import com.preethi.banking.dto.AdminUserResponse;
import com.preethi.banking.entity.Role;
import com.preethi.banking.entity.User;
import com.preethi.banking.exception.ResourceNotFoundException;
import com.preethi.banking.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<AdminUserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AdminUserResponse getUserById(Long id) {

        User user = getUser(id);

        return toResponse(user);
    }

    public AdminUserResponse updateUserRole(
            Long id,
            Role role) {

        User user = getUser(id);

        user.setRole(role);

        return toResponse(userRepository.save(user));
    }

    public AdminUserResponse enableUser(Long id) {

        User user = getUser(id);

        user.setEnabled(true);

        return toResponse(userRepository.save(user));
    }

    public AdminUserResponse disableUser(Long id) {

        User user = getUser(id);

        user.setEnabled(false);

        return toResponse(userRepository.save(user));
    }

    private User getUser(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id));
    }

    private AdminUserResponse toResponse(User user) {

        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled());
    }
}