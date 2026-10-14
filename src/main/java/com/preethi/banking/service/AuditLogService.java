package com.preethi.banking.service;

import com.preethi.banking.entity.AuditAction;
import com.preethi.banking.entity.AuditLog;
import com.preethi.banking.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public AuditLog record(
            String username,
            AuditAction action,
            String entityType,
            String entityId,
            String description) {

        AuditLog auditLog = new AuditLog(
                username,
                action,
                entityType,
                entityId,
                description
        );

        return auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<AuditLog> getUserAuditLogs(String username) {
        return auditLogRepository
                .findByUsernameOrderByCreatedAtDesc(username);
    }

    public List<AuditLog> getAuditLogsByAction(AuditAction action) {
        return auditLogRepository
                .findByActionOrderByCreatedAtDesc(action);
    }
}