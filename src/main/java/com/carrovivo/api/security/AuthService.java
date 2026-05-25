package com.carrovivo.api.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private static final int MAX_ATTEMPTS = 5;
    private final Map<String, AtomicInteger> loginAttempts = new ConcurrentHashMap<>();

    public String login(String username, String password) {
        log.info("[DEBUG] encoder class: {}", passwordEncoder.getClass().getName());
        log.info("[DEBUG] username recebido: '{}'", username);
        log.info("[DEBUG] password recebido: '{}'", password);
        log.info("[DEBUG] password length: {}", password.length());

        // Testa BCrypt direto, ignorando o encoder injetado
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        String hash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lihO";
        boolean directMatch = bcrypt.matches(password, hash);
        log.info("[DEBUG] BCrypt direto (sem injeção): {}", directMatch);

        AtomicInteger attempts = loginAttempts.computeIfAbsent(username, k -> new AtomicInteger(0));

        if (attempts.get() >= MAX_ATTEMPTS) {
            throw new SecurityException("Credenciais inválidas");
        }

        Optional<UserEntity> userOpt = userRepository.findByUsername(username);
        log.info("[DEBUG] usuário encontrado: {}", userOpt.isPresent());

        if (userOpt.isPresent()) {
            String storedHash = userOpt.get().getPassword();
            log.info("[DEBUG] hash do banco length: {}", storedHash.length());
            log.info("[DEBUG] hash do banco: '{}'", storedHash);
            boolean injectedMatch = passwordEncoder.matches(password, storedHash);
            boolean directMatchBank = bcrypt.matches(password, storedHash);
            log.info("[DEBUG] matches com encoder injetado: {}", injectedMatch);
            log.info("[DEBUG] matches com BCrypt direto + hash do banco: {}", directMatchBank);
        }

        boolean valid = userOpt
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);

        if (!valid) {
            attempts.incrementAndGet();
            throw new SecurityException("Credenciais inválidas");
        }

        attempts.set(0);
        UserEntity user = userOpt.get();
        return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
    }

    public UserEntity register(String username, String password, Role requestedRole) {
        Role safeRole = (requestedRole != null) ? requestedRole : Role.USER;
        UserEntity user = UserEntity.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(safeRole)
                .build();
        return userRepository.save(user);
    }
}
