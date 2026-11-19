# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

A hexagonal-architecture Quarkus skeleton (groupId `se.example.order`) demonstrating Order placement/cancellation. Built with Quarkus, MapStruct, Lombok, and PostgreSQL (Flyway).

This repo is meant to be used as a template: `init.sh` renames the Maven coordinates and base package (see README.md, "Using this as a template"). If invoked here, the groupId/package is still the placeholder `se.example.order` and hasn't been customized for a real project yet — don't treat that as a mistake to fix.

## Commands

```bash
mvn install                                  # build + run all tests across modules
mvn -f bootstrap/pom.xml quarkus:dev         # dev mode; live reload covers all modules (needs Docker/Podman for Dev Services)
mvn -pl domain test                          # run tests for a single module (e.g. domain, application)
mvn -pl bootstrap test -Dtest=OrderApiTest   # run a single test class
```

Requires JDK 21+, Maven 3.9+, and Docker or Podman (Quarkus Dev Services starts PostgreSQL automatically for dev/test).

Production run: `java -jar bootstrap/target/quarkus-app/quarkus-run.jar` with `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` set.

## Architecture

Five Maven modules, dependencies point inward: `bootstrap -> adapters -> application -> domain`. Maven's module graph enforces this physically; `ArchitectureTest` (ArchUnit, in `bootstrap`) enforces the rest at test time — domain must stay framework-free, `application` may not depend on Quarkus/JPA/JAX-RS/MapStruct, and JPA stays confined to `adapter-out-persistence`.

- **domain** — `Order` aggregate root, value objects (`Money`, `OrderId`, `OrderLine`, `OrderStatus`), domain exceptions. Plain Java; Lombok is the only allowed dependency (compile-time only, via `provided` scope, so it's acceptable even here). No setters — all state changes go through behaviour methods (`place()`, `cancel()`, `addLine()`). Rebuilding an existing aggregate (e.g. from storage) uses `Order.reconstitute()...build()`, a private Lombok `@Builder`, distinct from the `create()` factory that runs creation invariants.
- **application** — `port/in` (use case interfaces: `PlaceOrderUseCase`, `GetOrderUseCase`, `CancelOrderUseCase`), `port/out` (`OrderRepository`), `service/` (use case implementations). Services are package-private — everything outside this module goes through the ports. Only Jakarta spec APIs are allowed here (`cdi-api`, `transaction-api`, scope `provided`) — never Quarkus directly. `@Transactional` lives on the services: the transaction boundary is the use case, and the persistence adapter joins it.
- **adapter-in-rest** — JAX-RS `OrderResource`, request/response DTOs, MapStruct mapper, and `OrderExceptionMappers` translating domain exceptions to HTTP responses.
- **adapter-out-persistence** — `OrderEntity` (JPA, never leaves this module), Panache repository, MapStruct mapper (converts to/from the domain aggregate via `reconstitute()`), `OrderRepositoryAdapter` implementing the `OrderRepository` port, and Flyway migrations (`db/migration/`). `OrderLine`s are an `@ElementCollection(EAGER)`, saved via `merge` — the aggregate is always loaded and persisted whole, never partially.
- **bootstrap** — the Quarkus runnable: `application.properties`, `ClockProducer` (injects `Clock` so use cases are deterministic in tests), and all integration/architecture tests.

### Cross-cutting conventions

- **Bean discovery**: `application` and both adapters each have an empty `META-INF/beans.xml` so Quarkus's Jandex indexing picks them up — without it, CDI injection fails with "Unsatisfied dependency."
- **Strict MapStruct mapping**: the parent POM sets `-Amapstruct.unmappedTargetPolicy=ERROR` — an unmapped target field is a compile error, not a silent drop.
- **Lombok + MapStruct**: `lombok-mapstruct-binding` must stay on the annotation processor path *between* Lombok and the MapStruct processor (order matters) for generated mappers to see Lombok-generated accessors/builders.
- **Time**: never call `Instant.now()`/`Clock.systemUTC()` directly in `application` or `domain` — take `Clock` as a parameter/dependency, following the existing services.

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

When adding a use case or changing the aggregate, expect to touch tests at multiple layers (domain unit test, application fake-repository test, persistence mapper test, and the REST/end-to-end tests in `bootstrap`) — the module boundaries are deliberately mirrored in the test pyramid.
