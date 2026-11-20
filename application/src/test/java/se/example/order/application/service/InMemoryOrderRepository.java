package se.example.order.application.service;

import se.example.order.application.port.out.OrderRepository;
import se.example.order.domain.model.Order;
import se.example.order.domain.model.OrderId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Hand-written fake for the driven port. Usually nicer than mocks for repositories.
 * Mimics a database identity column: assigns the next id on first save.
 */
class InMemoryOrderRepository implements OrderRepository {

    private final Map<OrderId, Order> store = new HashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Optional<Order> findById(OrderId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public OrderId save(Order order) {
        OrderId id = order.getId() != null ? order.getId() : OrderId.of(sequence.incrementAndGet());
        Order toStore = order.getId() != null ? order : Order.reconstitute()
                .id(id)
                .customerId(order.getCustomerId())
                .lines(order.getLines())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
        store.put(id, toStore);
        return id;
    }

    int size() {
        return store.size();
    }
}
