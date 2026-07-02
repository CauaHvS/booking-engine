# Estrutura de Pacotes — Booking Engine

## Pacote raiz: `com.bookingengine`

O projeto segue a **Arquitetura Hexagonal (Ports & Adapters)**.

A regra de dependência é absoluta:

* Os adapters dependem da camada de aplicação.
* A camada de aplicação depende da camada de domínio.
* A camada de domínio não depende de nenhuma camada externa.

```text
com.bookingengine
├── domain
│   ├── model
│   ├── exception
│   └── port
│       ├── in
│       └── out
├── application
│   └── usecase
└── infrastructure
    ├── web
    │   ├── controller
    │   └── dto
    ├── persistence
    │   ├── entity
    │   ├── repository
    │   └── mapper
    └── config
```

---

# Camada: `domain`

É o núcleo da arquitetura (o hexágono interno). Nenhuma dependência de framework é permitida nesta camada. Apenas Java puro.

## `domain.model`

Objetos centrais do negócio com suas invariantes garantidas.

| Classe          | Tipo                | Responsabilidade                                                                                                  |
| --------------- | ------------------- | ----------------------------------------------------------------------------------------------------------------- |
| `Resource`      | Aggregate Root      | Representa um recurso que pode ser reservado (por exemplo, uma mesa ou uma sala). É responsável pelos seus slots. |
| `Slot`          | Entity              | Representa um intervalo de tempo pertencente a um recurso que pode ser reservado (início, fim e status).          |
| `Booking`       | Aggregate Root      | Relaciona um Slot a um usuário. Mantém o status da reserva.                                                       |
| `BookingStatus` | Value Object (enum) | `CONFIRMED`, `CANCELLED`.                                                                                         |
| `SlotStatus`    | Value Object (enum) | `AVAILABLE`, `RESERVED`.                                                                                          |
| `ResourceId`    | Value Object        | Identificador tipado de `Resource` (encapsula um UUID).                                                           |
| `SlotId`        | Value Object        | Identificador tipado de `Slot` (encapsula um UUID).                                                               |
| `BookingId`     | Value Object        | Identificador tipado de `Booking` (encapsula um UUID).                                                            |

### Exemplos de invariantes do domínio

* Um `Slot` não pode ser reservado caso seu status já seja `RESERVED`.
* Uma `Booking` não pode ser cancelada caso já esteja `CANCELLED`.
* O horário de término de um slot deve ser posterior ao horário de início.

---

## `domain.exception`

Exceções tipadas que representam violações das regras de negócio.

Nenhum conceito relacionado a HTTP deve existir nesta camada.

| Classe                      | Quando é lançada                                        |
| --------------------------- | ------------------------------------------------------- |
| `SlotNotAvailableException` | Tentativa de reservar um slot que não está `AVAILABLE`. |
| `BookingNotFoundException`  | Busca por uma reserva inexistente.                      |
| `SlotNotFoundException`     | Busca por um slot inexistente.                          |
| `ResourceNotFoundException` | Busca por um recurso inexistente.                       |

---

## `domain.port.in`

Ports de entrada (*Driving Ports*).

Representam os casos de uso que podem ser acionados pelo mundo externo.

São definidos como interfaces Java para que a camada de aplicação as implemente e os adapters (como REST) apenas as consumam.

| Interface              | Método(s)                                       | Descrição                                 |
| ---------------------- | ----------------------------------------------- | ----------------------------------------- |
| `CreateBookingUseCase` | `BookingResult create(CreateBookingCommand)`    | Cria uma reserva para um slot.            |
| `CancelBookingUseCase` | `void cancel(CancelBookingCommand)`             | Cancela uma reserva existente.            |
| `QueryBookingsUseCase` | `List<BookingResult> findByUser(String userId)` | Lista as reservas de um usuário.          |
| `QuerySlotsUseCase`    | `List<SlotResult> findAvailable(ResourceId)`    | Lista os slots disponíveis de um recurso. |

Os comandos também são definidos nesta camada como records imutáveis:

* `CreateBookingCommand(SlotId slotId, String userId)`
* `CancelBookingCommand(BookingId bookingId, String userId)`

Os tipos de retorno (`BookingResult` e `SlotResult`) também são records simples.

Eles representam o resultado devolvido pelo caso de uso e posteriormente convertido pelo adapter web em JSON. Não possuem comportamento de domínio.

---

## `domain.port.out`

Ports de saída (*Driven Ports*).

Representam tudo aquilo que a aplicação precisa do mundo externo (persistência, por exemplo).

São definidos como interfaces implementadas pelos adapters da infraestrutura.

| Interface            | Método(s)                                                                         | Descrição                                                                                                                                                                                                                             |
| -------------------- | --------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `BookingRepository`  | `save(Booking)`, `findById(BookingId)`, `findByUserId(String)`                    | Persistência do agregado `Booking`.                                                                                                                                                                                                   |
| `SlotRepository`     | `findByIdWithLock(SlotId)`, `findAvailableByResourceId(ResourceId)`, `save(Slot)` | Persistência do agregado `Slot`. O método `findByIdWithLock` é o ponto crítico do controle de concorrência. Cada implementação decide a estratégia utilizada (locking otimista com `@Version` ou pessimista com `SELECT FOR UPDATE`). |
| `ResourceRepository` | `findById(ResourceId)`                                                            | Persistência do agregado `Resource`.                                                                                                                                                                                                  |

---

# Camada: `application`

Orquestra os objetos de domínio para executar um caso de uso.

Também define os limites transacionais (`@Transactional`).

