package com.preethi.banking.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public String healthCheck() {
        return "Digital Banking Platform V1 is running";
    }
}