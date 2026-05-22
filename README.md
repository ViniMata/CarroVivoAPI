# 🚗 Carro Vivo API

API REST para gerenciamento inteligente de veículos — diagnóstico de peças, histórico de manutenções, garantias, notificações e concessionárias próximas.

---

## 📋 Sobre o Projeto

O **Carro Vivo** é um sistema orientado a serviços (SOA) desenvolvido com **Java 21** e **Spring Boot 3**, seguindo os princípios de arquitetura REST e boas práticas de segurança e desenvolvimento.

---

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────┐
│         Camada de Apresentação          │
│     Controllers REST + Swagger UI       │
├─────────────────────────────────────────┤
│           Camada de Serviço             │
│  VehicleService  │  MaintenanceService  │
│  DiagnosticService │  WarrantyService   │
│  NotificationService │  DealerService   │
├─────────────────────────────────────────┤
│            Camada de Dados              │
│     PostgreSQL + Flyway Migrations      │
└─────────────────────────────────────────┘
```

### Estrutura de Pacotes

```
com.carrovivo.api
├── vehicle/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── model/
│   └── dto/
├── maintenance/
├── diagnostic/
├── warranty/
├── notification/
├── dealer/
├── config/
├── exception/
└── security/
```

---

## 🛠️ Tecnologias

| Tecnologia | Uso |
|---|---|
| Java 21 | Linguagem principal |
| Spring Boot | Framework principal |
| Spring Security | Autenticação JWT + RBAC |
| PostgreSQL 16 | Banco de dados |
| Flyway | Controle de migrações |
| Lombok | Redução de boilerplate |
| SpringDoc OpenAPI | Documentação Swagger |
| Docker | Containerização do banco |

---

## 🚀 Como Rodar o Projeto

### Pré-requisitos

- Java 21+
- Maven
- Docker Desktop

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/carrovivo-api.git
cd carrovivo-api
```

### 2. Configure as variáveis de ambiente

Copie o arquivo de exemplo e preencha com seus valores:

```bash
cp .env.example .env
```

Edite o `.env` com suas credenciais. **Nunca commite o `.env`** — ele já está no `.gitignore`.

### 3. Suba o banco de dados

```bash
docker-compose up -d
```

> O Docker lê automaticamente as variáveis do `.env` na raiz do projeto.

### 4. Execute o projeto

```bash
./mvnw spring-boot:run
```

Ou pelo IntelliJ IDEA com `Shift + F10`.

### 5. Acesse a documentação

O Swagger está protegido e requer autenticação com role **ADMIN**:

```
http://localhost:8080/swagger-ui.html
```

---

## 🔐 Segurança

### Autenticação

A API usa **JWT Bearer Tokens** com expiração de **30 minutos**.

**Login:**
```
POST /api/auth/login
{ "username": "admin", "password": "sua-senha" }
```

Copie o token retornado e use no header de todas as requisições:
```
Authorization: Bearer <token>
```

### Roles (RBAC)

| Role | Acesso |
|---|---|
| ADMIN | Tudo, incluindo Swagger e registro de usuários |
| ANALYST | Veículos, manutenções, diagnósticos, garantias, notificações, dealers |
| USER | Veículos, notificações, dealers |

### Registro de usuários

Somente **ADMIN** pode registrar novos usuários:
```
POST /api/auth/register  (requer token ADMIN)
{ "username": "analista1", "password": "senha123", "role": "ANALYST" }
```

---

## 📦 Banco de Dados

A conexão usa variáveis de ambiente definidas no `.env`. As migrações são executadas automaticamente pelo Flyway ao iniciar:

| Versão | Arquivo | Descrição |
|---|---|---|
| V1 | `V1__create_vehicles.sql` | Tabela de veículos |
| V2 | `V2__create_maintenances.sql` | Tabela de manutenções |
| V3 | `V3__create_diagnostics.sql` | Tabela de diagnósticos |
| V4 | `V4__create_warranties.sql` | Tabela de garantias |
| V5 | `V5__create_notifications.sql` | Tabela de notificações |
| V6 | `V6__create_users.sql` | Tabela de usuários |
| V7 | `V7__create_audit_logs.sql` | Tabela de auditoria |

---

## 🔗 Endpoints da API

### Vehicles `/api/vehicles`

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/vehicles` | Listar todos os veículos |
| GET | `/api/vehicles/{id}` | Buscar veículo por ID |
| GET | `/api/vehicles/plate/{plate}` | Buscar veículo por placa |
| POST | `/api/vehicles` | Cadastrar novo veículo |
| PUT | `/api/vehicles/{id}` | Atualizar veículo |
| DELETE | `/api/vehicles/{id}` | Remover veículo |

### Maintenances `/api/maintenances`

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/maintenances` | Listar todas as manutenções |
| GET | `/api/maintenances/{id}` | Buscar manutenção por ID |
| GET | `/api/maintenances/vehicle/{id}` | Histórico por veículo |
| GET | `/api/maintenances/recurring` | Manutenções recorrentes |
| POST | `/api/maintenances` | Registrar nova manutenção |
| PUT | `/api/maintenances/{id}` | Atualizar manutenção |
| DELETE | `/api/maintenances/{id}` | Remover manutenção |

### Diagnostics `/api/diagnostics`

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/diagnostics/vehicle/{id}` | Diagnóstico atual do veículo |
| GET | `/api/diagnostics/{id}` | Buscar diagnóstico por ID |
| GET | `/api/diagnostics/vehicle/{id}/alerts` | Alertas ativos (YELLOW e RED) |
| POST | `/api/diagnostics` | Registrar leitura de diagnóstico |
| DELETE | `/api/diagnostics/{id}` | Remover diagnóstico |

### Warranties `/api/warranties`

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/warranties/vehicle/{id}` | Garantia ativa do veículo |
| GET | `/api/warranties/{id}/valid` | Verificar se garantia é válida |
| POST | `/api/warranties` | Registrar garantia |
| PUT | `/api/warranties/{id}` | Atualizar garantia |

### Notifications `/api/notifications`

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/notifications/vehicle/{id}` | Notificações do veículo |
| POST | `/api/notifications/send` | Enviar notificação |
| PUT | `/api/notifications/{id}/read` | Marcar como lida |

### Dealers `/api/dealers`

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/dealers/nearby?city={city}` | Concessionárias próximas |
| GET | `/api/dealers/{id}/prices` | Preços de serviços |
| GET | `/api/dealers/{id}/schedule` | Disponibilidade de agendamento |

### Auth `/api/auth`

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/auth/login` | Login e geração de token JWT |
| POST | `/api/auth/register` | Registrar usuário (somente ADMIN) |

### Audit `/api/audit`

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/audit` | Listar logs (paginado, somente ADMIN) |
| GET | `/api/audit/user/{username}` | Logs por usuário |
| GET | `/api/audit/status/{status}` | Logs por status |

---

## ⚠️ Respostas de Erro

A API retorna respostas padronizadas sem expor detalhes internos:

```json
{
  "timestamp": "2026-05-18T20:00:00",
  "status": 404,
  "message": "Recurso não encontrado"
}
```

| Status | Situação |
|---|---|
| 200 | Sucesso |
| 201 | Recurso criado |
| 204 | Removido com sucesso |
| 400 | Erro de validação |
| 401 | Credenciais inválidas |
| 429 | Muitas requisições (rate limit) |
| 500 | Erro interno do servidor |

---

## 👥 Equipe

Desenvolvido como projeto acadêmico — Integração de Sistemas / Arquitetura Orientada a Serviços.
