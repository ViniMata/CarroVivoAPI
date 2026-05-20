# 🚗 Carro Vivo API

API REST para gerenciamento inteligente de veículos — diagnóstico de peças, histórico de manutenções, garantias, notificações e concessionárias próximas.

---

## 📋 Sobre o Projeto

O **Carro Vivo** é um sistema orientado a serviços (SOA) desenvolvido com **Java 21** e **Spring Boot 4.0.6**, seguindo os princípios de arquitetura REST e boas práticas de desenvolvimento. O sistema permite o gerenciamento completo do ciclo de vida de um veículo, desde o cadastro até o diagnóstico de peças e agendamento em concessionárias.

---

## 🏗️ Arquitetura

O projeto segue o padrão **SOA (Service-Oriented Architecture)** com separação clara entre três camadas:

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

Cada módulo é **independente e reutilizável**, comunicando-se apenas através de seus DTOs e interfaces de serviço.

---

## 🛠️ Tecnologias

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 21 | Linguagem principal |
| Spring Boot | 4.0.6 | Framework principal |
| Spring Web | — | APIs RESTful |
| Spring Data JPA | — | Persistência |
| Spring Security | — | Autenticação e autorização |
| PostgreSQL | 16 | Banco de dados |
| Flyway | 11 | Controle de migrações |
| Lombok | — | Redução de boilerplate |
| SpringDoc OpenAPI | 2.5.0 | Documentação Swagger |
| Docker | — | Containerização do banco |

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

### 2. Suba o banco de dados

```bash
docker-compose up -d
```

O arquivo `docker-compose.yml` na raiz do projeto sobe um container PostgreSQL 16 na porta `5432` com as credenciais configuradas.

### 3. Execute o projeto

```bash
./mvnw spring-boot:run
```

Ou pelo IntelliJ IDEA com `Shift + F10`.

### 4. Acesse a documentação

```
http://localhost:8080/swagger-ui.html
```

---

## 📦 Banco de Dados

A conexão é configurada no `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/carrovivo
    username: carrovivo
    password: carrovivo123
```

### Migrações (Flyway)

As migrações são executadas automaticamente ao iniciar a aplicação:

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

A documentação completa e interativa está disponível no Swagger UI. Abaixo um resumo dos principais endpoints:

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
| GET | `/api/maintenances/vehicle/{id}` | Histórico de manutenções do veículo |
| GET | `/api/maintenances/recurring` | Listar manutenções recorrentes |
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
| GET | `/api/warranties/vehicle/{id}` | Buscar garantia ativa do veículo |
| GET | `/api/warranties/{id}/valid` | Verificar se garantia está válida |
| POST | `/api/warranties` | Registrar garantia |
| PUT | `/api/warranties/{id}` | Atualizar garantia |

### Notifications `/api/notifications`

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/notifications/vehicle/{id}` | Listar notificações do veículo |
| POST | `/api/notifications/send` | Enviar notificação |
| PUT | `/api/notifications/{id}/read` | Marcar como lida |

### Dealers `/api/dealers`

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/dealers/nearby?city={city}` | Concessionárias próximas por cidade |
| GET | `/api/dealers/{id}/prices` | Preços de serviços da concessionária |
| GET | `/api/dealers/{id}/schedule` | Disponibilidade de agendamento |

---

## ⚠️ Tratamento de Erros

A API retorna respostas padronizadas para erros:

```json
{
  "timestamp": "2026-05-18T20:00:00",
  "status": 404,
  "message": "Veículo não encontrado com id: 1"
}
```

| Status | Situação |
|---|---|
| 200 | Sucesso |
| 201 | Recurso criado |
| 204 | Removido com sucesso |
| 400 | Erro de validação |
| 404 | Recurso não encontrado |
| 500 | Erro interno do servidor |

---

## 📄 Documentação Interativa

Acesse o Swagger UI para testar todos os endpoints diretamente pelo navegador:

```
http://localhost:8080/swagger-ui.html
```

A especificação OpenAPI em JSON está disponível em:

```
http://localhost:8080/api-docs
```

---

## 👥 Equipe

Desenvolvido como projeto acadêmico — Integração de Sistemas / Arquitetura Orientada a Serviços.# CarroVivoAPI
# CarroVivoAPI
# CarroVivoAPI
# CarroVivoAPI
