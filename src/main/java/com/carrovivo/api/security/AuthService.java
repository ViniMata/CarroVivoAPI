package com.carrovivo.api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private static final int MAX_ATTEMPTS = 5;
    private final Map<String, AtomicInteger> loginAttempts = new ConcurrentHashMap<>();

    public String login(String username, String password) {
        AtomicInteger attempts = loginAttempts.computeIfAbsent(username, k -> new AtomicInteger(0));

        if (attempts.get() >= MAX_ATTEMPTS) {
            throw new SecurityException("Credenciais inválidas");
        }

        Optional<UserEntity> userOpt = userRepository.findByUsername(username);

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