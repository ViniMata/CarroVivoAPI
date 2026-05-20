package com.carrovivo.api.security;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Trilha de auditoria — somente ADMIN")
public class AuditController {

    private final AuditLogRepository repository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos os logs de auditoria")
    public ResponseEntity<List<AuditLog>> findAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Logs por usuário")
    public ResponseEntity<List<AuditLog>> findByUser(@PathVariable String username) {
        return ResponseEntity.ok(repository.findByUsername(username));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Logs por status")
    public ResponseEntity<List<AuditLog>> findByStatus(@PathVariable String status) {
        return ResponseEntity.ok(repository.findByStatus(status));
    }
}