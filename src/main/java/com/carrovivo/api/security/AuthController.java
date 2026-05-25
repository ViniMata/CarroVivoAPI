package com.carrovivo.api.security;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

// [SEC-42] CONTROLLER DE AUTENTICAÇÃO
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticação e registro")
public class AuthController {

    private final AuthService authService;

    // [SEC-43] ENDPOINT DE LOGIN — ÚNICO ENDPOINT PÚBLICO DA API
    // @Valid ativa a validação do LoginRequest antes de qualquer processamento.
    @PostMapping("/login")
    @Operation(summary = "Login e geração de token JWT (30 min)")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(Map.of("token", token));
    }

    // [SEC-44] REGISTRO PROTEGIDO POR ROLE ADMIN
    // @PreAuthorize garante que só ADMIN pode criar novos usuários.
    // Sem esta annotation (e sem @EnableMethodSecurity no SecurityConfig),
    // qualquer pessoa poderia se registrar como ADMIN passando role=ADMIN no body.
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Registrar novo usuário — somente ADMIN")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request.getUsername(), request.getPassword(), request.getRole());
        return ResponseEntity.ok(Map.of("message", "Usuário registrado com sucesso"));
    }

    // [SEC-45] VALIDAÇÃO DE ENTRADA NO LOGIN
    // @NotBlank e @Size limitam o tamanho máximo, prevenindo
    // buffer overflow e flooding via campos gigantes.
    @Data
    public static class LoginRequest {
        @NotBlank(message = "Username é obrigatório")
        @Size(max = 100, message = "Username deve ter no máximo 100 caracteres")
        private String username;

        @NotBlank(message = "Password é obrigatório")
        @Size(max = 100, message = "Password deve ter no máximo 100 caracteres")
        private String password;
    }

    // [SEC-46] VALIDAÇÃO DE ENTRADA NO REGISTRO
    // Tamanho mínimo de 8 caracteres na senha como política básica de segurança.
    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Username é obrigatório")
        @Size(min = 3, max = 100, message = "Username deve ter entre 3 e 100 caracteres")
        private String username;

        @NotBlank(message = "Password é obrigatório")
        @Size(min = 8, max = 100, message = "Password deve ter entre 8 e 100 caracteres")
        private String password;

        private Role role;
    }
}
