package se.example.order.adapter.out.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import se.example.order.application.port.out.OrderRepository;
import se.example.order.domain.model.Order;
import se.example.order.domain.model.OrderId;

import java.util.Optional;

/**
 * Implements the out-port. Joins the caller's transaction (the use case owns the boundary).
 */
@ApplicationScoped
@RequiredArgsConstructor
class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpa;
    private final OrderEntityMapper mapper;

    @Override
    public Optional<Order> findById(OrderId id) {
        return jpa.findByIdOptional(id.value()).map(mapper::toDomain);
    }

    @Override
    public OrderId save(Order order) {
        // merge = insert or update. Lines are replaced as a whole, matching aggregate semantics.
        // The generated id lands on the entity merge() returns, not the one passed in.
        OrderEntity merged = jpa.getEntityManager().merge(mapper.toEntity(order));
        return mapper.toOrderId(merged.getId());
    }
}
