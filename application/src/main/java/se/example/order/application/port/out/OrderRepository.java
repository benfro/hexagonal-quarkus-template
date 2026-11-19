package se.example.order.application.port.out;

import se.example.order.domain.model.Order;
import se.example.order.domain.model.OrderId;

import java.util.Optional;

/**
 * Driven port. Works on whole aggregates; there are no partial updates.
 */
public interface OrderRepository {

    Optional<Order> findById(OrderId id);

    void save(Order order);
}
