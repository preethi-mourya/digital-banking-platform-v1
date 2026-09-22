package com.preethi.banking.service;

import org.springframework.stereotype.Service;

@Service
public class HealthService {

    public String getHealthStatus() {
        return "Digital Banking Platform V1 is running";
    }
}