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
