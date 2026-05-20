package com.carrovivo.api.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository repository;

    public void log(String action, String resource, String resourceId,
                    String ipAddress, String status) {

        String username = "anonymous";
        try {
            username = SecurityContextHolder.getContext()
                    .getAuthentication().getName();
        } catch (Exception ignored) {}

        AuditLog audit = AuditLog.builder()
                .username(username)
                .action(action)
                .resource(resource)
                .resourceId(resourceId)
                .ipAddress(ipAddress)
                .status(status)
                .build();

        repository.save(audit);

        // Log estruturado sem dados sensíveis
        log.info("[AUDIT] user={} action={} resource={} id={} ip={} status={}",
                username, action, resource, resourceId, ipAddress, status);
    }
}