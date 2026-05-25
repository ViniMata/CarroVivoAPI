package com.carrovivo.api.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

// [SEC-54] AUDIT TRAIL — TRILHA DE AUDITORIA
// Registra todas as ações críticas: quem fez o quê, quando e de qual IP.
// Responde à pergunta de segurança fundamental: "quem fez isso?"
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository repository;

    // [SEC-55] LOG ESTRUTURADO SEM DADOS SENSÍVEIS
    // Registra ação, recurso, IP e status — nunca senhas, tokens ou payloads completos.
    // O log estruturado facilita busca e análise automatizada por ferramentas SIEM.
    public void log(String action, String resource, String resourceId,
                    String ipAddress, String status) {

        // [SEC-56] USERNAME EXTRAÍDO DO CONTEXTO DE SEGURANÇA
        // Não confia no username enviado pelo cliente — usa o do token JWT validado.
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

        // [SEC-57] PERSISTÊNCIA NO BANCO E LOG SIMULTÂNEOS
        // Garante que o registro existe tanto no banco (consultável)
        // quanto nos logs estruturados (rastreável em tempo real).
        repository.save(audit);
        log.info("[AUDIT] user={} action={} resource={} id={} ip={} status={}",
                username, action, resource, resourceId, ipAddress, status);
    }
}
