package se.example.order.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    @Test
    void placed_order_has_correct_total() {
        Order order = Order.create("c-1", NOW);
        order.addLine(new OrderLine("p-1", 2, Money.of("100.50")));
        order.addLine(new OrderLine("p-2", 1, Money.of("49")));

        order.place();

        assertEquals(OrderStatus.PLACED, order.getStatus());
        assertEquals(Money.of("250.00"), order.getTotal());
    }

    @Test
    void empty_order_cannot_be_placed() {
        Order order = Order.create("c-1", NOW);

        assertThrows(DomainException.class, order::place);
    }

    @Test
    void lines_cannot_be_added_after_placing() {
        Order order = Order.create("c-1", NOW);
        order.addLine(new OrderLine("p-1", 1, Money.of("10")));
        order.place();

        assertThrows(OrderNotModifiableException.class,
                () -> order.addLine(new OrderLine("p-2", 1, Money.of("10"))));
    }

    @Test
    void cancelled_order_cannot_be_cancelled_again() {
        Order order = Order.create("c-1", NOW);
        order.cancel();

        assertThrows(OrderNotModifiableException.class, order::cancel);
    }

    @Test
    void lines_are_not_modifiable_from_outside() {
        Order order = Order.create("c-1", NOW);

        assertThrows(UnsupportedOperationException.class,
                () -> order.getLines().add(new OrderLine("p-1", 1, Money.of("1"))));
    }
}
