package se.example.order.application.service;

import org.junit.jupiter.api.Test;
import se.example.order.application.OrderNotFoundException;
import se.example.order.application.port.in.PlaceOrderCommand;
import se.example.order.domain.model.DomainException;
import se.example.order.domain.model.Money;
import se.example.order.domain.model.Order;
import se.example.order.domain.model.OrderId;
import se.example.order.domain.model.OrderStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Plain JUnit, no Quarkus: wire the services by hand with fakes.
 */
class OrderServicesTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private final InMemoryOrderRepository repository = new InMemoryOrderRepository();
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private final PlaceOrderService placeOrder = new PlaceOrderService(repository, clock);
    private final GetOrderService getOrder = new GetOrderService(repository);
    private final CancelOrderService cancelOrder = new CancelOrderService(repository);

    @Test
    void places_and_stores_an_order() {
        OrderId id = placeOrder.placeOrder(command("p-1", 2, "10.00"));

        Order stored = getOrder.getOrder(id);
        assertEquals(OrderStatus.PLACED, stored.getStatus());
        assertEquals(NOW, stored.getCreatedAt());
        assertEquals(Money.of("20.00"), stored.getTotal());
    }

    @Test
    void rejects_an_order_without_lines_and_stores_nothing() {
        assertThrows(DomainException.class,
                () -> placeOrder.placeOrder(new PlaceOrderCommand("c-1", List.of())));
        assertEquals(0, repository.size());
    }

    @Test
    void cancels_an_order() {
        OrderId id = placeOrder.placeOrder(command("p-1", 1, "5"));

        cancelOrder.cancelOrder(id);

        assertEquals(OrderStatus.CANCELLED, getOrder.getOrder(id).getStatus());
    }

    @Test
    void unknown_order_is_reported() {
        assertThrows(OrderNotFoundException.class, () -> getOrder.getOrder(OrderId.newId()));
    }

    private static PlaceOrderCommand command(String productId, int quantity, String price) {
        return new PlaceOrderCommand("c-1",
                List.of(new PlaceOrderCommand.Line(productId, quantity, Money.of(price))));
    }
}
