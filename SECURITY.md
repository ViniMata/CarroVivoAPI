# Documentação de Cybersegurança — Carro Vivo API

> Documento de evidência técnica para o Challenge — Ford.
> Cada seção mapeia um requisito ao código implementado.
> Todos os comentários `[SEC-01]` a `[SEC-71]` estão documentados aqui.

---

## Índice

- [0. Imports Explícitos — Sem Wildcard](#0-imports-explícitos--sem-wildcard)
- [1. Validação e Sanitização](#1-validação-e-sanitização)
- [2. Tratamento Seguro de Erros](#2-tratamento-seguro-de-erros)
- [3. Autenticação — JWT e Bearer Token](#3-autenticação--jwt-e-bearer-token)
- [4. Autorização — RBAC](#4-autorização--rbac)
- [5. Proteção contra Ataques](#5-proteção-contra-ataques)
- [6. HTTPS e Blindagem da Comunicação](#6-https-e-blindagem-da-comunicação)
- [7. CORS Correto](#7-cors-correto)
- [8. Criptografia em Repouso](#8-criptografia-em-repouso)
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

### 1.1 Sanitização XSS — `XssRequestWrapper.java` + `XssFilter.java`

**Arquivo:** `XssRequestWrapper.java` + `XssFilter.java`

```java
// [SEC-18] SANITIZAÇÃO XSS — PROTEÇÃO CONTRA CROSS-SITE SCRIPTING
// Intercepta e sanitiza toda entrada antes que chegue aos controllers.
// Cobre query params, headers E body JSON (via getInputStream/getReader).
public class XssRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] sanitizedBody;

    // [SEC-19] SANITIZAÇÃO DO BODY NO CONSTRUTOR
    // O body é lido, sanitizado e armazenado em memória uma única vez.
    public XssRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        String body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        this.sanitizedBody = sanitizeBody(body).getBytes(StandardCharsets.UTF_8);
    }

    // [SEC-20] getInputStream E getReader SOBRESCRITOS
    // O Spring usa estes métodos para ler @RequestBody em APIs REST.
    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream bais = new ByteArrayInputStream(sanitizedBody);
        return new ServletInputStream() {
            @Override public boolean isFinished() { return bais.available() == 0; }
            @Override public boolean isReady()    { return true; }
            @Override public void setReadListener(ReadListener l) {}
            @Override public int read()           { return bais.read(); }
        };
    }

    // [SEC-21] SANITIZAÇÃO DE QUERY PARAMS E HEADERS
    @Override
    public String getParameter(String parameter) {
        return sanitizeParam(super.getParameter(parameter));
    }

    // [SEC-22] SANITIZAÇÃO DO BODY JSON — remove padrões XSS sem tocar na estrutura JSON
    private String sanitizeBody(String value) {
        if (value == null) return null;
        return value
                .replaceAll("(?i)<script[^>]*>.*?</script>", "")
                .replaceAll("(?i)<script",      "")
                .replaceAll("(?i)javascript:",  "")
                .replaceAll("(?i)eval\\(.*?\\)", "");
    }

    // [SEC-22] SANITIZAÇÃO DE PARAMS — escapa caracteres HTML especiais
    private String sanitizeParam(String value) {
        if (value == null) return null;
        return value
                .replaceAll("<", "&lt;").replaceAll(">", "&gt;")
                .replaceAll("'", "&#x27;").replaceAll("\"", "&quot;")
                .replaceAll("/", "&#x2F;")
                .replaceAll("(?i)<script", "").replaceAll("(?i)javascript:", "");
    }
}
```

```java
// [SEC-23] FILTRO XSS — PONTO DE ENTRADA DA SANITIZAÇÃO
// Intercepta todas as requisições HTTP e as envolve com o XssRequestWrapper.
@Component
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // [SEC-24] TODA REQUISIÇÃO É SANITIZADA
        // A partir daqui, qualquer leitura retornará dados já sanitizados.
        chain.doFilter(new XssRequestWrapper((HttpServletRequest) request), response);
    }
}
```

**Cobertura:**

| Entrada | Método interceptado |
|---|---|
| Body JSON (`@RequestBody`) | `getInputStream()` e `getReader()` |
| Query parameters | `getParameter()` e `getParameterValues()` |
| Headers HTTP | `getHeader()` |

**Regras de sanitização:**

| Caractere / Padrão | Resultado |
|---|---|
| `<` | `&lt;` |
| `>` | `&gt;` |
| `'` | `&#x27;` |
| `"` | `&quot;` |
| `/` | `&#x2F;` |
| `eval(...)` | removido |
| `javascript:` | removido |
| `<script` | removido |

---

### 1.2 Validação — Tipagem, Presença e Tamanho

**Arquivo:** todos os DTOs (`VehicleDTO`, `MaintenanceDTO`, `DiagnosticDTO`, `WarrantyDTO`, `NotificationDTO`, `DealerDTO`)

> O `VehicleDTO.java` é usado como **exemplo representativo** — todos os outros DTOs seguem o mesmo padrão.

```java
// [SEC-65] DTO COM VALIDAÇÃO COMPLETA — PRIMEIRA LINHA DE DEFESA
// Toda entrada do usuário é validada antes de chegar ao service.
// Cobre tipagem, presença, tamanho e formato.
public class VehicleDTO {

    // [SEC-66] VALIDAÇÃO DE PLACA — TIPAGEM + PRESENÇA + TAMANHO + FORMATO
    @NotBlank(message = "Placa é obrigatória")              // PRESENÇA
    @Size(min = 7, max = 8, message = "...")                // TAMANHO

    // [SEC-67] PATTERN REGEX — WHITELIST DE CARACTERES
    // Aceita apenas letras maiúsculas, números e hífen.
    // Qualquer outro caractere (incluindo SQL injection e XSS) é rejeitado.
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Placa contém caracteres inválidos") // TIPAGEM
    private String plate;

    // [SEC-68] TAMANHO MÁXIMO EM TODOS OS CAMPOS DE TEXTO
    // Previne buffer overflow e flooding via campos gigantes.
    @NotBlank(message = "Modelo é obrigatório")
    @Size(max = 100, message = "Modelo deve ter no máximo 100 caracteres")
    private String model;

    // [SEC-69] ENUM PARA MARCA — NORMALIZAÇÃO E WHITELIST
    // Aceita apenas valores do enum VehicleBrand (FORD, CHEVROLET...).
    // Impede valores arbitrários: fOrD, ford, FORD são todos rejeitados.
    @NotNull(message = "Marca é obrigatória")
    private VehicleBrand brand;

    // [SEC-70] INTERVALO NUMÉRICO — VALIDAÇÃO DE TIPAGEM
    @NotNull(message = "Ano é obrigatório")
    @Min(value = 1900, message = "Ano inválido")
    @Max(value = 2100, message = "Ano inválido")
    private Integer year;

    // [SEC-71] VALIDAÇÃO DE EMAIL — FORMATO RFC
    // @Email valida o formato conforme RFC. Combinado com @Size
    // para prevenir endereços absurdamente longos.
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Size(max = 200, message = "Email deve ter no máximo 200 caracteres")
    private String ownerEmail;
}
```

**As três dimensões de validação:**

| Dimensão | Annotation | Exemplo |
|---|---|---|
| **Tipagem** | `@Email`, `@Pattern` | `ownerEmail`, `plate`, `status` |
| **Presença** | `@NotBlank`, `@NotNull` | `plate`, `vehicleId`, `startDate` |
| **Tamanho** | `@Size`, `@Min`, `@Max` | `model`, `description`, `year` |

**Limites por campo em todos os DTOs:**

| Campo | Limite |
|---|---|
| `serviceType` (Maintenance) | 200 caracteres |
| `description` (Maintenance, Warranty, Diagnostic) | 500 caracteres |
| `title` (Notification) | 200 caracteres |
| `message` (Notification) | 500 caracteres |
| `name` (Dealer) | 200 caracteres |
| `model` (Vehicle) | 100 caracteres |
| `ownerName` (Vehicle) | 200 caracteres |

---

## 2. Tratamento Seguro de Erros

> **Requisito:** Esconder stack traces, tecnologias e detalhes internos nas respostas de erro.

**Arquivo:** `GlobalExceptionHandler.java`

```java
// [SEC-60] TRATAMENTO SEGURO DE ERROS — PONTO CENTRAL
// Captura todas as exceptions e retorna respostas padronizadas
// sem expor stack traces, tecnologias ou estrutura interna.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // [SEC-61] MENSAGEM GENÉRICA PARA RECURSO NÃO ENCONTRADO
    // Nunca retorna "Veículo não encontrado com id: 42" —
    // isso expõe estrutura interna e facilita enumeração de recursos.
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorBody("Recurso não encontrado", 404));
    }

    // [SEC-62] 401 GENÉRICO PARA FALHAS DE AUTENTICAÇÃO
    // SecurityException é lançada para usuário inexistente E senha errada —
    // a mesma mensagem impede user enumeration.
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurity(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorBody("Credenciais inválidas", 401));
    }

    // [SEC-63] ERROS DE VALIDAÇÃO — RETORNA MENSAGENS DOS @Constraints
    // Expõe apenas as mensagens definidas nas annotations,
    // nunca detalhes técnicos internos.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(FieldError::getDefaultMessage).toList();
        Map<String, Object> body = errorBody("Erro de validação nos campos informados", 400);
        body.put("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    // [SEC-64] FALLBACK GENÉRICO — NENHUM DETALHE INTERNO VAZA
    // Stack trace, nome da classe e tecnologia nunca aparecem na resposta.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody("Ocorreu um erro interno. Tente novamente mais tarde.", 500));
    }
}
```

**Configuração no `application.yml`:**
```yaml
server:
  error:
    include-message: never
    include-stacktrace: never
    include-binding-errors: never
```

**Comparação — inseguro vs implementado:**

| Situação | ❌ Inseguro | ✅ Implementado |
|---|---|---|
| Erro 500 | `NullPointerException at VehicleService.java:42` | `"Ocorreu um erro interno."` |
| Recurso não encontrado | `Veículo não encontrado com id: 42` | `"Recurso não encontrado"` |
| Login inválido | `Usuário não encontrado no banco` | `"Credenciais inválidas"` |

**Formato padronizado:**
```json
{
  "timestamp": "2026-05-18T20:00:00",
  "status": 404,
  "message": "Recurso não encontrado"
}
```

---

## 3. Autenticação — JWT e Bearer Token

> **Requisito:** JWT com Header, Payload e Signature. Bearer token com expiração obrigatória.

### 3.1 Geração do Token — `JwtUtil.java`

```java
// [SEC-25] JWT — JSON WEB TOKEN
// Mecanismo de autenticação stateless. O token carrega as informações
// do usuário (username e role) assinadas digitalmente com HMAC-SHA256.
// Composto por: Header (algoritmo) + Payload (dados) + Signature (selo).
@Component
public class JwtUtil {

    // [SEC-26] SECRET E EXPIRATION VIA VARIÁVEL DE AMBIENTE
    // Nunca hardcoded. Lidos do .env para evitar exposição no repositório.
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration; // 1800000ms = 30 minutos

    // [SEC-27] GERAÇÃO DO TOKEN COM HMAC-SHA256
    // O token é assinado com a chave secreta. Qualquer alteração
    // no payload invalida a assinatura, impedindo adulteração.
    public String generateToken(String username, String role) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                // [SEC-28] EXPIRAÇÃO OBRIGATÓRIA — 30 MINUTOS
                // Token sem expiração é uma vulnerabilidade permanente.
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // [SEC-29] VALIDAÇÃO EXPLÍCITA DE EXPIRAÇÃO
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
}
```

**Estrutura do token JWT:**
```
Header:    {"alg": "HS256", "typ": "JWT"}
Payload:   {"sub": "admin", "role": "ADMIN", "iat": 1716000000, "exp": 1716001800}
Signature: HMAC-SHA256(Base64(header) + "." + Base64(payload), JWT_SECRET)
```

> O Payload **nunca contém** a senha ou o JWT_SECRET — apenas username e role.

**Configuração:**
```yaml
jwt:
  expiration: ${JWT_EXPIRATION:1800000}  # 30 minutos em ms
```

---

### 3.2 Validação do Token — `JwtFilter.java`

```java
// [SEC-30] FILTRO JWT — INTERCEPTA E VALIDA O TOKEN EM CADA REQUISIÇÃO
// Estende OncePerRequestFilter: garante execução única por requisição.
@Component
public class JwtFilter extends OncePerRequestFilter {

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
            // Token inválido é silenciosamente ignorado — bloqueado pelo SecurityConfig.
            if (jwtUtil.isTokenValid(token)) {
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);

                // [SEC-33] INJEÇÃO DA AUTENTICAÇÃO NO CONTEXTO DO SPRING SECURITY
                // O Spring reconhece o usuário como autenticado e aplica as regras de autorização.
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

**Fluxo:**
```
Requisição → JwtFilter → Bearer presente? → Token válido?
    ├─→ SIM → injeta autenticação → Controller
    └─→ NÃO → sem autenticação → 401/403 pelo SecurityConfig
```

---

## 4. Autorização — RBAC

> **Requisito:** Role Based Access Control. Admin: configurações globais. Analista: leads e dashboard. User: dados pessoais.

### 4.1 Configuração Central — `SecurityConfig.java`

```java
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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // [SEC-03] CSRF DESABILITADO — API STATELESS
                // APIs REST com JWT não usam cookies de sessão,
                // portanto CSRF não se aplica aqui.
                .csrf(AbstractHttpConfigurer::disable)

                // [SEC-04] CORS COM ORIGENS EXPLÍCITAS
                // Nunca usar wildcard (*).
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // [SEC-05] SESSÃO STATELESS
                // Cada requisição deve apresentar seu próprio token JWT.
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // [SEC-06] ÚNICO ENDPOINT PÚBLICO
                        // Apenas o login é acessível sem autenticação.
                        .requestMatchers("/api/auth/login").permitAll()

                        // [SEC-07] SWAGGER PROTEGIDO POR ROLE ADMIN
                        // Evita exposição da estrutura interna da API.
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                "/api-docs/**", "/v3/api-docs/**", "/webjars/**").hasRole("ADMIN")

                        // [SEC-08] RBAC — CONTROLE DE ACESSO BASEADO EM PAPÉIS
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
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // [SEC-11] BCRYPT PARA HASHING DE SENHAS
    // BCrypt é um algoritmo de hashing adaptativo com salt automático.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // [SEC-12] CONFIGURAÇÃO CORS RESTRITIVA
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
```

**Mapa de acesso por endpoint:**

| Endpoint | ADMIN | ANALYST | USER |
|---|:---:|:---:|:---:|
| `POST /api/auth/login` | ✅ | ✅ | ✅ |
| `POST /api/auth/register` | ✅ | ❌ | ❌ |
| `/swagger-ui/**` | ✅ | ❌ | ❌ |
| `/api/vehicles/**` | ✅ | ✅ | ✅ |
| `/api/maintenances/**` | ✅ | ✅ | ❌ |
| `/api/diagnostics/**` | ✅ | ✅ | ❌ |
| `/api/warranties/**` | ✅ | ✅ | ❌ |
| `/api/notifications/**` | ✅ | ✅ | ✅ |
| `/api/dealers/**` | ✅ | ✅ | ✅ |
| `/api/audit/**` | ✅ | ❌ | ❌ |

---

### 4.2 Proteção do Registro — `AuthController.java`

```java
// [SEC-42] CONTROLLER DE AUTENTICAÇÃO
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // [SEC-43] ENDPOINT DE LOGIN — ÚNICO ENDPOINT PÚBLICO DA API
    // @Valid ativa a validação do LoginRequest antes de qualquer processamento.
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(Map.of("token", token));
    }

    // [SEC-44] REGISTRO PROTEGIDO POR ROLE ADMIN
    // Sem @PreAuthorize qualquer pessoa poderia se registrar como ADMIN.
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request.getUsername(), request.getPassword(), request.getRole());
        return ResponseEntity.ok(Map.of("message", "Usuário registrado com sucesso"));
    }

    // [SEC-45] VALIDAÇÃO DE ENTRADA NO LOGIN
    // @NotBlank e @Size previnem buffer overflow via campos gigantes.
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
```

---

## 5. Proteção contra Ataques

> **Requisito:** Buffer overflow, flooding, rate limiting, proteção contra brute force.

### 5.1 Rate Limiting — `RateLimitFilter.java`

```java
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
    // Janela deslizante (greedy refill): tokens reabastecidos continuamente.
    private Bucket createBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(60, Refill.greedy(60, Duration.ofMinutes(1))))
                .build();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String ip = ((HttpServletRequest) request).getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(ip, k -> createBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            // [SEC-17] RESPOSTA 429 — TOO MANY REQUESTS
            // Retorna status padronizado sem expor detalhes internos.
            ((HttpServletResponse) response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            ((HttpServletResponse) response).getWriter().write(
                    "{\"status\":429,\"message\":\"Muitas requisições. Tente novamente em instantes.\"}"
            );
        }
    }
}
```

---

### 5.2 Brute Force e User Enumeration — `AuthService.java`

```java
// [SEC-34] SERVIÇO DE AUTENTICAÇÃO COM MÚLTIPLAS CAMADAS DE PROTEÇÃO
@Service
public class AuthService {

    // [SEC-35] BRUTE FORCE — MÁXIMO DE TENTATIVAS POR USERNAME
    // Após 5 tentativas falhas consecutivas, o username é bloqueado.
    private static final int MAX_ATTEMPTS = 5;
    private final Map<String, AtomicInteger> loginAttempts = new ConcurrentHashMap<>();

    public String login(String username, String password) {
        AtomicInteger attempts = loginAttempts.computeIfAbsent(username, k -> new AtomicInteger(0));

        // [SEC-36] VERIFICAÇÃO DE BLOQUEIO ANTES DA CONSULTA AO BANCO
        // Bloqueia imediatamente sem acessar o banco, economizando recursos.
        if (attempts.get() >= MAX_ATTEMPTS) {
            throw new SecurityException("Credenciais inválidas");
        }

        // [SEC-37] QUERY ÚNICA — SEM USER ENUMERATION
        // orElse(false) retorna false se o usuário não existe —
        // resposta idêntica à de senha incorreta. Impede user enumeration.
        Optional<UserEntity> userOpt = userRepository.findByUsername(username);
        boolean valid = userOpt
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);

        if (!valid) {
            // [SEC-38] MESMA MENSAGEM SEMPRE — IMPEDE USER ENUMERATION
            // Não diferencia "usuário não existe" de "senha errada".
            attempts.incrementAndGet();
            throw new SecurityException("Credenciais inválidas");
        }

        // [SEC-39] RESET DO CONTADOR EM LOGIN BEM-SUCEDIDO
        attempts.set(0);
        UserEntity user = userOpt.get();
        return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
    }

    public UserEntity register(String username, String password, Role requestedRole) {
        // [SEC-40] FALLBACK SEGURO DE ROLE
        // Se a role vier nula, atribui USER como padrão mais restritivo.
        Role safeRole = (requestedRole != null) ? requestedRole : Role.USER;

        UserEntity user = UserEntity.builder()
                .username(username)
                // [SEC-41] SENHA ARMAZENADA COMO HASH BCRYPT
                // A senha nunca é armazenada em texto claro.
                .password(passwordEncoder.encode(password))
                .role(safeRole)
                .build();
        return userRepository.save(user);
    }
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

> **Requisito:** Cross-Origin Resource Sharing com origens explícitas. Nunca usar wildcard `*`.

Implementado em `[SEC-12]` e `[SEC-13]` no `SecurityConfig.java` (ver seção 4.1).

| Configuração | Valor |
|---|---|
| Origens permitidas | `localhost:3000`, `localhost:8080`, `carrovivo.com` |
| Wildcard `*` | ❌ nunca usado |
| Métodos permitidos | GET, POST, PUT, DELETE, OPTIONS |
| Headers permitidos | Authorization, Content-Type |

---

## 8. Criptografia em Repouso

> **Requisito:** Dado no DB deve ser ilegível. AES-256 para dados sensíveis. BCrypt para senhas. Não usar DES ou MD5.

### 8.1 AES-256 — `EncryptionService.java`

```java
// [SEC-47] CRIPTOGRAFIA EM REPOUSO — AES-256-CBC
// Dados sensíveis armazenados no banco são criptografados com AES-256.
@Service
public class EncryptionService {

    // [SEC-48] CHAVE SECRETA VIA VARIÁVEL DE AMBIENTE
    // Nunca hardcoded. Deve ter exatamente 32 caracteres para AES-256.
    @Value("${encryption.secret}")
    private String secret;

    // [SEC-49] AES-256 COM MODO CBC E PADDING PKCS5
    // CBC: cada bloco cifrado depende do anterior,
    // tornando padrões repetitivos invisíveis no ciphertext.
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;

    // [SEC-50] IV ALEATÓRIO POR CHAMADA — SEGURANÇA REAL DO AES-CBC
    // O mesmo dado produz ciphertexts diferentes a cada chamada.
    // Formato: Base64(IV):Base64(ciphertext)
    public String encrypt(String data) {
        try {
            byte[] ivBytes = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(ivBytes); // IV criptograficamente seguro
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(ivBytes)
                    + ":" + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            // [SEC-51] ERRO GENÉRICO — NÃO EXPÕE DETALHES DA CRIPTOGRAFIA
            throw new RuntimeException("Erro ao criptografar dado");
        }
    }

    // [SEC-52] DESCRIPTOGRAFIA — EXTRAI IV DO PREFIXO
    public String decrypt(String encryptedData) {
        try {
            String[] parts = encryptedData.split(":");
            byte[] ivBytes = Base64.getDecoder().decode(parts[0]);
            byte[] cipherBytes = Base64.getDecoder().decode(parts[1]);
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            return new String(cipher.doFinal(cipherBytes));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar dado");
        }
    }

    // [SEC-53] ANONIMIZAÇÃO DE DADOS — LGPD
    // Substitui parte do dado por asteriscos para exibição em dashboards e logs.
    public String anonymize(String data) {
        if (data == null || data.length() < 4) return "***";
        return data.substring(0, 2) + "***" + data.substring(data.length() - 2);
    }
}
```

### 8.2 BCrypt para Senhas

Implementado em `[SEC-11]` e `[SEC-41]` (ver seções 4.1 e 5.2).

| Algoritmo | Status | Motivo |
|---|---|---|
| BCrypt | ✅ Usado para senhas | Adaptativo, salt automático |
| AES-256-CBC | ✅ Usado para dados | Simétrico, reversível com chave |
| MD5 | ❌ Não usado | Quebrado, rainbow tables |
| DES | ❌ Não usado | Chave 56-bit, quebrado |
| SHA-1 | ❌ Não usado | Colisões conhecidas |

---

## 9. Retenção, Descarte e Pseudoanonimização

> **Requisito:** Dado guardado é risco guardado. LGPD. Anonimização vs deleção física. Pseudoanonimização reversível.

Implementado em `[SEC-53]` no `EncryptionService.java` (ver seção 8.1).

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

```yaml
# application.yml — sem nenhum valor hardcoded
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

### `AuditService.java`

```java
// [SEC-54] AUDIT TRAIL — TRILHA DE AUDITORIA
// Registra todas as ações críticas: quem fez o quê, quando e de qual IP.
@Slf4j
@Service
public class AuditService {

    // [SEC-55] LOG ESTRUTURADO SEM DADOS SENSÍVEIS
    // Registra ação, recurso, IP e status — nunca senhas ou tokens.
    public void log(String action, String resource, String resourceId,
                    String ipAddress, String status) {

        // [SEC-56] USERNAME EXTRAÍDO DO CONTEXTO DE SEGURANÇA
        // Não confia no username enviado pelo cliente — usa o do token JWT validado.
        String username = "anonymous";
        try {
            username = SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception ignored) {}

        AuditLog audit = AuditLog.builder()
                .username(username).action(action).resource(resource)
                .resourceId(resourceId).ipAddress(ipAddress).status(status)
                .build();

        // [SEC-57] PERSISTÊNCIA NO BANCO E LOG SIMULTÂNEOS
        repository.save(audit);
        log.info("[AUDIT] user={} action={} resource={} id={} ip={} status={}",
                username, action, resource, resourceId, ipAddress, status);
    }
}
```

### `AuditController.java`

```java
// [SEC-58] CONTROLLER DE AUDITORIA — ACESSO EXCLUSIVO ADMIN
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    // [SEC-59] PAGINAÇÃO OBRIGATÓRIA — PREVENÇÃO DE DoS
    // Sem paginação, findAll() poderia retornar milhões de registros.
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLog>> findAll(
            @PageableDefault(size = 50, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(repository.findAll(pageable));
    }
}
```

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
| 2 | Filtro XSS global | Sanitização | Filtro global de sanitização | `XssFilter.java` | SEC-23, SEC-24 |
| 3 | Validação 101 | Validação | Tipagem, presença e tamanho | todos os DTOs | SEC-65 a SEC-71 |
| 4 | Normalização | Validação | Enum para marca/modelo | `VehicleBrand.java` | SEC-69 |
| 5 | Buffer Overflow | Validação | @Size em todos os campos | todos os DTOs | SEC-68 |
| 6 | JWT | Autenticação | JWT com HMAC-SHA256 | `JwtUtil.java` | SEC-25 a SEC-27 |
| 7 | Expiração | Autenticação | Token expira em 30 minutos | `JwtUtil.java` | SEC-28, SEC-29 |
| 8 | Bearer Token | Autenticação | Validação em cada requisição | `JwtFilter.java` | SEC-30 a SEC-33 |
| 9 | RBAC | Autorização | 3 roles (ADMIN, ANALYST, USER) | `SecurityConfig.java` | SEC-01 a SEC-10 |
| 10 | BCrypt | Criptografia | Hash de senhas | `SecurityConfig.java` | SEC-11 |
| 11 | CORS | Comunicação | Origens explícitas, sem wildcard | `SecurityConfig.java` | SEC-12, SEC-13 |
| 12 | Rate Limiting | Ataques | 60 req/min por IP | `RateLimitFilter.java` | SEC-14 a SEC-17 |
| 13 | Brute Force | Ataques | Bloqueio após 5 tentativas | `AuthService.java` | SEC-34 a SEC-36 |
| 14 | User Enumeration | Ataques | Mesma mensagem sempre | `AuthService.java` | SEC-37, SEC-38 |
| 15 | Role segura | Autorização | Fallback para USER | `AuthService.java` | SEC-39, SEC-40 |
| 16 | BCrypt hash | Criptografia | Senha hasheada no registro | `AuthService.java` | SEC-41 |
| 17 | Login público | Autorização | Único endpoint público | `AuthController.java` | SEC-42, SEC-43 |
| 18 | Registro ADMIN | Autorização | @PreAuthorize no registro | `AuthController.java` | SEC-44 a SEC-46 |
| 19 | AES-256 | Criptografia | Criptografia com IV aleatório | `EncryptionService.java` | SEC-47 a SEC-52 |
| 20 | LGPD | Dados | Pseudoanonimização | `EncryptionService.java` | SEC-53 |
| 21 | Audit Trail | Auditoria | Trilha completa de ações | `AuditService.java` | SEC-54 a SEC-57 |
| 22 | Anti-DoS | Auditoria | Consulta paginada | `AuditController.java` | SEC-58, SEC-59 |
| 23 | Erros seguros | Erros | Mensagens genéricas | `GlobalExceptionHandler.java` | SEC-60 a SEC-64 |
| 24 | Stateless | Autorização | Sem sessão no servidor | `SecurityConfig.java` | SEC-05 |
| 25 | Swagger seguro | Autorização | Swagger só para ADMIN | `SecurityConfig.java` | SEC-07 |
| 26 | .env | Secrets | Nenhum valor hardcoded | `application.yml` | — |
| 27 | .gitignore | Secrets | .env protegido | `.gitignore` | — |
| 28 | HTTPS | Comunicação | TLS obrigatório em produção | configuração de deploy | — |
| 29 | Imports | Boas práticas | Sem wildcard em todo projeto | todos os arquivos | — |

### Distribuição dos comentários SEC por arquivo

| Arquivo | Comentários | Total |
|---|---|---|
| `SecurityConfig.java` | SEC-01 a SEC-13 | 13 |
| `RateLimitFilter.java` | SEC-14 a SEC-17 | 4 |
| `XssRequestWrapper.java` | SEC-18 a SEC-22 | 5 |
| `XssFilter.java` | SEC-23, SEC-24 | 2 |
| `JwtUtil.java` | SEC-25 a SEC-29 | 5 |
| `JwtFilter.java` | SEC-30 a SEC-33 | 4 |
| `AuthService.java` | SEC-34 a SEC-41 | 8 |
| `AuthController.java` | SEC-42 a SEC-46 | 5 |
| `EncryptionService.java` | SEC-47 a SEC-53 | 7 |
| `AuditService.java` | SEC-54 a SEC-57 | 4 |
| `AuditController.java` | SEC-58, SEC-59 | 2 |
| `GlobalExceptionHandler.java` | SEC-60 a SEC-64 | 5 |
| todos os DTOs | SEC-65 a SEC-71 | 7 |
| **Total** | **SEC-01 a SEC-71** | **71** |
