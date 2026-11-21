# order-service: hexagonal Quarkus skeleton

Ports and adapters with Quarkus, MapStruct, Lombok and PostgreSQL (Flyway).

```
domain/                   Order aggregate, value objects, rules. Plain Java (+ Lombok, compile time only)
application/              port/in (use cases), port/out (OrderRepository), service/ (use case impls, @Transactional)
adapter-in-rest/          JAX-RS resource, request/response DTOs, MapStruct mapper, exception -> HTTP mapping
adapter-out-persistence/  JPA entity, Panache repository, MapStruct mapper, port implementation, Flyway migrations
bootstrap/                Quarkus plugin, application.properties, producers (Clock), @QuarkusTest + ArchUnit
```

Dependencies point inward: `bootstrap -> adapters -> application -> domain`. Maven enforces the module
direction; `ArchitectureTest` enforces the rest (no JPA or Quarkus in the core, adapters independent of each other).

## Architecture, at helicopter level: ports and adapters

This is an implementation of **Ports and Adapters** (also called **Hexagonal Architecture**), the pattern
behind what Robert C. Martin popularized as **Clean Architecture**. The idea in one sentence: business rules
should not know that a framework, database, or network protocol exists.

- **Domain + application are the "inside."** They contain the business rules (`Order`, its invariants, the
  use cases that operate on it) and nothing else — no Quarkus, no JPA, no JAX-RS. They don't know or care
  whether they're being driven by a REST call, a message queue, or a test.
- **A port is an interface owned by the inside**, describing a capability it needs or offers, without saying
  how. `port/in` (e.g. `PlaceOrderUseCase`) is "what the outside can ask the inside to do." `port/out` (e.g.
  `OrderRepository`) is "what the inside needs from the outside," like persistence.
- **An adapter is a concrete implementation living on the outside** that plugs into a port.
  `adapter-in-rest` adapts incoming HTTP requests into calls on a `port/in` interface. `adapter-out-persistence`
  adapts a `port/out` interface into actual SQL/JPA calls. Adapters depend on the ports; the ports (and
  everything inside them) never depend on the adapters.
- **Why bother:** you can swap REST for gRPC, or Postgres for an in-memory fake (see `OrderServicesTest`'s
  `InMemoryOrderRepository`), by writing a new adapter — the domain and use cases don't change, and don't
  even need recompiling in a different module.

Background reading, roughly in the order it helps to read them:

