# ADR 0001 — Arquitetura Hexagonal (Ports & Adapters)

**Data:** 01/07/2026
**Status:** Aceito
**Decisores:** Equipe de Arquitetura

---

## Contexto

O **booking-engine** é um projeto de portfólio cujo principal objetivo é demonstrar domínio sobre controle de concorrência em transações. O sistema deve comprovar que **N** tentativas simultâneas de reservar o mesmo slot resultam em exatamente **uma reserva bem-sucedida** e **N-1 erros de conflito**, sem qualquer corrupção de dados.

Diante desse objetivo, a arquitetura deve atender às seguintes restrições:

1. A estratégia de controle de concorrência (locking otimista versus pessimista) é uma decisão técnica central e pode precisar ser alterada ou comparada (o ROADMAP prevê explicitamente testar ambas as abordagens). O adaptador de persistência deve poder ser substituído sem impactar a lógica de negócio.

2. As regras de negócio (um slot não pode ser reservado duas vezes) devem ser testáveis sem a necessidade de iniciar um banco de dados. O teste de concorrência utiliza um banco real, porém os testes unitários das invariantes devem executar em milissegundos.

3. O projeto é um monólito. Não há necessidade de múltiplas unidades de implantação, Event Sourcing ou CQRS com bases separadas para leitura e escrita. Qualquer complexidade além do necessário para resolver o problema está explicitamente fora do escopo.

---

## Decisão

Adotar a **Arquitetura Hexagonal (Ports & Adapters)**, conforme proposta por Alistair Cockburn.

A arquitetura é organizada em três zonas concêntricas:

* **Domínio:** modelo de negócio em Java puro, sem dependências de frameworks. Contém os agregados `Resource`, `Slot` e `Booking`, suas invariantes e as interfaces dos ports.

* **Aplicação:** serviços de caso de uso responsáveis por orquestrar os objetos de domínio e definir os limites transacionais. Depende apenas da camada de domínio.

* **Infraestrutura:** todo o código dependente de frameworks (Spring, JPA, HTTP). Os adapters implementam os ports definidos no domínio e fazem a tradução entre os tipos do framework e os objetos do domínio.

A estrutura completa de pacotes e o inventário de classes estão definidos em:

`docs/architecture/package-structure.md`

---

## Alternativas consideradas

### Arquitetura em Camadas (Layered Architecture)

Os Controllers chamam Services, que chamam Repositories dentro de um único módulo utilizando anotações `@Service` e `@Repository`.

**Vantagens:**

* Familiar para a maioria dos desenvolvedores Java.
* Menor quantidade de código estrutural (sem ports e adapters).
* Desenvolvimento inicial mais rápido.

**Desvantagens:**

* As entidades JPA tendem a se propagar para as camadas de serviço e apresentação, dificultando a troca da estratégia de persistência sem modificar a lógica de negócio.
* As regras de negócio tornam-se difíceis de testar sem um contexto Spring ou um banco de dados em memória.
* A estratégia de controle de concorrência (a decisão técnica mais importante deste projeto) fica incorporada ao código dos serviços, em vez de isolada em um port de persistência. Comparar locking otimista e pessimista exige alterações espalhadas pelo código, em vez de apenas substituir um adapter.

**Conclusão:** rejeitada. A impossibilidade de isolar a estratégia de persistência foi o fator decisivo para não utilizar a arquitetura em camadas neste projeto.

---

### DDD Completo com Contextos Delimitados (Bounded Contexts)

Múltiplos módulos Spring, cada um contendo seu próprio agregado e comunicação entre eles por meio de mensagens.

**Vantagens:**

* Isolamento explícito entre contextos de negócio.
* Organização semelhante à utilizada em grandes sistemas corporativos.

**Desvantagens:**

* O domínio deste projeto representa apenas um único contexto de negócio (reserva de recursos). Dividi-lo em vários módulos adicionaria complexidade sem benefícios práticos.
* Utilizar Kafka ou outro broker para comunicação interna seria um claro caso de over-engineering.
* O documento `CLAUDE.md` proíbe explicitamente adicionar complexidade que o problema não exige.

**Conclusão:** rejeitada por representar uma complexidade desnecessária.

---

## Consequências

### Aspectos positivos

* O port `SlotRepository`, localizado em `domain.port.out`, define o método `findByIdWithLock`, cujo contrato é propositalmente independente da estratégia de concorrência. O adapter pessimista utiliza `SELECT FOR UPDATE`, enquanto o adapter otimista utiliza o mecanismo `@Version` do JPA. Ambos implementam a mesma interface. Dessa forma, o teste de concorrência pode ser executado contra ambas as estratégias sem qualquer alteração no caso de uso ou no domínio.

* As invariantes do domínio (por exemplo, `Slot.reserve()` lançando `SlotNotAvailableException` quando o slot já estiver reservado) podem ser testadas utilizando apenas Java puro, sem contexto Spring. Esses testes executam em menos de 1 ms cada.

* O `GlobalExceptionHandler`, localizado na camada de infraestrutura, é responsável por mapear exceções do domínio para respostas HTTP. A criação de um novo tipo de exceção não exige alterações nos controllers.

### Aspectos negativos (trade-offs aceitos)

* A Arquitetura Hexagonal gera uma quantidade maior de arquivos em comparação com a arquitetura em camadas. Para um projeto de portfólio desenvolvido individualmente, o ganho em clareza e separação de responsabilidades justifica esse custo.

* As classes responsáveis por converter entidades JPA para objetos de domínio representam código repetitivo (boilerplate). O projeto opta por mantê-las explícitas (sem utilizar MapStruct) para reduzir dependências e tornar a transformação de dados visível durante a revisão do código.

* Desenvolvedores que ainda não conhecem Arquitetura Hexagonal precisarão de uma breve introdução. O documento `package-structure.md` e este ADR cumprem esse papel.
