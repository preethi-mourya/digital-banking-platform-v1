
package com.preethi.banking.controller;

import com.preethi.banking.entity.AuditAction;
import com.preethi.banking.entity.AuditLog;
import com.preethi.banking.service.AuditLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditLogService auditLogService;

    public AuditController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<AuditLog> getAllAuditLogs() {
        return auditLogService.getAllAuditLogs();
    }

    @GetMapping("/user/{username}")
    public List<AuditLog> getUserAuditLogs(
            @PathVariable String username) {
        return auditLogService.getUserAuditLogs(username);
    }

    @GetMapping("/action/{action}")
    public List<AuditLog> getAuditLogsByAction(
            @PathVariable AuditAction action) {
        return auditLogService.getAuditLogsByAction(action);
    }
}
