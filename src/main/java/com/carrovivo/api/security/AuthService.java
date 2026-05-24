package com.carrovivo.api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// SERVIÇO DE AUTENTICAÇÃO COM MÚLTIPLAS CAMADAS DE PROTEÇÃO
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // BRUTE FORCE — MÁXIMO DE TENTATIVAS POR USERNAME
    // Após 5 tentativas falhas consecutivas, o username é bloqueado.
    // Usa ConcurrentHashMap para garantir thread-safety em ambiente multi-threaded.
    private static final int MAX_ATTEMPTS = 5;
    private final Map<String, AtomicInteger> loginAttempts = new ConcurrentHashMap<>();

    public String login(String username, String password) {
        AtomicInteger attempts = loginAttempts.computeIfAbsent(username, k -> new AtomicInteger(0));

        // VERIFICAÇÃO DE BLOQUEIO ANTES DA CONSULTA AO BANCO
        // Bloqueia sem ir ao banco, economizando recursos e
        // retornando a mesma mensagem genérica para não revelar motivo.
        if (attempts.get() >= MAX_ATTEMPTS) {
            throw new SecurityException("Credenciais inválidas");
        }

        // QUERY ÚNICA — SEM USER ENUMERATION
        // Uma única consulta ao banco cujo resultado é reutilizado.
        // Se o usuário não existe, orElse(false) retorna false sem exceção —
        // tornando a resposta idêntica à de senha incorreta.
        // Isso impede que atacantes descubram quais usernames existem.
        Optional<UserEntity> userOpt = userRepository.findByUsername(username);

        boolean valid = userOpt
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);

        if (!valid) {
            // INCREMENTO DO CONTADOR E MESMA MENSAGEM SEMPRE
            // Não diferencia "usuário não existe" de "senha errada".
            attempts.incrementAndGet();
            throw new SecurityException("Credenciais inválidas");
        }

        // RESET DO CONTADOR EM LOGIN BEM-SUCEDIDO
        attempts.set(0);
        UserEntity user = userOpt.get();
        return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
    }

    public UserEntity register(String username, String password, Role requestedRole) {
        // FALLBACK SEGURO DE ROLE
        // Se a role vier nula, atribui USER como padrão mais restritivo.
        // A proteção principal está no @PreAuthorize do AuthController,
        // mas esta é uma segunda camada de defesa.
        Role safeRole = (requestedRole != null) ? requestedRole : Role.USER;
        UserEntity user = UserEntity.builder()
                .username(username)
                // SENHA ARMAZENADA COMO HASH BCRYPT
                // A senha nunca é armazenada em texto claro.
                .password(passwordEncoder.encode(password))
                .role(safeRole)
                .build();
        return userRepository.save(user);
    }
}
