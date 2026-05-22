package com.carrovivo.api.security;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Trilha de auditoria — somente ADMIN")
public class AuditController {

    private final AuditLogRepository repository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar logs de auditoria (paginado, máx 50 por página)")
    public ResponseEntity<Page<AuditLog>> findAll(
            @PageableDefault(size = 50, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(repository.findAll(pageable));
    }

    @GetMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Logs por usuário (paginado)")
    public ResponseEntity<Page<AuditLog>> findByUser(
            @PathVariable String username,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(repository.findByUsername(username, pageable));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Logs por status (paginado)")
    public ResponseEntity<Page<AuditLog>> findByStatus(
            @PathVariable String status,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(repository.findByStatus(status, pageable));
    }
}
