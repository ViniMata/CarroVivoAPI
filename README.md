# CarroVivoAPI

API REST desenvolvida para gerenciamento inteligente de veículos, manutenções, diagnósticos, garantias, concessionárias e notificações.

O projeto segue uma arquitetura baseada em microsserviços e fornece endpoints para monitoramento automotivo, histórico de manutenção e comunicação com usuários.

---

# Funcionalidades

* Cadastro e gerenciamento de veículos
* Controle de manutenções
* Diagnóstico automotivo
* Controle de garantias
* Busca de concessionárias
* Sistema de notificações
* Integração entre serviços
* Estrutura preparada para microsserviços

---

# Arquitetura

A API foi estruturada utilizando separação por serviços:

* `VehicleService`
* `MaintenanceService`
* `DiagnosticService`
* `WarrantyService`
* `DealerService`
* `NotificationService`

Fluxo principal:

```text
Cliente → API Gateway → Serviços → Banco de Dados
```

Possui suporte para:

* JWT Authentication
* Logs centralizados
* Cache Redis
* Mensageria (RabbitMQ/Kafka)
* Observabilidade
* Containerização com Docker

---

# Tecnologias Utilizadas

## Backend

* Java
* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Security
* JWT
* Maven

## Banco de Dados

* PostgreSQL
* Redis

## Infraestrutura

* Docker
* Kubernetes
* API Gateway
* RabbitMQ / Kafka

---

# Estrutura do Projeto

```text
src/
 ├── controller/
 ├── service/
 ├── repository/
 ├── model/
 ├── dto/
 ├── config/
 ├── security/
 └── exception/
```

---

# Como Executar o Projeto

## 1. Clonar o repositório

```bash
git clone <URL_DO_REPOSITORIO>
```

## 2. Entrar na pasta

```bash
cd CarroVivoAPI
```

## 3. Configurar variáveis de ambiente

Copie o arquivo `.env.example`:

```bash
cp .env.example .env
```

Configure:

* Banco de dados
* JWT Secret
* Redis
* Mensageria

---

## 4. Executar o projeto

### Maven

```bash
./mvnw spring-boot:run
```

ou

```bash
mvn spring-boot:run
```

---

# Autenticação

A API utiliza autenticação JWT Bearer Token.

Exemplo:

```http
Authorization: Bearer TOKEN
```

---

# Endpoints

# VehicleService

| Método | Endpoint                  | Descrição         |
| ------ | ------------------------- | ----------------- |
| GET    | `/vehicles`               | Listar veículos   |
| POST   | `/vehicles`               | Cadastrar veículo |
| GET    | `/vehicles/{id}`          | Buscar por ID     |
| PUT    | `/vehicles/{id}`          | Atualizar veículo |
| DELETE | `/vehicles/{id}`          | Remover veículo   |
| GET    | `/vehicles/plate/{plate}` | Buscar por placa  |

---

# MaintenanceService

| Método | Endpoint                     | Descrição            |
| ------ | ---------------------------- | -------------------- |
| GET    | `/maintenances`              | Listar manutenções   |
| POST   | `/maintenances`              | Registrar manutenção |
| GET    | `/maintenances/{id}`         | Buscar manutenção    |
| PUT    | `/maintenances/{id}`         | Atualizar manutenção |
| GET    | `/maintenances/vehicle/{id}` | Histórico do veículo |
| GET    | `/maintenances/recurring`    | Serviços recorrentes |

---

# DiagnosticService

| Método | Endpoint                    | Descrição         |
| ------ | --------------------------- | ----------------- |
| GET    | `/diagnostics/vehicle/{id}` | Diagnóstico atual |
| POST   | `/diagnostics`              | Registrar leitura |
| GET    | `/diagnostics/{id}/parts`   | Status das peças  |
| GET    | `/diagnostics/{id}/alerts`  | Alertas ativos    |

---

# WarrantyService

| Método | Endpoint                   | Descrição          |
| ------ | -------------------------- | ------------------ |
| GET    | `/warranties/vehicle/{id}` | Status da garantia |
| POST   | `/warranties`              | Registrar garantia |
| PUT    | `/warranties/{id}`         | Atualizar garantia |
| GET    | `/warranties/{id}/valid`   | Verificar validade |

---

# DealerService

| Método | Endpoint                 | Descrição                |
| ------ | ------------------------ | ------------------------ |
| GET    | `/dealers/nearby`        | Concessionárias próximas |
| GET    | `/dealers/{id}/prices`   | Preços por serviço       |
| GET    | `/dealers/{id}/schedule` | Disponibilidade          |

---

# NotificationService

| Método | Endpoint                      | Descrição               |
| ------ | ----------------------------- | ----------------------- |
| POST   | `/notifications/send`         | Enviar alerta           |
| GET    | `/notifications/vehicle/{id}` | Notificações do veículo |
| PUT    | `/notifications/{id}/read`    | Marcar como lida        |

---

# Observabilidade

O projeto possui suporte para:

* Prometheus
* Grafana
* ELK Stack
* OpenTelemetry

---

# Testes

Executar testes:

```bash
mvn test
```

---

# Docker

Build da aplicação:

```bash
docker build -t carrovivo-api .
```

Executar container:

```bash
docker run -p 8080:8080 carrovivo-api
```

---

# Autores

Fabiano RM: 555524
Lorran RM: 558982
Maria RM: 557478
Pedro RM: 556268
Vinícius RM: 555200