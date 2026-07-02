# Booking Engine

![CI](https://github.com/CauaHvS/booking-engine/actions/workflows/ci.yml/badge.svg)

API REST de reservas com **controle de concorrência transacional** em Spring Boot + PostgreSQL.

O diferencial do projeto é provar que N threads simultâneas tentando reservar o mesmo slot resultam em **exatamente uma reserva confirmada** e N-1 conflitos — sem double-booking, sem corrupção de dados.

---

## Arquitetura

O projeto segue **Arquitetura Hexagonal (Ports & Adapters)**. O domínio é Java puro, sem dependências de framework. A estratégia de concorrência (locking otimista com `@Version`) está isolada no adapter de persistência — trocar por locking pessimista não toca no domínio nem nos use cases.

```
com.bookingengine
├── domain          → agregados (Resource, Slot, Booking), exceções, ports (interfaces)
├── application     → use cases (CreateBookingService, CancelBookingService, ...)
└── infrastructure  → adapters JPA, controllers REST, GlobalExceptionHandler
```

Diagrama detalhado: [`docs/architecture/architecture-diagram.md`](docs/architecture/architecture-diagram.md)

### Controle de concorrência — locking otimista

Cada linha na tabela `slots` tem uma coluna `version`. O UPDATE inclui `WHERE version = :expected`:

- **Thread vencedora:** `version=0 → version=1`, 1 linha atualizada, commit OK → `201 Created`
- **Thread perdedora:** `version=0`, mas já é `1` no banco → 0 linhas afetadas → `ObjectOptimisticLockingFailureException` → `409 Conflict`

O teste [`BookingConcurrencyTest`](src/test/java/com/bookingengine/concurrency/BookingConcurrencyTest.java) dispara 10 threads simultâneas e verifica as contagens.

Decisão documentada em: [`docs/adr/0002-concurrency-control-strategy.md`](docs/adr/0002-concurrency-control-strategy.md)

---

## Stack

| Camada | Tecnologia |
|--------|-----------|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.4 |
| Persistência | Spring Data JPA + Hibernate |
| Banco de dados | PostgreSQL 16 |
| Migrações | Flyway |
| Testes | JUnit 5, Mockito, Testcontainers 2 |
| Documentação | SpringDoc OpenAPI 2 (Swagger UI) |
| Empacotamento | Docker (multi-stage), Docker Compose |
| CI | GitHub Actions |

---

## Pré-requisitos

- Java 21+
- Docker Desktop (para rodar com Compose ou os testes de integração)

---

## Como executar

### Stack completa com Docker Compose

```bash
docker compose up -d --build
```

Aguarda o Postgres ficar saudável antes de subir a aplicação.

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`

### Desenvolvimento local (IDE + Docker só para infra)

```bash
docker compose up -d postgres   # sobe só o Postgres
./mvnw spring-boot:run          # sobe a aplicação localmente
```

### Testes

```bash
./mvnw clean verify             # todos os testes (55 ao todo)
```

Os testes de integração e o de concorrência sobem um container PostgreSQL via Testcontainers automaticamente.

---

## API

Documentação interativa disponível em `/swagger-ui.html` após subir a aplicação.

### Reservas

| Método | Endpoint | Descrição | Sucesso | Erros |
|--------|----------|-----------|---------|-------|
| `POST` | `/api/bookings` | Cria uma reserva | `201 Created` + `Location` | `404` slot não encontrado, `409` slot já reservado, `422` campos inválidos |
| `DELETE` | `/api/bookings/{id}` | Cancela uma reserva e libera o slot | `204 No Content` | `404` reserva não encontrada, `409` já cancelada |
| `GET` | `/api/bookings?userId={userId}` | Lista reservas de um usuário | `200 OK` | — |

### Slots

| Método | Endpoint | Descrição | Sucesso |
|--------|----------|-----------|---------|
| `GET` | `/api/slots?resourceId={resourceId}` | Lista slots disponíveis de um recurso | `200 OK` |

### Formato de erro

Todos os erros seguem **RFC 9457 (Problem Details)**:

```json
{
  "status": 409,
  "code": "slot_not_available",
  "title": "Slot não disponível",
  "detail": "slot abc-123 não está disponível para reserva (status atual: RESERVED)",
  "instance": "/api/bookings"
}
```

---

## Testes

| Categoria | Quantidade | O que cobre |
|-----------|-----------|-------------|
| Unit (domínio) | 18 | Invariantes de `Resource`, `Slot`, `Booking` |
| Unit (use cases) | 12 | `CreateBookingService`, `CancelBookingService` com mocks |
| `@WebMvcTest` | 5 | Controller + `GlobalExceptionHandler` sem DB |
| Integração (persistência) | 9 | Adapters JPA contra PostgreSQL real |
| Integração (HTTP) | 10 | Fluxos completos via `TestRestTemplate` |
| Concorrência | 1 | 10 threads simultâneas, 1 vence, 9 recebem 409 |
| **Total** | **55** | |

---

## Decisões de arquitetura (ADRs)

- [ADR 0001 — Arquitetura Hexagonal](docs/adr/0001-hexagonal-architecture.md)
- [ADR 0002 — Estratégia de Controle de Concorrência](docs/adr/0002-concurrency-control-strategy.md)
