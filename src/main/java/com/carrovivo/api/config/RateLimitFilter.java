package com.carrovivo.api.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// [SEC-14] RATE LIMITING — PROTEÇÃO CONTRA FLOODING E DoS
// Limita o número de requisições por IP para evitar ataques
// de negação de serviço (DoS) e flooding de endpoints.
@Component
public class RateLimitFilter implements Filter {

    // [SEC-15] BUCKET POR IP — ISOLAMENTO DE LIMITE
    // Cada IP tem seu próprio bucket de tokens, garantindo que
    // um IP agressivo não afete os outros usuários.
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // [SEC-16] 60 REQUISIÇÕES POR MINUTO POR IP
    // Janela deslizante (greedy refill): tokens são reabastecidos
    // continuamente, não em rajada ao fim do minuto.
    private Bucket createBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(60, Refill.greedy(60, Duration.ofMinutes(1))))
                .build();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(ip, k -> createBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            // [SEC-17] RESPOSTA 429 — TOO MANY REQUESTS
            // Retorna status padronizado sem expor detalhes internos.
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(
                    "{\"status\":429,\"message\":\"Muitas requisições. Tente novamente em instantes.\"}"
            );
        }
    }
}