- Alistair Cockburn, [*Hexagonal Architecture*](https://alistair.cockburn.us/hexagonal-architecture/) — the
  original "ports and adapters" write-up that gives the pattern its name.
- Robert C. Martin, [*The Clean Architecture*](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) —
  the "dependencies only point inward, towards business rules" framing most people mean by "clean architecture."
- Quarkus's own [*Getting Started*](https://quarkus.io/guides/getting-started) guide is useful background
  for how the framework expects CDI beans and resources to be wired, which is exactly what this skeleton
  keeps at arm's length from `domain`/`application`.

## Using this as a template

This repo is meant to be cloned and renamed, not forked as-is. Two steps:

1. **Rename the Maven coordinates and base package.** Run `./init.sh <new-group-id> <new-artifact-prefix>`,
   e.g. `./init.sh com.acme.shop shop`. It rewrites the groupId/package (`se.example.order`), the module
   artifactIds (`order-domain`, `order-application`, ...) and `quarkus.application.name`, and moves the
   `src/.../se/example/order` directories to match. Use `--dry-run` first to preview. Commit your clone to
   git before running it (`git init && git add -A && git commit -m init`) — it edits files and moves
   directories in place with no built-in undo.
2. **Replace the sample `Order` domain.** The script above only touches coordinates/package — it
   deliberately does not rename the `Order`/`OrderLine`/`orders` business logic, since that's the example
   you're meant to replace with your own aggregate, use cases, REST resource, entity and migration. Use the
   existing `Order` slice (one class/test per module) as the template for the shape each layer should take.

If you're hosting this on GitHub, you can also mark the repo as a "Template repository" in Settings so
`git clone` is replaced by the green "Use this template" button.

## Run

Requires JDK 21+, Maven 3.9+, and Docker or Podman for Dev Services (PostgreSQL starts automatically in dev/test).

```bash
mvn install                                  # build + all tests
mvn -f bootstrap/pom.xml quarkus:dev         # dev mode; live reload also covers the other modules
```

```bash
curl -i -X POST localhost:8080/orders -H 'Content-Type: application/json' \
  -d '{"customerId":"c-1","lines":[{"productId":"p-1","quantity":2,"unitPrice":99.50}]}'
curl localhost:8080/orders/<id>
curl -X POST localhost:8080/orders/<id>/cancel
```

OpenAPI document at `GET /q/openapi`; Swagger UI at `/q/swagger-ui` (dev/test only by default).

Production: `java -jar bootstrap/target/quarkus-app/quarkus-run.jar` with `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` set.

## Adding a new REST endpoint

Working outside-in through the layers for a new endpoint:

1. **Port + use case** (`application`): add or extend a `port/in` interface (e.g. `PlaceOrderUseCase`) and
   implement it as a package-private service under `service/`, annotated `@Transactional` if it writes.
2. **REST resource** (`adapter-in-rest`): add a method to `OrderResource` (or a new JAX-RS resource class),
   plus request/response DTOs and a MapStruct mapper method, following the existing `OrderResource` as the
   template. Map any new domain exception in `OrderExceptionMappers`.
3. **Wire it up and try it**: `mvn -f bootstrap/pom.xml quarkus:dev`, then hit it with `curl` or via
   Swagger UI at `/q/swagger-ui`.

If you haven't worked with Quarkus before, these guides cover the mechanics above in more depth:

- [Quarkus Getting Started](https://quarkus.io/guides/getting-started) — project layout, dev mode, the
  overall CDI-based programming model.
- [Writing JSON REST Services](https://quarkus.io/guides/rest-json) — JAX-RS resources that consume/produce
  JSON, which is what `OrderResource` does.
- [Quarkus REST](https://quarkus.io/guides/rest) — the full reference for request/response handling, path
  and query parameters, content negotiation, etc.
- [Validation](https://quarkus.io/guides/validation) — Bean Validation (`@NotNull`, `@Valid`, ...) on
  request DTOs.
- [OpenAPI and Swagger UI](https://quarkus.io/guides/openapi-swaggerui) — how the `/q/openapi` document and
  `/q/swagger-ui` endpoint (already enabled in this project) are generated and customized.
- [Getting Started Testing](https://quarkus.io/guides/getting-started-testing) — `@QuarkusTest`,
  `@InjectMock`, and `RestAssured`, as used in `OrderResourceTest` and `OrderApiTest`.

## Design decisions worth knowing

- **Bean discovery across modules.** `application` and both adapters contain an empty
  `META-INF/beans.xml`, so Quarkus indexes them. Without it you get "Unsatisfied dependency".
  (`jandex-maven-plugin` or `quarkus.index-dependency.*` are alternatives.)
- **CDI in the application layer.** The pragmatic choice: only Jakarta spec APIs (`cdi-api`,
  `transaction-api`, scope `provided`), never Quarkus. Use case services are package-private,
  so everything goes through the ports.
- **Transaction boundary = use case.** `@Transactional` sits on the services. The repository adapter joins.
- **Domain vs. persistence model.** `OrderEntity` never leaves the adapter. `Order` has no setters and is
  rebuilt with Lombok's `@Builder(builderMethodName = "reconstitute")`, which MapStruct uses
  automatically. `lombok-mapstruct-binding` in the processor path is what makes Lombok and MapStruct cooperate.
- **Strict mapping.** `-Amapstruct.unmappedTargetPolicy=ERROR`: a new field that isn't mapped breaks the build.
- **Aggregate loading.** Lines are an `@ElementCollection(EAGER)` and saved with `merge`, so the aggregate is
  always loaded and stored whole.
- **Time.** `Clock` is produced in bootstrap and injected, so the use cases are deterministic in tests.

## Testing pyramid

| Module | Test | Needs |
|---|---|---|
| domain | `OrderTest` | nothing |
| application | `OrderServicesTest` with `InMemoryOrderRepository` fake | nothing |
| adapter-out-persistence | `OrderEntityMapperTest` | nothing |
| bootstrap | `OrderResourceTest` (`@InjectMock` on in-ports) | Docker |
| bootstrap | `OrderRepositoryAdapterTest` (real PostgreSQL) | Docker |
| bootstrap | `OrderApiTest` (end to end) | Docker |
| bootstrap | `ArchitectureTest` | nothing |

## Natural next steps

- Optimistic locking: add `version` to `Order` (and `@Version` on the entity) so concurrent updates fail cleanly.
- An outbound messaging adapter (`OrderEventPublisher` port + `adapter-out-messaging` with Kafka/AMQP),
  ideally with an outbox table.
