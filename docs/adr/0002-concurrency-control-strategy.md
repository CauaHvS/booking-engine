# ADR 0002 — Estratégia de Controle de Concorrência

**Data:** 01/07/2026
**Status:** Aceito
**Decisores:** Equipe de Arquitetura

---

## Contexto

O principal objetivo deste projeto é demonstrar que **N** tentativas simultâneas de reservar o mesmo `Slot` resultam em exatamente **uma reserva com status `CONFIRMED`** e **N-1 erros de conflito**, sem ocorrência de reservas duplicadas (*double-booking*).

Trata-se, propositalmente, de um cenário de alta contenção: o teste de concorrência dispara **N threads** (por exemplo, 100), todas tentando reservar o mesmo slot exatamente no mesmo instante, utilizando `CountDownLatch`.

A estratégia escolhida deve atender aos seguintes requisitos:

1. Garantir a corretude: nenhuma reserva duplicada, independentemente do nível de concorrência.
2. Produzir um sinal claro de falha para as threads que perderem a disputa, permitindo que a API retorne um erro significativo (HTTP **409 Conflict**) em vez de ocorrer corrupção silenciosa dos dados.
3. Ser facilmente demonstrável: o comportamento deve ficar evidente em um teste que um recrutador ou entrevistador consiga compreender em menos de cinco minutos.

Restrições adicionais definidas no ROADMAP:

* O projeto poderá implementar ambas as estratégias para fins de comparação (ROADMAP — Fatia 3).
* A arquitetura deve permitir trocar a estratégia sem modificar o domínio ou os casos de uso (garantido pelo isolamento por ports definido no ADR 0001).

---

## Opções analisadas

### Opção A — Locking Otimista (`@Version`)

**Mecanismo:** Uma coluna `version` é adicionada à tabela `slots`. Toda instrução `UPDATE` passa a incluir a cláusula `WHERE version = :expectedVersion`. Se duas transações lerem a mesma versão e ambas tentarem atualizar o registro, a segunda encontrará zero linhas afetadas e o JPA lançará uma `OptimisticLockException`.

**Como trata o cenário:**

* Thread 1 e Thread 2 leem o mesmo `Slot` com `version = 0` e status `AVAILABLE`.
* A Thread 1 realiza o commit:

```sql
UPDATE slots
SET status = 'RESERVED', version = 1
WHERE id = ? AND version = 0;
```

A atualização afeta uma linha e é concluída com sucesso.

* A Thread 2 executa o mesmo `UPDATE`, porém a versão agora é `1`. Nenhuma linha é atualizada e o JPA lança `OptimisticLockException`, que é capturada na camada de aplicação e convertida em `SlotNotAvailableException`.

#### Vantagens

* Não mantém bloqueios de linha no banco entre a leitura e a escrita. Em cenários de baixa contenção, oferece maior throughput porque as leituras não bloqueiam umas às outras.
* É o padrão mais conhecido no ecossistema JPA/Hibernate.
* `OptimisticLockException` é uma exceção padrão do JPA, simples de capturar e traduzir no `GlobalExceptionHandler`.
* A coluna `version` torna o mecanismo explícito e autoexplicativo no modelo JPA.

#### Desvantagens

* Em cenários de alta contenção (como o teste com 100 threads concorrendo pelo mesmo slot), praticamente todas as threads falharão com conflito. Esse comportamento é esperado, porém gera muitas falhas passíveis de nova tentativa (*retry*). Em sistemas que fazem repetição automática, isso pode causar o efeito conhecido como *thundering herd*.
* A `OptimisticLockException` é lançada apenas no momento do commit, e não durante a validação. Ou seja, a lógica de negócio já foi executada quando a aplicação descobre que perdeu a disputa. Neste projeto isso não representa um problema relevante, pois o processamento é leve.
* Exige cuidado no tratamento de exceções: o Spring encapsula `OptimisticLockException` em `ObjectOptimisticLockingFailureException`, sendo necessário capturar o tipo correto ou percorrer a cadeia de causas.

**Adequação ao projeto:** **ALTA.** Solução correta, amplamente utilizada e facilmente demonstrável.

---

### Opção B — Locking Pessimista (`SELECT FOR UPDATE`)

**Mecanismo:** A consulta `findByIdWithLock` adquire imediatamente um bloqueio exclusivo sobre a linha correspondente ao `Slot`. Outras transações que tentarem obter o mesmo bloqueio permanecerão aguardando até que a primeira transação seja concluída.

