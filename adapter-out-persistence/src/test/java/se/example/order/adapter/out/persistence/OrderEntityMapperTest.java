package se.example.order.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import se.example.order.domain.model.Money;
import se.example.order.domain.model.Order;
import se.example.order.domain.model.OrderId;
import se.example.order.domain.model.OrderLine;
import se.example.order.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Mapper round trip without Quarkus or a database. The generated impl has a no-arg constructor.
 * Uses reconstitute() directly, since toEntity() on a brand new, unsaved Order.create() has a null
 * id by design - the database assigns one on save.
 */
class OrderEntityMapperTest {

    private final OrderEntityMapper mapper = new OrderEntityMapperImpl();

    @Test
    void round_trip_preserves_the_aggregate() {
        Order order = Order.reconstitute()
                .id(new OrderId(42L))
                .customerId("c-1")
                .lines(List.of(new OrderLine("p-1", 2, Money.of("12.50"))))
                .status(OrderStatus.PLACED)
                .createdAt(Instant.parse("2026-01-01T10:00:00Z"))
                .build();

        OrderEntity entity = mapper.toEntity(order);
        assertEquals(42L, entity.getId());
        assertEquals("PLACED", entity.getStatus());
        assertEquals(new BigDecimal("12.50"), entity.getLines().get(0).getUnitPrice());

        Order back = mapper.toDomain(entity);
        assertEquals(order, back);
        assertEquals(order.getStatus(), back.getStatus());
        assertEquals(order.getLines(), back.getLines());
        assertEquals(order.getCreatedAt(), back.getCreatedAt());
    }
}
