# Documentação de Cybersegurança — Carro Vivo API

## Visão Geral

Este documento descreve todas as camadas de segurança implementadas na Carro Vivo API, mapeadas diretamente ao código-fonte. Cada seção indica o arquivo correspondente, o número do comentário `[SEC-XX]` no código e um espaço para o print da evidência.

---

## Índice

1. [Validação e Sanitização de Entrada](#1-validação-e-sanitização-de-entrada)
2. [Autenticação — JWT e Bearer Token](#2-autenticação--jwt-e-bearer-token)
3. [Autorização — RBAC](#3-autorização--rbac)
4. [Proteção contra Ataques](#4-proteção-contra-ataques)
5. [Criptografia em Repouso](#5-criptografia-em-repouso)
6. [Tratamento Seguro de Erros](#6-tratamento-seguro-de-erros)
7. [CORS](#7-cors)
8. [Auditoria e Logging](#8-auditoria-e-logging)
9. [Gerenciamento de Secrets](#9-gerenciamento-de-secrets)
10. [Resumo das Implementações](#10-resumo-das-implementações)

---

## 1. Validação e Sanitização de Entrada

### 1.1 Sanitização XSS — `XssRequestWrapper.java` e `XssFilter.java`

**Comentários no código:** `[SEC-18]` a `[SEC-24]`

**O que faz:**
Todo dado que entra na API passa pelo `XssFilter`, que envolve cada requisição com o `XssRequestWrapper`. O wrapper sanitiza caracteres HTML perigosos e padrões XSS antes que qualquer controller os leia.

**Cobertura da sanitização:**
- Body JSON (`@RequestBody`) via `getInputStream()` e `getReader()`
- Query parameters via `getParameter()` e `getParameterValues()`
- Headers via `getHeader()`

**Regras aplicadas:**

| Caractere/Padrão | Resultado |
|---|---|
| `<` | `&lt;` |
| `>` | `&gt;` |
| `'` | `&#x27;` |
| `"` | `&quot;` |
| `/` | `&#x2F;` |
| `eval(...)` | removido |
| `javascript:` | removido |
| `<script` | removido |

> 📸 **Print sugerido:** Requisição POST com `<script>alert('xss')</script>` no body e a resposta sanitizada.

---

### 1.2 Validação de DTOs — `VehicleDTO.java` (e demais DTOs)

**Comentários no código:** `[SEC-65]` a `[SEC-71]`

**O que faz:**
Cada campo do DTO é anotado com constraints de validação que o Spring verifica automaticamente antes de executar qualquer lógica de negócio.

**Três dimensões de validação (conforme especificação do challenge):**

| Dimensão | Annotation | Exemplo |
|---|---|---|
| **Tipagem** | `@Email`, `@Pattern` | Email no formato correto |
| **Presença** | `@NotBlank`, `@NotNull` | Campo obrigatório preenchido |
| **Tamanho** | `@Size`, `@Min`, `@Max` | Texto dentro do limite |

**Exemplo — campo `plate`:**
```java
@NotBlank(message = "Placa é obrigatória")           // presença
@Size(min = 7, max = 8, ...)                          // tamanho
@Pattern(regexp = "^[A-Z0-9-]+$", ...)               // tipagem (whitelist)
private String plate;
```

> 📸 **Print sugerido:** Requisição com placa `<script>xss</script>` retornando erro de validação 400.

---

### 1.3 Normalização — Enum `VehicleBrand`

**Comentários no código:** `[SEC-69]`

**O que faz:**
A marca do veículo aceita apenas valores do enum `VehicleBrand`. Qualquer valor fora do enum (`fOrD`, `FORD`, `ford`) é rejeitado, garantindo consistência e evitando injeção por variações de capitalização.

> 📸 **Print sugerido:** Requisição com `brand: "FoRd"` retornando erro de validação.

---

## 2. Autenticação — JWT e Bearer Token

### 2.1 Geração do Token — `JwtUtil.java`

**Comentários no código:** `[SEC-25]` a `[SEC-29]`

**O que faz:**
Gera tokens JWT assinados com HMAC-SHA256. O token contém username e role no payload, com validade de **30 minutos**.

**Estrutura do token:**
```
Header:    {"alg": "HS256", "typ": "JWT"}
Payload:   {"sub": "usuario", "role": "ADMIN", "iat": ..., "exp": ...}
Signature: HMAC-SHA256(header + payload, secret)
```

**Por que 30 minutos?**
Token sem expiração é uma vulnerabilidade permanente. Se vazado, um token de 30 min se torna inútil rapidamente.

> 📸 **Print sugerido:** Resposta do `/api/auth/login` com o token JWT. Também decodificar em jwt.io mostrando o payload com `exp`.

---

### 2.2 Validação do Token em Cada Requisição — `JwtFilter.java`

**Comentários no código:** `[SEC-30]` a `[SEC-33]`

**O que faz:**
Intercepta cada requisição, extrai o token do header `Authorization: Bearer <token>`, valida assinatura e expiração, e injeta a autenticação no contexto do Spring Security.

**Fluxo de validação:**
```
Requisição
    → JwtFilter
        → Header "Authorization" presente?
            → Começa com "Bearer "?
                → Token válido (assinatura + expiração)?
                    → Autentica no SecurityContext
                    → Controller executado
                → Token inválido → requisição sem autenticação → 401/403
```

> 📸 **Print sugerido:** Requisição com token expirado retornando 401. Requisição com token válido retornando 200.

---

## 3. Autorização — RBAC

### 3.1 Controle de Acesso Baseado em Papéis — `SecurityConfig.java`

**Comentários no código:** `[SEC-01]` a `[SEC-13]`

**Roles disponíveis (`Role.java`):**

| Role | Descrição |
|---|---|
| `ADMIN` | Acesso total, incluindo Swagger e registro de usuários |
| `ANALYST` | Acesso a veículos, manutenções, diagnósticos, garantias, notificações, dealers |
| `USER` | Acesso a veículos, notificações e dealers |

**Mapa de acesso por endpoint:**

| Endpoint | ADMIN | ANALYST | USER |
|---|---|---|---|
| `/api/auth/login` | ✅ | ✅ | ✅ |
| `/api/auth/register` | ✅ | ❌ | ❌ |
| `/swagger-ui/**` | ✅ | ❌ | ❌ |
| `/api/vehicles/**` | ✅ | ✅ | ✅ |
| `/api/maintenances/**` | ✅ | ✅ | ❌ |
| `/api/diagnostics/**` | ✅ | ✅ | ❌ |
| `/api/warranties/**` | ✅ | ✅ | ❌ |
| `/api/notifications/**` | ✅ | ✅ | ✅ |
| `/api/dealers/**` | ✅ | ✅ | ✅ |
| `/api/audit/**` | ✅ | ❌ | ❌ |

> 📸 **Print sugerido:** Requisição com token USER tentando acessar `/api/maintenances` retornando 403.

---

### 3.2 Proteção do Registro — `AuthController.java`

**Comentários no código:** `[SEC-44]`

**O que faz:**
O endpoint `/api/auth/register` é protegido por `@PreAuthorize("hasRole('ADMIN')")`, exigindo que apenas ADMINs criem novos usuários. Isso requer `@EnableMethodSecurity` no `SecurityConfig` — sem ele, a annotation é silenciosamente ignorada.

> 📸 **Print sugerido:** Requisição para `/register` com token USER retornando 403. Mesma requisição com token ADMIN retornando 200.

---

## 4. Proteção contra Ataques

### 4.1 Rate Limiting — `RateLimitFilter.java`

**Comentários no código:** `[SEC-14]` a `[SEC-17]`

**O que faz:**
Limita cada IP a 60 requisições por minuto usando o algoritmo Token Bucket (Bucket4j). IPs que excedem o limite recebem HTTP 429.

**Proteções cobertas:**
- Flooding de endpoints
- Denial of Service (DoS) por volume
- Scraping automatizado

> 📸 **Print sugerido:** Script fazendo 61 requisições rápidas e a 61ª retornando 429.

---

### 4.2 Brute Force — `AuthService.java`

**Comentários no código:** `[SEC-35]` a `[SEC-39]`

**O que faz:**
Conta tentativas de login falhas por username. Após 5 tentativas, o username é bloqueado independentemente da senha. A mesma mensagem `"Credenciais inválidas"` é retornada em todos os casos.

**Por que mesma mensagem para tudo?**
Se o sistema retornar mensagens diferentes para "usuário não existe" vs "senha errada", um atacante consegue enumerar quais usernames existem (User Enumeration Attack).

> 📸 **Print sugerido:** 5 tentativas de login falhas seguidas, com a 6ª bloqueada — todas retornando a mesma mensagem.

---

### 4.3 Proteção contra Buffer Overflow — Validações de Tamanho

**Comentários no código:** `[SEC-68]`

**O que faz:**
Todo campo de texto tem `@Size(max = N)`. Campos enviados além do limite são rejeitados com 400 antes de qualquer processamento.

> 📸 **Print sugerido:** Requisição com campo `model` de 10.000 caracteres retornando 400.

---

## 5. Criptografia em Repouso

### 5.1 AES-256 para Dados Sensíveis — `EncryptionService.java`

**Comentários no código:** `[SEC-47]` a `[SEC-53]`

**O que faz:**
Criptografa dados sensíveis armazenados no banco usando AES-256 no modo CBC com IV aleatório por chamada.

**Formato armazenado no banco:**
```
Base64(IV):Base64(ciphertext)
```

**Por que IV aleatório?**
IV fixo (todos zeros) faz com que o mesmo dado produza sempre o mesmo ciphertext, permitindo que um atacante com acesso ao banco identifique registros iguais por comparação. IV aleatório torna cada criptografia única.

**Algoritmos utilizados:**

| Dado | Algoritmo |
|---|---|
| Dados sensíveis | AES-256-CBC |
| Senhas de usuários | BCrypt |

> 📸 **Print sugerido:** Registro no banco com campo criptografado exibindo o formato `IV:ciphertext` ilegível.

---

### 5.2 BCrypt para Senhas — `SecurityConfig.java` + `AuthService.java`

**Comentários no código:** `[SEC-11]`, `[SEC-41]`

**O que faz:**
Senhas nunca são armazenadas em texto claro. O BCrypt gera um hash com salt automático, tornando ataques de rainbow table inviáveis.

> 📸 **Print sugerido:** Registro na tabela `users` mostrando o campo `password` com hash BCrypt (`$2a$...`).

---

## 6. Tratamento Seguro de Erros

### 6.1 Handler Global — `GlobalExceptionHandler.java`

**Comentários no código:** `[SEC-60]` a `[SEC-64]`

**O que faz:**
Captura todas as exceptions da aplicação e retorna respostas padronizadas sem expor stack traces, nomes de classes, consultas SQL ou qualquer detalhe interno.

**Comparação — antes vs depois:**

| Situação | ❌ Inseguro | ✅ Implementado |
|---|---|---|
| Erro 500 | `NullPointerException at VehicleService.java:42` | `"Ocorreu um erro interno."` |
| Recurso não encontrado | `Veículo não encontrado com id: 42` | `"Recurso não encontrado"` |
| Login inválido | `Usuário não encontrado` | `"Credenciais inválidas"` |

**Formato padronizado de resposta:**
```json
{
  "timestamp": "2026-05-18T20:00:00",
  "status": 404,
  "message": "Recurso não encontrado"
}
```

> 📸 **Print sugerido:** Requisição para recurso inexistente mostrando resposta genérica sem detalhes internos.

---

## 7. CORS

### 7.1 Configuração CORS Restritiva — `SecurityConfig.java`

**Comentários no código:** `[SEC-12]`, `[SEC-13]`

**O que faz:**
Define exatamente quais origens, métodos e headers podem fazer requisições cross-origin para a API. Wildcard `*` nunca é usado.

**Origens permitidas:**
```
http://localhost:3000
http://localhost:8080
https://carrovivo.com
```

**Métodos permitidos:** `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`

**Headers permitidos:** `Authorization`, `Content-Type`

> 📸 **Print sugerido:** Requisição de uma origem não listada retornando erro CORS no browser.

---

## 8. Auditoria e Logging

### 8.1 Audit Trail — `AuditService.java` e `AuditController.java`

**Comentários no código:** `[SEC-54]` a `[SEC-59]`

**O que faz:**
Registra todas as ações críticas no banco de dados e nos logs estruturados. Responde à pergunta: *"quem fez o quê, quando e de onde?"*

**Campos registrados em cada log:**

| Campo | Descrição |
|---|---|
| `username` | Extraído do token JWT (não do request) |
| `action` | Ação realizada (CREATE, UPDATE, DELETE...) |
| `resource` | Recurso afetado (Vehicle, User...) |
| `resourceId` | ID do recurso |
| `ipAddress` | IP de origem da requisição |
| `status` | SUCCESS ou FAILURE |
| `createdAt` | Timestamp automático |

**Endpoint de consulta (somente ADMIN, paginado):**
```
GET /api/audit?page=0&size=50
GET /api/audit/user/{username}
GET /api/audit/status/{status}
```

> 📸 **Print sugerido:** Resposta do `GET /api/audit` mostrando logs com username, action, ip e timestamp.

---

## 9. Gerenciamento de Secrets

### 9.1 Variáveis de Ambiente — `application.yml` e `.env`

**O que faz:**
Nenhum secret é hardcoded no código-fonte. Todos os valores sensíveis são lidos de variáveis de ambiente definidas no arquivo `.env` local (nunca commitado).

**Secrets gerenciados:**

| Variável | Uso |
|---|---|
| `JWT_SECRET` | Assinatura dos tokens JWT |
| `JWT_EXPIRATION` | Tempo de expiração do token (ms) |
| `ENCRYPTION_SECRET` | Chave AES-256 para criptografia |
| `DB_USER` | Usuário do banco de dados |
| `DB_PASSWORD` | Senha do banco de dados |

**Proteção no `.gitignore`:**
```
.env
.env.*
env
env.*
```

**`application.yml` — sem nenhum valor hardcoded:**
```yaml
jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:1800000}
encryption:
  secret: ${ENCRYPTION_SECRET}
```

> 📸 **Print sugerido:** Arquivo `application.yml` mostrando apenas referências a variáveis de ambiente. Arquivo `.gitignore` com `.env` listado.

---

## 10. Resumo das Implementações

A tabela abaixo lista as **22 funcionalidades de segurança** implementadas. Os comentários `[SEC-XX]` no código vão de **SEC-01 até SEC-71** — cada funcionalidade pode conter múltiplos comentários detalhando partes diferentes da implementação.

| # | Categoria | Implementação | Arquivo | Comentários no código |
|---|---|---|---|---|
| 1 | Sanitização | XSS — body, params e headers | `XssRequestWrapper.java` | SEC-18, SEC-19, SEC-20, SEC-21, SEC-22 |
| 2 | Sanitização | Filtro XSS global | `XssFilter.java` | SEC-23, SEC-24 |
| 3 | Validação | Tipagem, presença e tamanho nos DTOs | `VehicleDTO.java` e demais DTOs | SEC-65, SEC-66, SEC-67, SEC-68, SEC-69, SEC-70, SEC-71 |
| 4 | Validação | Normalização por enum | `VehicleBrand.java` | SEC-69 |
| 5 | Autenticação | JWT com HMAC-SHA256 | `JwtUtil.java` | SEC-25, SEC-26, SEC-27 |
| 6 | Autenticação | Expiração de 30 minutos | `JwtUtil.java` | SEC-28, SEC-29 |
| 7 | Autenticação | Validação do token em cada requisição | `JwtFilter.java` | SEC-30, SEC-31, SEC-32, SEC-33 |
| 8 | Autorização | RBAC — 3 roles (ADMIN, ANALYST, USER) | `SecurityConfig.java` | SEC-01, SEC-02, SEC-08, SEC-09 |
| 9 | Autorização | Swagger protegido por ADMIN | `SecurityConfig.java` | SEC-07 |
| 10 | Autorização | Sessão stateless | `SecurityConfig.java` | SEC-03, SEC-04, SEC-05 |
| 11 | Autorização | @PreAuthorize no registro | `AuthController.java` | SEC-42, SEC-43, SEC-44 |
| 12 | Ataques | Rate limiting 60 req/min por IP | `RateLimitFilter.java` | SEC-14, SEC-15, SEC-16, SEC-17 |
| 13 | Ataques | Brute force — bloqueio após 5 tentativas | `AuthService.java` | SEC-34, SEC-35, SEC-36, SEC-38, SEC-39 |
| 14 | Ataques | User enumeration — mensagem genérica | `AuthService.java` | SEC-37, SEC-38 |
| 15 | Criptografia | AES-256-CBC com IV aleatório | `EncryptionService.java` | SEC-47, SEC-48, SEC-49, SEC-50, SEC-51, SEC-52 |
| 16 | Criptografia | BCrypt para senhas | `SecurityConfig.java` + `AuthService.java` | SEC-11, SEC-40, SEC-41 |
| 17 | Erros | Mensagens genéricas sem stack trace | `GlobalExceptionHandler.java` | SEC-60, SEC-61, SEC-62, SEC-63, SEC-64 |
| 18 | CORS | Origens explícitas, sem wildcard | `SecurityConfig.java` | SEC-12, SEC-13 |
| 19 | Auditoria | Trilha completa de ações | `AuditService.java` | SEC-54, SEC-55, SEC-56, SEC-57 |
| 20 | Auditoria | Consulta paginada (anti-DoS) | `AuditController.java` | SEC-58, SEC-59 |
| 21 | Secrets | Nenhum valor hardcoded | `application.yml` | — |
| 22 | Secrets | `.env` no `.gitignore` | `.gitignore` | — |

### Distribuição completa por arquivo

| Arquivo | Comentários SEC | Total |
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
| `VehicleDTO.java` e demais DTOs | SEC-65 a SEC-71 | 7 |
| **Total** | **SEC-01 a SEC-71** | **71** |