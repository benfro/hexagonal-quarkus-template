package se.example.order.bootstrap;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import se.example.order.application.port.out.OrderRepository;
import se.example.order.domain.model.Money;
import se.example.order.domain.model.Order;
import se.example.order.domain.model.OrderId;
import se.example.order.domain.model.OrderLine;
import se.example.order.domain.model.OrderStatus;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the persistence adapter against a real PostgreSQL (Dev Services) through the port interface.
 */
@QuarkusTest
class OrderRepositoryAdapterTest {

    @Inject
    OrderRepository repository;

    @Inject
    EntityManager em;

    @Test
    @TestTransaction
    void saves_and_loads_the_whole_aggregate() {
        Order order = Order.create("c-1", Instant.parse("2026-01-01T10:00:00Z"));
        order.addLine(new OrderLine("p-1", 3, Money.of("10.00")));
        order.addLine(new OrderLine("p-2", 1, Money.of("5.50")));
        order.place();

        OrderId id = repository.save(order);
        em.flush();
        em.clear(); // force a real round trip to the database

        Order loaded = repository.findById(id).orElseThrow();
        assertEquals(order.getStatus(), loaded.getStatus());
        assertEquals(order.getLines(), loaded.getLines());
        assertEquals(Money.of("35.50"), loaded.getTotal());
        assertEquals(order.getCreatedAt(), loaded.getCreatedAt());
    }

    @Test
    @TestTransaction
    void updates_an_existing_aggregate() {
        Order order = Order.create("c-1", Instant.parse("2026-01-01T10:00:00Z"));
        order.addLine(new OrderLine("p-1", 1, Money.of("1")));
        order.place();
        OrderId id = repository.save(order);
        em.flush();
        em.clear();

        Order loaded = repository.findById(id).orElseThrow();
        loaded.cancel();
        repository.save(loaded);
        em.flush();
        em.clear();

        assertEquals(OrderStatus.CANCELLED, repository.findById(id).orElseThrow().getStatus());
    }
}