Exemplo utilizando Spring Data JPA:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM SlotJpaEntity s WHERE s.id = :id")
Optional<SlotJpaEntity> findByIdWithPessimisticLock(@Param("id") UUID id);
```

**Como trata o cenário:**

* A Thread 1 obtém o bloqueio, lê o status `AVAILABLE`, altera para `RESERVED` e realiza o commit. O bloqueio é liberado.
* A Thread 2 permaneceu bloqueada aguardando. Após a conclusão da Thread 1, ela obtém o bloqueio, lê o status `RESERVED` e o método `Slot.reserve()` lança `SlotNotAvailableException`. A transação é revertida.
* Não há necessidade de coluna de versão. O próprio banco de dados serializa o acesso à linha.

#### Vantagens

* O conflito é detectado no momento da leitura, e não apenas no commit. A transação perdedora descobre imediatamente que o recurso já foi reservado.
* Elimina o risco de *lost update*, mesmo que existam falhas sutis no código da aplicação, pois o banco garante exclusão mútua.
* Em cenários de alta contenção, as threads entram em fila e executam de forma sequencial, sem necessidade de novas tentativas.
* O comportamento é intuitivo: apenas uma thread manipula o recurso crítico por vez.

#### Desvantagens

* Mantém um bloqueio de linha durante toda a transação. Se a transação for lenta (chamadas externas, lógica complexa etc.), as demais threads permanecerão bloqueadas por mais tempo.
* Existe risco de *deadlock* caso múltiplas linhas sejam bloqueadas em ordens diferentes. Neste projeto o risco é reduzido, pois cada reserva bloqueia apenas um único `Slot`.
* É ligeiramente menos portátil, pois depende de mecanismos específicos do banco de dados. O PostgreSQL oferece excelente suporte.
* Em cenários de baixa contenção, possui maior custo que o locking otimista, pois mesmo leituras sem concorrência adquirem bloqueios.

**Adequação ao projeto:** **ALTA.** Inclusive, apresenta um comportamento mais simples de compreender no cenário de testes de concorrência.

---

### Opção C — Lock na camada da aplicação (`synchronized`, `ReentrantLock`, Redis SETNX)

**Mecanismo:** Serializar o acesso antes de chegar ao banco de dados.

#### Vantagens

* Não depende de recursos específicos do banco de dados.

#### Desvantagens

* Bloqueios em memória (`synchronized`) deixam de funcionar corretamente quando há mais de uma instância da aplicação. O projeto é executado em Docker e pode ser escalado facilmente.
* Redis utilizando `SETNX` é uma solução válida para bloqueios distribuídos, porém introduz uma nova infraestrutura para resolver um problema que o banco já resolve nativamente, caracterizando over-engineering.
* Não demonstra controle de concorrência em nível de banco de dados, que é justamente o objetivo principal deste projeto de portfólio.

**Adequação ao projeto:** **REJEITADA.** Ferramenta inadequada para este problema.

---

## Decisão

**Adotar Locking Otimista (`@Version`) como estratégia principal.**

### Justificativa

1. O padrão `@Version` é a solução idiomática do JPA para esse tipo de problema e a abordagem mais frequentemente discutida em entrevistas para vagas de desenvolvimento Java Backend.

2. Demonstra conhecimento do modelo de concorrência do JPA, e não apenas a capacidade de escrever consultas utilizando `SELECT FOR UPDATE`.

3. O modo de falha (`OptimisticLockException`) é explícito e facilmente tratável, permitindo que o teste de concorrência seja simples e objetivo: capturar a exceção, contabilizar as falhas e verificar que exatamente uma thread obteve sucesso.

4. A interface `SlotRepository` isola a estratégia utilizada. Uma implementação baseada em locking pessimista (`SlotPessimisticRepositoryAdapter`) pode ser adicionada posteriormente reutilizando o mesmo port, conforme previsto no ROADMAP, sem qualquer alteração nos casos de uso.

### Locking Pessimista como estratégia secundária (Fatia 3 — Opcional)

Após validar o funcionamento da estratégia otimista, será adicionado um `SlotPessimisticRepositoryAdapter` utilizando `@Lock(LockModeType.PESSIMISTIC_WRITE)`.

O mesmo teste de concorrência será executado novamente para comparar os comportamentos:

* **Locking Otimista:** maior throughput em baixa contenção, porém grande quantidade de conflitos em cenários altamente concorrentes (*thundering herd*).
* **Locking Pessimista:** serializa as threads, comporta-se melhor sob alta contenção, porém introduz bloqueios mesmo quando a concorrência é baixa.

Essa comparação representa o diferencial técnico do projeto.

---

## Contrato de Implementação

O port responsável por definir esse contrato é:

```java
// domain.port.out.SlotRepository
Slot findByIdWithLock(SlotId id); // lança SlotNotFoundException caso não exista
```

* O adapter otimista implementa esse método utilizando um `findById` simples (o controle ocorre automaticamente através de `@Version` no momento do commit).
* O adapter pessimista implementa o método utilizando `@Lock(PESSIMISTIC_WRITE)`.
* O caso de uso `CreateBookingService` utiliza apenas `findByIdWithLock`, sem conhecer qual estratégia está ativa.

### Cadeia de tradução de exceções (Locking Otimista)

```
JPA OptimisticLockException
        ↓
ObjectOptimisticLockingFailureException (Spring)
        ↓
GlobalExceptionHandler
        ↓
HTTP 409 Conflict
{
    "error": "SLOT_CONFLICT",
    "message": "..."
}
```

### Cadeia de tradução de exceções (Locking Pessimista)

```
SlotNotAvailableException
(lançada por Slot.reserve())
        ↓
GlobalExceptionHandler
        ↓
HTTP 409 Conflict
{
    "error": "SLOT_NOT_AVAILABLE",
    "message": "..."
}
```

---

## Consequências

* A tabela `slots` deverá possuir uma coluna:

```sql
version BIGINT NOT NULL DEFAULT 0
```

Essa coluna será adicionada por meio de uma migration Flyway quando o adapter otimista for implementado.

* `SlotJpaEntity` possuirá o atributo:

```java
@Version
private Long version;
```

* O `CreateBookingService` será anotado com `@Transactional`. A transação deverá permanecer aberta até que tanto a atualização do `Slot` quanto a inserção da `Booking` sejam efetivamente confirmadas. Encerrar a transação antes desse momento impediria a validação realizada pelo locking otimista.

* O teste de concorrência utilizará um `CountDownLatch(1)` como barreira de início e um `CountDownLatch(N)` como barreira de conclusão. Ao final, deverá validar que:

    * exatamente **uma** tentativa resultou em uma reserva criada com sucesso;
    * exatamente **N-1** tentativas retornaram erro de conflito;
    * ao consultar o slot após a execução, seu status é `RESERVED`.
