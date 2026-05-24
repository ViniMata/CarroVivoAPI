package com.carrovivo.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

// JWT — JSON WEB TOKEN
// Mecanismo de autenticação stateless. O token carrega as informações
// do usuário (username e role) assinadas digitalmente com HMAC-SHA256.
// Composto por: Header (algoritmo) + Payload (dados) + Signature (selo).
@Component
public class JwtUtil {

    // SECRET E EXPIRATION VIA VARIÁVEL DE AMBIENTE
    // Nunca hardcoded. Lidos do .env para evitar exposição no repositório.
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration; // padrão: 1800000ms = 30 minutos

    // GERAÇÃO DO TOKEN COM HMAC-SHA256
    // O token é assinado com a chave secreta. Qualquer alteração
    // no payload invalida a assinatura, impedindo adulteração.
    public String generateToken(String username, String role) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                // EXPIRAÇÃO OBRIGATÓRIA — 30 MINUTOS
                // Token sem expiração é uma vulnerabilidade permanente.
                // Após 30 min, o usuário deve autenticar novamente.
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return (String) parseClaims(token).get("role");
    }

    // VALIDAÇÃO EXPLÍCITA DE EXPIRAÇÃO
    // Verifica assinatura E data de expiração.
    // JwtException cobre token adulterado, malformado ou com assinatura inválida.
    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
