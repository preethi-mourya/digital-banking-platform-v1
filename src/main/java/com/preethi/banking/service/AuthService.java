
package com.preethi.banking.service;

import com.preethi.banking.dto.LoginRequest;
import com.preethi.banking.dto.LoginResponse;
import com.preethi.banking.dto.RegisterRequest;
import com.preethi.banking.dto.RegisterResponse;
import com.preethi.banking.entity.AuditAction;
import com.preethi.banking.entity.Customer;
import com.preethi.banking.entity.CustomerStatus;
import com.preethi.banking.entity.Role;
import com.preethi.banking.entity.User;
import com.preethi.banking.repository.CustomerRepository;
import com.preethi.banking.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditLogService auditLogService;

    public AuthService(
            UserRepository userRepository,
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            AuditLogService auditLogService) {

        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setStatus(CustomerStatus.ACTIVE);

        Customer savedCustomer = customerRepository.save(customer);

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CUSTOMER);
        user.setEnabled(true);
        user.setCustomer(savedCustomer);

        User savedUser = userRepository.save(user);

        auditLogService.record(
                savedUser.getUsername(),
                AuditAction.REGISTER,
                "USER",
                String.valueOf(savedUser.getId()),
                "Customer registered successfully"
        );

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                "User registered successfully"
        );
    }

    public LoginResponse login(LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        User user = userRepository.findByUsername(
                userDetails.getUsername()
        ).orElseThrow(() ->
                new RuntimeException("User not found")
        );

        String token = jwtService.generateToken(
                user.getUsername(),
                user.getRole().name()
        );

        auditLogService.record(
                user.getUsername(),
                AuditAction.LOGIN,
                "USER",
                String.valueOf(user.getId()),
                "User logged in successfully"
        );

        return new LoginResponse(
                token,
                user.getUsername(),
                user.getRole().name(),
                "Login successful"
        );
    }
}
