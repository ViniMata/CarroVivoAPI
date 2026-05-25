package com.carrovivo.api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// [SEC-34] SERVIÇO DE AUTENTICAÇÃO COM MÚLTIPLAS CAMADAS DE PROTEÇÃO
// Centraliza toda a lógica de autenticação: brute force, user enumeration e geração de token.
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // [SEC-35] BRUTE FORCE — MÁXIMO DE TENTATIVAS POR USERNAME
    // Após 5 tentativas falhas consecutivas, o username é bloqueado.
    // ConcurrentHashMap garante thread-safety em ambiente multi-threaded.
    private static final int MAX_ATTEMPTS = 5;
    private final Map<String, AtomicInteger> loginAttempts = new ConcurrentHashMap<>();

    public String login(String username, String password) {
        AtomicInteger attempts = loginAttempts.computeIfAbsent(username, k -> new AtomicInteger(0));

        // [SEC-36] VERIFICAÇÃO DE BLOQUEIO ANTES DA CONSULTA AO BANCO
        // Bloqueia imediatamente sem acessar o banco, economizando recursos.
        // Usa a mesma mensagem genérica para não revelar o motivo do bloqueio.
        if (attempts.get() >= MAX_ATTEMPTS) {
            throw new SecurityException("Credenciais inválidas");
        }

        // [SEC-37] QUERY ÚNICA — SEM USER ENUMERATION
        // Uma única consulta ao banco cujo Optional é reutilizado em todo o fluxo.
        // Se o usuário não existe, orElse(false) retorna false sem lançar exceção —
        // tornando a resposta idêntica à de senha incorreta.
        // Isso impede que atacantes descubram quais usernames existem no sistema.
        Optional<UserEntity> userOpt = userRepository.findByUsername(username);

        boolean valid = userOpt
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);

        if (!valid) {
            // [SEC-38] MESMA MENSAGEM SEMPRE — IMPEDE USER ENUMERATION
            // Não diferencia "usuário não existe" de "senha errada".
            // Ambos retornam 401 com "Credenciais inválidas".
            attempts.incrementAndGet();
            throw new SecurityException("Credenciais inválidas");
        }

        // [SEC-39] RESET DO CONTADOR EM LOGIN BEM-SUCEDIDO
        // Garante que o usuário não fique bloqueado após login correto.
        attempts.set(0);
        UserEntity user = userOpt.get();
        return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
    }

    public UserEntity register(String username, String password, Role requestedRole) {
        // [SEC-40] FALLBACK SEGURO DE ROLE
        // Se a role vier nula, atribui USER como padrão mais restritivo.
        // A proteção principal está no @PreAuthorize do AuthController,
        // mas esta é uma segunda camada de defesa.
        Role safeRole = (requestedRole != null) ? requestedRole : Role.USER;

        UserEntity user = UserEntity.builder()
                .username(username)
                // [SEC-41] SENHA ARMAZENADA COMO HASH BCRYPT
                // A senha nunca é armazenada em texto claro.
                // BCrypt gera salt automático e é resistente a rainbow tables.
                .password(passwordEncoder.encode(password))
                .role(safeRole)
                .build();
        return userRepository.save(user);
    }
}
