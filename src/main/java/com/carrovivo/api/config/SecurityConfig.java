package com.carrovivo.api.config;

import com.carrovivo.api.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

// [SEC-01] CONFIGURAÇÃO CENTRAL DE SEGURANÇA
// Ponto único onde todas as regras de autenticação, autorização,
// CORS e sessão são definidas para toda a aplicação.
@Configuration
@RequiredArgsConstructor
// [SEC-02] HABILITA @PreAuthorize NOS CONTROLLERS
// Sem esta annotation, o @PreAuthorize é completamente ignorado
// pelo Spring e qualquer usuário poderia acessar endpoints protegidos.
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // [SEC-03] CSRF DESABILITADO — API STATELESS
                // APIs REST com JWT não usam cookies de sessão,
                // portanto CSRF não se aplica aqui.
                .csrf(AbstractHttpConfigurer::disable)

                // [SEC-04] CORS COM ORIGENS EXPLÍCITAS
                // Nunca usar wildcard (*). Apenas origens conhecidas
                // podem fazer requisições cross-origin para esta API.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // [SEC-05] SESSÃO STATELESS
                // Sem estado de sessão no servidor. Cada requisição
                // deve apresentar seu próprio token JWT.
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // [SEC-06] ÚNICO ENDPOINT PÚBLICO
                        // Apenas o login é acessível sem autenticação.
                        // O /register exige ADMIN via @PreAuthorize no AuthController.
                        .requestMatchers("/api/auth/login").permitAll()

                        // [SEC-07] SWAGGER PROTEGIDO POR ROLE ADMIN
                        // A documentação da API não é pública. Apenas administradores
                        // podem visualizar os endpoints, evitando exposição da estrutura interna.
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                "/api-docs/**", "/v3/api-docs/**", "/webjars/**").hasRole("ADMIN")

                        // [SEC-08] RBAC — CONTROLE DE ACESSO BASEADO EM PAPÉIS
                        // Cada recurso tem seu conjunto de roles permitidas.
                        // ADMIN > ANALYST > USER em hierarquia de privilégios.
                        .requestMatchers("/api/vehicles/**").hasAnyRole("ADMIN", "ANALYST", "USER")
                        .requestMatchers("/api/maintenances/**").hasAnyRole("ADMIN", "ANALYST")
                        .requestMatchers("/api/diagnostics/**").hasAnyRole("ADMIN", "ANALYST")
                        .requestMatchers("/api/warranties/**").hasAnyRole("ADMIN", "ANALYST")
                        .requestMatchers("/api/notifications/**").hasAnyRole("ADMIN", "ANALYST", "USER")
                        .requestMatchers("/api/dealers/**").hasAnyRole("ADMIN", "ANALYST", "USER")

                        // [SEC-09] FALLBACK: QUALQUER ROTA NÃO MAPEADA EXIGE ADMIN
                        .anyRequest().hasRole("ADMIN")
                )
                // [SEC-10] FILTRO JWT ANTES DO FILTRO DE AUTENTICAÇÃO PADRÃO
                // O JwtFilter intercepta e valida o token antes que o Spring
                // tente qualquer outro mecanismo de autenticação.
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // [SEC-11] BCRYPT PARA HASHING DE SENHAS
    // BCrypt é um algoritmo de hashing adaptativo com salt automático.
    // Resistente a ataques de dicionário e rainbow tables.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // [SEC-12] CONFIGURAÇÃO CORS RESTRITIVA
    // Define exatamente quais origens, métodos e headers são permitidos.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // [SEC-13] ORIGENS EXPLÍCITAS — NUNCA USAR WILDCARD (*)
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:8080",
                "https://carrovivo.com"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