Depende apenas do domínio (modelos e ports).

Nenhuma anotação de framework é utilizada além de `@Service` e `@Transactional`.

## `application.usecase`

| Classe                 | Implementa             | Observações                                                                                                                                    |
| ---------------------- | ---------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| `CreateBookingService` | `CreateBookingUseCase` | Chama `SlotRepository.findByIdWithLock`, verifica disponibilidade, cria a `Booking` e persiste ambos. O limite da transação está nesta classe. |
| `CancelBookingService` | `CancelBookingUseCase` | Carrega a reserva, executa a lógica do domínio e persiste as alterações.                                                                       |
| `QueryBookingsService` | `QueryBookingsUseCase` | Caso de uso somente leitura. Não necessita locking.                                                                                            |
| `QuerySlotsService`    | `QuerySlotsUseCase`    | Caso de uso somente leitura.                                                                                                                   |

---

# Camada: `infrastructure`

Todo o código dependente de frameworks fica nesta camada.

Os adapters fazem a tradução entre o mundo externo e os contratos definidos pelas camadas internas.

---

## `infrastructure.web.controller`

| Classe              | Responsabilidade                                                                                                 |
| ------------------- | ---------------------------------------------------------------------------------------------------------------- |
| `BookingController` | Endpoints REST para criação e cancelamento de reservas. Utiliza `CreateBookingUseCase` e `CancelBookingUseCase`. |
| `SlotController`    | Endpoint REST responsável por listar os slots disponíveis. Utiliza `QuerySlotsUseCase`.                          |

Nenhuma regra de negócio deve existir nos controllers.

Eles apenas:

* recebem requisições HTTP;
* convertem para Commands;
* executam um port de entrada;
* convertem o resultado em resposta HTTP.

---

## `infrastructure.web.dto`

Objetos de transferência de dados (DTOs) utilizados exclusivamente pela camada HTTP.

São independentes dos objetos do domínio para permitir evolução do contrato da API sem impactar as regras de negócio.

| Classe                 | Direção                     |
| ---------------------- | --------------------------- |
| `CreateBookingRequest` | Corpo da requisição         |
| `CancelBookingRequest` | Corpo da requisição         |
| `BookingResponse`      | Corpo da resposta           |
| `SlotResponse`         | Corpo da resposta           |
| `ErrorResponse`        | Estrutura padrão para erros |

---

## `infrastructure.persistence.entity`

Entidades gerenciadas pelo JPA.

Nunca devem ser expostas à camada de domínio.

| Classe              | Tabela                                                                                   |
| ------------------- | ---------------------------------------------------------------------------------------- |
| `ResourceJpaEntity` | `resources`                                                                              |
| `SlotJpaEntity`     | `slots`. Contém a coluna `@Version` quando a estratégia de locking otimista é utilizada. |
| `BookingJpaEntity`  | `bookings`                                                                               |

---

## `infrastructure.persistence.repository`

Interfaces do Spring Data JPA e seus respectivos adapters.

| Classe                      | Responsabilidade                                                                                                              |
| --------------------------- | ----------------------------------------------------------------------------------------------------------------------------- |
| `SlotJpaRepository`         | Interface do Spring Data. Implementa `findByIdWithPessimisticLock` ou utiliza `@Version`, dependendo da estratégia escolhida. |
| `BookingJpaRepository`      | Interface do Spring Data.                                                                                                     |
| `ResourceJpaRepository`     | Interface do Spring Data.                                                                                                     |
| `SlotRepositoryAdapter`     | Implementa `domain.port.out.SlotRepository`. Realiza a conversão entre entidades JPA e objetos de domínio.                    |
| `BookingRepositoryAdapter`  | Implementa `domain.port.out.BookingRepository`.                                                                               |
| `ResourceRepositoryAdapter` | Implementa `domain.port.out.ResourceRepository`.                                                                              |

---

## `infrastructure.persistence.mapper`

Mappers sem estado responsáveis por converter entidades JPA em objetos de domínio e vice-versa.

Não possuem nenhuma lógica além do mapeamento de atributos.

| Classe           | Conversão                        |
| ---------------- | -------------------------------- |
| `SlotMapper`     | `SlotJpaEntity` ↔ `Slot`         |
| `BookingMapper`  | `BookingJpaEntity` ↔ `Booking`   |
| `ResourceMapper` | `ResourceJpaEntity` ↔ `Resource` |

---

## `infrastructure.config`

Classes de configuração do Spring e tratamento global de exceções.

| Classe                     | Responsabilidade                                                                                     |
| -------------------------- | ---------------------------------------------------------------------------------------------------- |
| `BookingEngineApplication` | Classe principal da aplicação (`@SpringBootApplication`).                                            |
| `GlobalExceptionHandler`   | `@RestControllerAdvice` responsável por converter exceções do domínio em respostas HTTP apropriadas. |

---

# Resumo do fluxo de dependências

```text
HTTP Request
    |
    v
[BookingController]          infrastructure.web
    |
    | chama uma interface de port.in
    v
[CreateBookingService]       application.usecase
    |
    | utiliza o modelo de domínio
    | chama uma interface de port.out
    v
[SlotRepositoryAdapter]      infrastructure.persistence
    |
    v
[SlotJpaRepository / DB]     PostgreSQL via JPA
```

As dependências nunca são invertidas.

* O modelo de domínio nunca é importado diretamente pela camada `infrastructure.web`.
* Pacotes da camada `infrastructure` não devem depender diretamente uns dos outros.
* Toda comunicação deve ocorrer por meio dos ports definidos no domínio.
