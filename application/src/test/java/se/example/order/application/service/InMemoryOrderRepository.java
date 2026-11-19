package se.example.order.application.service;

import se.example.order.application.port.out.OrderRepository;
import se.example.order.domain.model.Order;
import se.example.order.domain.model.OrderId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Hand-written fake for the driven port. Usually nicer than mocks for repositories.
 */
class InMemoryOrderRepository implements OrderRepository {

    private final Map<OrderId, Order> store = new HashMap<>();

    @Override
    public Optional<Order> findById(OrderId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void save(Order order) {
        store.put(order.getId(), order);
    }

    int size() {
        return store.size();
    }
}
