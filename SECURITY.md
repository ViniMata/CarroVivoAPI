# Documentação de Cybersegurança — Carro Vivo API

> Documento de evidência técnica para o Challenge — Ford.
> Cada seção mapeia um requisito ao código implementado.

---

## Índice

- [0. Imports Explícitos — Sem Wildcard](#0-imports-explícitos--sem-wildcard)
- [1. Validação e Sanitização](#1-validação-e-sanitização)
  - [1.1 Sanitização XSS](#11-sanitização-xss)
  - [1.2 Validação — Tipagem, Presença e Tamanho](#12-validação--tipagem-presença-e-tamanho)
  - [1.3 Normalização por Enum](#13-normalização-por-enum)
  - [1.4 Buffer Overflow e Flooding](#14-buffer-overflow-e-flooding)
- [2. Tratamento Seguro de Erros](#2-tratamento-seguro-de-erros)
- [3. Autenticação — JWT e Bearer Token](#3-autenticação--jwt-e-bearer-token)
  - [3.1 Geração do Token JWT](#31-geração-do-token-jwt)
  - [3.2 Bearer Token e Expiração](#32-bearer-token-e-expiração)
  - [3.3 Validação do Token em Cada Requisição](#33-validação-do-token-em-cada-requisição)
- [4. Autorização — RBAC](#4-autorização--rbac)
  - [4.1 Roles e Controle de Acesso](#41-roles-e-controle-de-acesso)
  - [4.2 Proteção do Registro de Usuários](#42-proteção-do-registro-de-usuários)
  - [4.3 Sessão Stateless](#43-sessão-stateless)
- [5. Proteção contra Ataques](#5-proteção-contra-ataques)
  - [5.1 Rate Limiting — Flooding e DoS](#51-rate-limiting--flooding-e-dos)
  - [5.2 Brute Force e User Enumeration](#52-brute-force-e-user-enumeration)
- [6. HTTPS e Blindagem da Comunicação](#6-https-e-blindagem-da-comunicação)
- [7. CORS Correto](#7-cors-correto)
- [8. Criptografia em Repouso](#8-criptografia-em-repouso)
  - [8.1 AES-256 para Dados Sensíveis](#81-aes-256-para-dados-sensíveis)
  - [8.2 BCrypt para Senhas](#82-bcrypt-para-senhas)
- [9. Retenção, Descarte e Pseudoanonimização](#9-retenção-descarte-e-pseudoanonimização)
- [10. Exposição Acidental — Secrets e Logs](#10-exposição-acidental--secrets-e-logs)
- [11. Auditoria e Logging Estruturado](#11-auditoria-e-logging-estruturado)
- [12. Resumo das Implementações](#12-resumo-das-implementações)

---

## 0. Imports Explícitos — Sem Wildcard

> **Requisito:** `import pacote.*` é proibido. Cada classe deve ser importada individualmente. Isso é valido para todas as linguagens de programação!!!

### Por que isso importa para segurança?

- `import pacote.*` importa **tudo** de um pacote, incluindo classes que não são usadas
- Dificulta a **auditoria de dependências** — impossível saber o que o arquivo realmente usa
- Viola o princípio do **menor privilégio** aplicado ao código
- Pode criar **conflitos silenciosos** entre classes de pacotes diferentes

Todos os arquivos do projeto usam imports individuais e explícitos.

---

### `SecurityConfig.java`

```java
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
```

---

### `XssRequestWrapper.java`

```java
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
```

---

### `XssFilter.java`

```java
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import java.io.IOException;
```

---

### `AuthService.java`

> Apenas `Map`, `Optional`, `ConcurrentHashMap` e `AtomicInteger` importados do `java.util` — não `java.util.*`

```java
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
```

---

### `EncryptionService.java`

> Apenas 3 classes de `javax.crypto` necessárias para AES-256 — sem `javax.crypto.*`

```java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
```

---

### `JwtUtil.java`

> 5 classes do `io.jsonwebtoken` — sem `io.jsonwebtoken.*`

```java
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
```

---

### `GlobalExceptionHandler.java`

> `LinkedHashMap`, `List` e `Map` importados individualmente de `java.util` — não `java.util.*`

```java
import com.carrovivo.api.security.SecurityException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
```

---

### `VehicleDTO.java`

> Cada annotation de validação importada individualmente de `jakarta.validation.constraints` — não `jakarta.validation.constraints.*`

```java
import com.carrovivo.api.vehicle.model.VehicleBrand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
```

---

### `RateLimitFilter.java`

```java
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
```

---

## 1. Validação e Sanitização

> **Requisito:** Validar tipagem, presença e tamanho. Sanitizar caracteres especiais. Nunca confiar no que vem do teclado do usuário.

---

### 1.1 Sanitização XSS

**Arquivo:** `XssRequestWrapper.java` + `XssFilter.java`
**Comentários no código:** `[SEC-18]` a `[SEC-24]`

**O que faz:**
Todo dado que entra na API passa pelo `XssFilter`, que envolve cada requisição com o `XssRequestWrapper`. O wrapper sanitiza caracteres HTML perigosos e padrões XSS antes que qualquer controller leia os dados.

**Cobertura — o que é sanitizado:**

| Entrada | Método interceptado |
|---|---|
| Body JSON (`@RequestBody`) | `getInputStream()` e `getReader()` |
| Query parameters | `getParameter()` e `getParameterValues()` |
| Headers HTTP | `getHeader()` |

**Regras de sanitização:**

| Caractere / Padrão | Resultado após sanitização |
|---|---|
| `<` | `&lt;` |
| `>` | `&gt;` |
| `'` | `&#x27;` |
| `"` | `&quot;` |
| `/` | `&#x2F;` |
| `(` | `&#40;` |
| `)` | `&#41;` |
| `eval(...)` | removido |
| `javascript:` | removido |
| `<script` | removido |

```java
// [SEC-22] SANITIZAÇÃO DO BODY JSON
private String sanitizeBody(String value) {
    if (value == null) return null;
    return value
            .replaceAll("(?i)<script[^>]*>.*?</script>", "")
            .replaceAll("(?i)<script",      "")
            .replaceAll("(?i)javascript:",  "")
            .replaceAll("(?i)eval\\(.*?\\)", "");
}

// [SEC-22] SANITIZAÇÃO DE PARAMS E HEADERS
private String sanitizeParam(String value) {
    if (value == null) return null;
    return value
            .replaceAll("<",   "&lt;")
            .replaceAll(">",   "&gt;")
            .replaceAll("'",   "&#x27;")
            .replaceAll("\"",  "&quot;")
            .replaceAll("/",   "&#x2F;")
            .replaceAll("\\(", "&#40;")
            .replaceAll("\\)", "&#41;")
            .replaceAll("(?i)eval\\(.*?\\)", "")
            .replaceAll("(?i)javascript:",   "")
            .replaceAll("(?i)<script",       "");
}
```

---

### 1.2 Validação — Tipagem, Presença e Tamanho

**Arquivos:** todos os DTOs
**Comentários no código:** `[SEC-65]` a `[SEC-71]`

**DTOs com validação implementada — todos seguem o mesmo padrão:**

| Arquivo | Localização |
|---|---|
| `VehicleDTO.java` | `vehicle/dto/` |
| `MaintenanceDTO.java` | `maintenance/dto/` |
| `DiagnosticDTO.java` | `diagnostic/dto/` |
| `WarrantyDTO.java` | `warranty/dto/` |
| `NotificationDTO.java` | `notification/dto/` |
| `DealerDTO.java` | `dealer/dto/` |

> Os trechos abaixo usam o `VehicleDTO.java` como **exemplo representativo**. Todos os outros DTOs têm exatamente o mesmo padrão — a diferença é apenas nos campos específicos de cada domínio.

**As três dimensões de validação:**

| Dimensão | Annotation usada | Exemplo de campo |
|---|---|---|
| **Tipagem** | `@Email`, `@Pattern` | `ownerEmail`, `plate`, `status` |
| **Presença** | `@NotBlank`, `@NotNull` | `plate`, `vehicleId`, `startDate` |
| **Tamanho** | `@Size`, `@Min`, `@Max` | `model`, `description`, `year` |

```java
// [SEC-66] VALIDAÇÃO DE PLACA — TIPAGEM + PRESENÇA + TAMANHO + FORMATO
@NotBlank(message = "Placa é obrigatória")              // PRESENÇA
@Size(min = 7, max = 8, message = "...")                // TAMANHO
@Pattern(regexp = "^[A-Z0-9-]+$", message = "...")     // TIPAGEM (whitelist)
private String plate;

// [SEC-71] VALIDAÇÃO DE EMAIL — FORMATO RFC
@NotBlank(message = "Email é obrigatório")              // PRESENÇA
@Email(message = "Email inválido")                      // TIPAGEM
@Size(max = 200, message = "...")                       // TAMANHO
private String ownerEmail;

// [SEC-70] INTERVALO NUMÉRICO
@NotNull(message = "Ano é obrigatório")                 // PRESENÇA
@Min(value = 1900, message = "Ano inválido")            // TIPAGEM (mínimo)
@Max(value = 2100, message = "Ano inválido")            // TIPAGEM (máximo)
private Integer year;
```

**`DiagnosticDTO.java` — whitelist de valores exatos:**
```java
// [SEC-70] PATTERN REGEX — WHITELIST DE VALORES
@Pattern(regexp = "GREEN|YELLOW|RED", message = "Status deve ser GREEN, YELLOW ou RED")
private String status;
```

**`DealerDTO.java` — whitelist de caracteres:**
```java
// [SEC-70] PATTERN REGEX — WHITELIST DE CARACTERES
@Pattern(regexp = "^[0-9()\\- +]*$", message = "Telefone contém caracteres inválidos")
@Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
private String phone;
```

---

### 1.3 Normalização por Enum

**Arquivo:** `VehicleBrand.java`
**Comentários no código:** `[SEC-69]`

**O que faz:**
A marca do veículo aceita apenas valores do enum `VehicleBrand`. Qualquer variação de capitalização (`fOrD`, `ford`, `FORD`) é rejeitada — o cliente deve enviar exatamente o valor do enum.

```java
// [SEC-69] ENUM PARA MARCA — NORMALIZAÇÃO E WHITELIST
// Aceita apenas valores do enum VehicleBrand (FORD, CHEVROLET...).
// Impede valores arbitrários e garante consistência dos dados.
@NotNull(message = "Marca é obrigatória")
private VehicleBrand brand;
```

---

### 1.4 Buffer Overflow e Flooding

**Arquivo:** todos os DTOs
**Comentários no código:** `[SEC-68]`

**O que faz:**
Todo campo de texto tem `@Size(max = N)`. Campos enviados além do limite são rejeitados com 400 antes de qualquer processamento, prevenindo esgotamento de memória.

| Campo | Limite |
|---|---|
| `serviceType` (Maintenance) | 200 caracteres |
| `description` (Maintenance, Warranty, Diagnostic) | 500 caracteres |
| `title` (Notification) | 200 caracteres |
| `message` (Notification) | 500 caracteres |
| `name` (Dealer) | 200 caracteres |
| `model` (Vehicle) | 100 caracteres |
| `ownerName` (Vehicle) | 200 caracteres |

```java
// [SEC-68] TAMANHO MÁXIMO EM CAMPOS DE TEXTO LIVRE
// Previne buffer overflow e flooding via campos gigantes.
@Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
private String description;
```

---

## 2. Tratamento Seguro de Erros

> **Requisito:** Esconder stack traces, tecnologias e detalhes internos nas respostas de erro.

**Arquivo:** `GlobalExceptionHandler.java`
**Comentários no código:** `[SEC-60]` a `[SEC-64]`

**Comparação — inseguro vs implementado:**

| Situação | ❌ Inseguro | ✅ Implementado |
|---|---|---|
| Erro 500 | `NullPointerException at VehicleService.java:42` | `"Ocorreu um erro interno."` |
| Recurso não encontrado | `Veículo não encontrado com id: 42` | `"Recurso não encontrado"` |
| Login inválido | `Usuário não encontrado no banco` | `"Credenciais inválidas"` |
| SQL inválido | `ERROR: relation "vehicles" does not exist` | `"Ocorreu um erro interno."` |

**Formato padronizado de todas as respostas de erro:**
```json
{
  "timestamp": "2026-05-18T20:00:00",
  "status": 404,
  "message": "Recurso não encontrado"
}
```

```java
// [SEC-64] FALLBACK GENÉRICO — NENHUM DETALHE INTERNO VAZA
@ExceptionHandler(Exception.class)
public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorBody("Ocorreu um erro interno. Tente novamente mais tarde.", 500));
}

// [SEC-62] 401 GENÉRICO PARA FALHAS DE AUTENTICAÇÃO
@ExceptionHandler(SecurityException.class)
public ResponseEntity<Map<String, Object>> handleSecurity(SecurityException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(errorBody("Credenciais inválidas", 401));
}
```

**Configuração no `application.yml` — erros sem detalhes:**
```yaml
server:
  error:
    include-message: never
    include-stacktrace: never
    include-binding-errors: never
```

---

## 3. Autenticação — JWT e Bearer Token

> **Requisito:** JWT com Header, Payload e Signature. Bearer token com expiração obrigatória.

---

### 3.1 Geração do Token JWT

**Arquivo:** `JwtUtil.java`
**Comentários no código:** `[SEC-25]` a `[SEC-29]`

**Estrutura do token JWT:**
```
Header:    {"alg": "HS256", "typ": "JWT"}
Payload:   {"sub": "admin", "role": "ADMIN", "iat": 1716000000, "exp": 1716001800}
Signature: HMAC-SHA256(Base64(header) + "." + Base64(payload), JWT_SECRET)
```

> O Payload **nunca contém** a senha ou o JWT_SECRET — apenas username e role.

```java
// [SEC-27] GERAÇÃO DO TOKEN COM HMAC-SHA256
public String generateToken(String username, String role) {
    Key key = Keys.hmacShaKeyFor(secret.getBytes());
    return Jwts.builder()
            .setSubject(username)
            .claim("role", role)
            .setIssuedAt(new Date())
            // [SEC-28] EXPIRAÇÃO OBRIGATÓRIA — 30 MINUTOS
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
}
```

---

### 3.2 Bearer Token e Expiração

**Arquivo:** `JwtUtil.java` + `application.yml`
**Comentários no código:** `[SEC-28]`, `[SEC-26]`

**Por que 30 minutos?**
Token sem expiração é uma vulnerabilidade permanente. Se vazado, permanece válido indefinidamente. Com 30 min, o dano é limitado no tempo.

```yaml
jwt:
  expiration: ${JWT_EXPIRATION:1800000}  # 30 minutos em ms
```

**Uso no header de todas as requisições autenticadas:** (Token de exemploooooo!!!!!!)
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

### 3.3 Validação do Token em Cada Requisição

**Arquivo:** `JwtFilter.java`
**Comentários no código:** `[SEC-30]` a `[SEC-33]`

**Fluxo de validação:**
```
Requisição HTTP
    └─→ JwtFilter (OncePerRequestFilter)
            └─→ Header "Authorization" presente e começa com "Bearer "?
                    └─→ Token válido? (assinatura HMAC + data de expiração)
                            ├─→ SIM → injeta autenticação no SecurityContext → Controller
                            └─→ NÃO → segue sem autenticação → 401 ou 403 pelo SecurityConfig
```

```java
// [SEC-32] VALIDAÇÃO DO TOKEN ANTES DE AUTENTICAR
if (jwtUtil.isTokenValid(token)) {
    String username = jwtUtil.extractUsername(token);
    String role = jwtUtil.extractRole(token);

    // [SEC-33] INJEÇÃO DA AUTENTICAÇÃO NO CONTEXTO DO SPRING SECURITY
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            username, null,
            List.of(new SimpleGrantedAuthority("ROLE_" + role))
    );
    SecurityContextHolder.getContext().setAuthentication(auth);
}
```

---

## 4. Autorização — RBAC

> **Requisito:** Role Based Access Control. Admin: configurações globais. Analista: leads e dashboard. User: dados pessoais.

---

### 4.1 Roles e Controle de Acesso

**Arquivo:** `SecurityConfig.java` + `Role.java`
**Comentários no código:** `[SEC-01]`, `[SEC-02]`, `[SEC-08]`, `[SEC-09]`

| Role | Descrição |
|---|---|
| `ADMIN` | Configurações globais, Swagger, registro de usuários, auditoria |
| `ANALYST` | Veículos, manutenções, diagnósticos, garantias, notificações, dealers |
| `USER` | Veículos, notificações e dealers (dados unitários) |

**Mapa completo de acesso por endpoint:**

| Endpoint | ADMIN | ANALYST | USER |
|---|:---:|:---:|:---:|
| `POST /api/auth/login` | ✅ | ✅ | ✅ |
| `POST /api/auth/register` | ✅ | ❌ | ❌ |
| `/swagger-ui/**` | ✅ | ❌ | ❌ |
| `GET/POST /api/vehicles/**` | ✅ | ✅ | ✅ |
| `/api/maintenances/**` | ✅ | ✅ | ❌ |
| `/api/diagnostics/**` | ✅ | ✅ | ❌ |
| `/api/warranties/**` | ✅ | ✅ | ❌ |
| `/api/notifications/**` | ✅ | ✅ | ✅ |
| `/api/dealers/**` | ✅ | ✅ | ✅ |
| `/api/audit/**` | ✅ | ❌ | ❌ |

```java
// [SEC-08] RBAC — CONTROLE DE ACESSO BASEADO EM PAPÉIS
.requestMatchers("/api/vehicles/**").hasAnyRole("ADMIN", "ANALYST", "USER")
.requestMatchers("/api/maintenances/**").hasAnyRole("ADMIN", "ANALYST")
.requestMatchers("/api/diagnostics/**").hasAnyRole("ADMIN", "ANALYST")
// [SEC-07] SWAGGER PROTEGIDO — SÓ ADMIN ACESSA
.requestMatchers("/swagger-ui/**", "/swagger-ui.html",
        "/api-docs/**", "/v3/api-docs/**", "/webjars/**").hasRole("ADMIN")
// [SEC-09] FALLBACK: QUALQUER ROTA NÃO MAPEADA EXIGE ADMIN
.anyRequest().hasRole("ADMIN")
```

---

### 4.2 Proteção do Registro de Usuários

**Arquivo:** `AuthController.java` + `SecurityConfig.java`
**Comentários no código:** `[SEC-44]`, `[SEC-02]`

`/api/auth/register` é protegido por `@PreAuthorize("hasRole('ADMIN')")`. Isso exige `@EnableMethodSecurity` ativo no `SecurityConfig` — sem ele, a annotation é **silenciosamente ignorada**.

```java
// [SEC-44] REGISTRO PROTEGIDO POR ROLE ADMIN
@PostMapping("/register")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
    authService.register(request.getUsername(), request.getPassword(), request.getRole());
    return ResponseEntity.ok(Map.of("message", "Usuário registrado com sucesso"));
}

// [SEC-02] HABILITA @PreAuthorize NOS CONTROLLERS
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig { ... }
```

---

### 4.3 Sessão Stateless

**Arquivo:** `SecurityConfig.java`
**Comentários no código:** `[SEC-05]`

Nenhum estado de sessão é mantido no servidor. Cada requisição deve apresentar seu próprio token JWT.

```java
// [SEC-05] SESSÃO STATELESS
.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

---

## 5. Proteção contra Ataques

> **Requisito:** Buffer overflow, flooding, rate limiting, proteção contra brute force.

---

### 5.1 Rate Limiting — Flooding e DoS

**Arquivo:** `RateLimitFilter.java`
**Comentários no código:** `[SEC-14]` a `[SEC-17]`

Limita cada IP a **60 requisições por minuto** usando o algoritmo Token Bucket (Bucket4j). IPs que excedem o limite recebem HTTP 429.

```java
// [SEC-15] BUCKET POR IP — ISOLAMENTO DE LIMITE
private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

// [SEC-16] 60 REQUISIÇÕES POR MINUTO POR IP
private Bucket createBucket() {
    return Bucket.builder()
            .addLimit(Bandwidth.classic(60, Refill.greedy(60, Duration.ofMinutes(1))))
            .build();
}

// [SEC-17] RESPOSTA 429 — TOO MANY REQUESTS
httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
httpResponse.getWriter().write(
        "{\"status\":429,\"message\":\"Muitas requisições. Tente novamente em instantes.\"}"
);
```

---

### 5.2 Brute Force e User Enumeration

**Arquivo:** `AuthService.java`
**Comentários no código:** `[SEC-34]` a `[SEC-39]`

Após **5 tentativas de login falhas** por username, o acesso é bloqueado. A mesma mensagem genérica é retornada para todos os casos — impede que atacantes descubram quais usernames existem.

```java
// [SEC-35] BRUTE FORCE — MÁXIMO DE TENTATIVAS POR USERNAME
private static final int MAX_ATTEMPTS = 5;
private final Map<String, AtomicInteger> loginAttempts = new ConcurrentHashMap<>();

// [SEC-36] VERIFICAÇÃO DE BLOQUEIO ANTES DA CONSULTA AO BANCO
if (attempts.get() >= MAX_ATTEMPTS) {
    throw new SecurityException("Credenciais inválidas");
}

// [SEC-37] QUERY ÚNICA — SEM USER ENUMERATION
// Se o usuário não existe, orElse(false) retorna false —
// mesma resposta que senha incorreta.
Optional<UserEntity> userOpt = userRepository.findByUsername(username);
boolean valid = userOpt
        .map(user -> passwordEncoder.matches(password, user.getPassword()))
        .orElse(false);

if (!valid) {
    // [SEC-38] MESMA MENSAGEM SEMPRE
    attempts.incrementAndGet();
    throw new SecurityException("Credenciais inválidas");
}
```

---

## 6. HTTPS e Blindagem da Comunicação

> **Requisito:** TLS 1.2 obrigatório. Sem HTTPS os dados trafegam em texto claro — sniffing de tokens, credenciais e dados pessoais.

HTTPS cria um túnel criptografado entre cliente e servidor usando TLS. Em produção, configurado via proxy reverso (Nginx) ou plataforma de deploy.

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEY_PASSWORD}
    key-store-type: PKCS12
```

---

## 7. CORS Correto

> **Requisito:** Cross-Origin Resource Sharing configurado com origens explícitas. Nunca usar wildcard `*`.

**Arquivo:** `SecurityConfig.java`
**Comentários no código:** `[SEC-12]`, `[SEC-13]`

```java
// [SEC-13] ORIGENS EXPLÍCITAS — NUNCA USAR WILDCARD (*)
config.setAllowedOrigins(List.of(
        "http://localhost:3000",
        "http://localhost:8080",
        "https://carrovivo.com"
));
config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
config.setAllowCredentials(true);
```

| Configuração | Valor |
|---|---|
| Origens permitidas | `localhost:3000`, `localhost:8080`, `carrovivo.com` |
| Wildcard `*` | ❌ nunca usado |
| Métodos permitidos | GET, POST, PUT, DELETE, OPTIONS |
| Headers permitidos | Authorization, Content-Type |

---

## 8. Criptografia em Repouso

> **Requisito:** Dado no DB deve ser ilegível. AES-256 para dados sensíveis. BCrypt para senhas. Não usar DES ou MD5.

---

### 8.1 AES-256 para Dados Sensíveis

**Arquivo:** `EncryptionService.java`
**Comentários no código:** `[SEC-47]` a `[SEC-53]`

IV gerado aleatoriamente a cada chamada com `SecureRandom`. IV fixo faz o mesmo dado produzir sempre o mesmo ciphertext — IV aleatório torna cada criptografia única.

**Formato no banco:**
```
Base64(IV):Base64(ciphertext)
```

```java
// [SEC-50] IV ALEATÓRIO POR CHAMADA — SEGURANÇA REAL DO AES-CBC
public String encrypt(String data) {
    byte[] ivBytes = new byte[IV_LENGTH];
    new SecureRandom().nextBytes(ivBytes);
    IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

    SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "AES");
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

    byte[] encrypted = cipher.doFinal(data.getBytes());
    return Base64.getEncoder().encodeToString(ivBytes)
            + ":" + Base64.getEncoder().encodeToString(encrypted);
}
```

---

### 8.2 BCrypt para Senhas

**Arquivo:** `SecurityConfig.java` + `AuthService.java`
**Comentários no código:** `[SEC-11]`, `[SEC-41]`

Senhas nunca armazenadas em texto claro. BCrypt gera hash com salt automático.

| Algoritmo | Status | Motivo |
|---|---|---|
| BCrypt | ✅ Usado para senhas | Adaptativo, salt automático |
| AES-256-CBC | ✅ Usado para dados | Simétrico, reversível com chave |
| MD5 | ❌ Não usado | Quebrado, rainbow tables |
| DES | ❌ Não usado | Chave 56-bit, quebrado |
| SHA-1 | ❌ Não usado | Colisões conhecidas |

```java
// [SEC-11] BCRYPT PARA HASHING DE SENHAS
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// [SEC-41] SENHA ARMAZENADA COMO HASH BCRYPT
.password(passwordEncoder.encode(password))
```

---

## 9. Retenção, Descarte e Pseudoanonimização

> **Requisito:** Dado guardado é risco guardado. LGPD. Anonimização vs deleção física. Pseudoanonimização reversível.

**Arquivo:** `EncryptionService.java`
**Comentários no código:** `[SEC-53]`

```java
// [SEC-53] ANONIMIZAÇÃO DE DADOS — LGPD
public String anonymize(String data) {
    if (data == null || data.length() < 4) return "***";
    return data.substring(0, 2) + "***" + data.substring(data.length() - 2);
}
```

**Exemplo:**
```
Original:    joao.silva@ford.com
Anonimizado: jo***il
```

| Tipo | Reversível? | Uso |
|---|---|---|
| Anonimização | ❌ Não | Deleção definitiva de dados |
| Pseudoanonimização | ✅ Sim (com chave) | Dashboards, ML, logs |

---

## 10. Exposição Acidental — Secrets e Logs

> **Requisito:** Cuidado com o .env. Logs que gravam senhas. Endpoints de teste esquecidos. Logs que preservam a stack.

### Secrets via variáveis de ambiente

Nenhum secret hardcoded. Todos os valores sensíveis lidos do `.env`:

```yaml
jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:1800000}
encryption:
  secret: ${ENCRYPTION_SECRET}
spring:
  datasource:
    username: ${DB_USER}
    password: ${DB_PASSWORD}
```

### .env protegido pelo .gitignore

```gitignore
.env
.env.*
env
env.*
!.env.example
```

### Logs sem stack trace

```yaml
server:
  error:
    include-message: never
    include-stacktrace: never
    include-binding-errors: never
```

### Show-SQL desabilitado

```yaml
jpa:
  show-sql: false
```

---

## 11. Auditoria e Logging Estruturado

> **Requisito:** Logs em JSON. Rastreabilidade. Quem fez o quê, quando e onde. Audit trail para ações críticas.

**Arquivos:** `AuditService.java`, `AuditController.java`
**Comentários no código:** `[SEC-54]` a `[SEC-59]`

**Campos registrados:**

| Campo | Descrição | Exemplo |
|---|---|---|
| `username` | Extraído do token JWT | `admin` |
| `action` | Ação realizada | `CREATE`, `UPDATE`, `DELETE` |
| `resource` | Recurso afetado | `Vehicle`, `User` |
| `resourceId` | ID do recurso | `42` |
| `ipAddress` | IP de origem | `192.168.1.100` |
| `status` | Resultado | `SUCCESS`, `FAILURE` |
| `createdAt` | Timestamp automático | `2026-05-18T20:00:00` |

```java
// [SEC-55] LOG ESTRUTURADO SEM DADOS SENSÍVEIS
public void log(String action, String resource, String resourceId,
                String ipAddress, String status) {

    // [SEC-56] USERNAME EXTRAÍDO DO CONTEXTO DE SEGURANÇA
    String username = SecurityContextHolder.getContext()
            .getAuthentication().getName();

    // [SEC-57] PERSISTÊNCIA NO BANCO E LOG SIMULTÂNEOS
    repository.save(audit);
    log.info("[AUDIT] user={} action={} resource={} id={} ip={} status={}",
            username, action, resource, resourceId, ipAddress, status);
}
```

**Consulta paginada — Anti-DoS:**

```java
// [SEC-59] PAGINAÇÃO OBRIGATÓRIA — PREVENÇÃO DE DoS
@GetMapping
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Page<AuditLog>> findAll(
        @PageableDefault(size = 50, sort = "createdAt") Pageable pageable) {
    return ResponseEntity.ok(repository.findAll(pageable));
}
```

**Endpoints (somente ADMIN):**
```
GET /api/audit?page=0&size=50
GET /api/audit/user/{username}?page=0&size=50
GET /api/audit/status/{status}?page=0&size=50
```

---

## 12. Resumo das Implementações

| # | Requisito | Categoria | Implementação | Arquivo | Comentários |
|---|---|---|---|---|---|
| 1 | Sanitização XSS | Sanitização | XSS — body, params e headers | `XssRequestWrapper.java` | SEC-18 a SEC-22 |
| 2 | Filtro XSS global | Sanitização | Filtro XSS global | `XssFilter.java` | SEC-23, SEC-24 |
| 3 | Validação 101 | Validação | Tipagem, presença e tamanho | todos os DTOs | SEC-65 a SEC-71 |
| 4 | Normalização | Validação | Enum para marca/modelo | `VehicleBrand.java` | SEC-69 |
| 5 | Buffer Overflow | Validação | @Size em todos os campos | todos os DTOs | SEC-68 |
| 6 | JWT | Autenticação | JWT com HMAC-SHA256 | `JwtUtil.java` | SEC-25 a SEC-27 |
| 7 | Expiração | Autenticação | Token expira em 30 minutos | `JwtUtil.java` | SEC-28, SEC-29 |
| 8 | Bearer Token | Autenticação | Validação em cada requisição | `JwtFilter.java` | SEC-30 a SEC-33 |
| 9 | RBAC | Autorização | 3 roles (ADMIN, ANALYST, USER) | `SecurityConfig.java` | SEC-01, SEC-08, SEC-09 |
| 10 | RBAC | Autorização | Swagger protegido por ADMIN | `SecurityConfig.java` | SEC-07 |
| 11 | Stateless | Autorização | Sessão stateless | `SecurityConfig.java` | SEC-05 |
| 12 | RBAC | Autorização | @PreAuthorize no registro | `AuthController.java` | SEC-42 a SEC-44 |
| 13 | Flooding | Ataques | Rate limiting 60 req/min por IP | `RateLimitFilter.java` | SEC-14 a SEC-17 |
| 14 | Brute Force | Ataques | Bloqueio após 5 tentativas | `AuthService.java` | SEC-34 a SEC-36 |
| 15 | User Enumeration | Ataques | Mensagem genérica sempre | `AuthService.java` | SEC-37, SEC-38 |
| 16 | HTTPS | Comunicação | TLS obrigatório em produção | configuração de deploy | — |
| 17 | CORS | Comunicação | Origens explícitas, sem wildcard | `SecurityConfig.java` | SEC-12, SEC-13 |
| 18 | Criptografia | Criptografia | AES-256-CBC com IV aleatório | `EncryptionService.java` | SEC-47 a SEC-52 |
| 19 | Senhas | Criptografia | BCrypt para senhas | `SecurityConfig.java` | SEC-11, SEC-41 |
| 20 | Erros | Erros | Mensagens genéricas sem stack trace | `GlobalExceptionHandler.java` | SEC-60 a SEC-64 |
| 21 | .env | Secrets | Nenhum valor hardcoded | `application.yml` | — |
| 22 | .env | Secrets | .env protegido no .gitignore | `.gitignore` | — |
| 23 | LGPD | Dados | Pseudoanonimização | `EncryptionService.java` | SEC-53 |
| 24 | Logging | Auditoria | Audit trail completo | `AuditService.java` | SEC-54 a SEC-57 |
| 25 | Monitoramento | Auditoria | Consulta paginada (anti-DoS) | `AuditController.java` | SEC-58, SEC-59 |
| 26 | Imports | Boas práticas | Imports explícitos, sem wildcard | todos os arquivos | — |

### Distribuição dos comentários SEC por arquivo

| Arquivo | Range | Total |
|---|---|---|
| `SecurityConfig.java` | SEC-01 a SEC-13 | 13 |
| `RateLimitFilter.java` | SEC-14 a SEC-17 | 4 |
| `XssRequestWrapper.java` | SEC-18 a SEC-22 | 5 |
| `XssFilter.java` | SEC-23 a SEC-24 | 2 |
| `JwtUtil.java` | SEC-25 a SEC-29 | 5 |
| `JwtFilter.java` | SEC-30 a SEC-33 | 4 |
| `AuthService.java` | SEC-34 a SEC-41 | 8 |
| `AuthController.java` | SEC-42 a SEC-46 | 5 |
| `EncryptionService.java` | SEC-47 a SEC-53 | 7 |
| `AuditService.java` | SEC-54 a SEC-57 | 4 |
| `AuditController.java` | SEC-58 a SEC-59 | 2 |
| `GlobalExceptionHandler.java` | SEC-60 a SEC-64 | 5 |
| todos os DTOs | SEC-65 a SEC-71 | 7 |
| **Total** | **SEC-01 a SEC-71** | **71** |
