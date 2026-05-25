package com.carrovivo.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

// [SEC-30] FILTRO JWT — INTERCEPTA E VALIDA O TOKEN EM CADA REQUISIÇÃO
// Estende OncePerRequestFilter: garante execução única por requisição,
// mesmo em casos de forward ou dispatch interno.
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // [SEC-31] LEITURA DO BEARER TOKEN NO HEADER Authorization
        // Padrão OAuth2: "Authorization: Bearer <token>"
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            // [SEC-32] VALIDAÇÃO DO TOKEN ANTES DE AUTENTICAR
            // Só autentica se o token for válido (assinatura + expiração).
            // Token inválido é silenciosamente ignorado — a requisição
            // continua sem autenticação e será bloqueada pelo SecurityConfig.
            if (jwtUtil.isTokenValid(token)) {
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);

                // [SEC-33] INJEÇÃO DA AUTENTICAÇÃO NO CONTEXTO DO SPRING SECURITY
                // A partir daqui, o Spring reconhece o usuário como autenticado
                // e aplica as regras de autorização definidas no SecurityConfig.
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
