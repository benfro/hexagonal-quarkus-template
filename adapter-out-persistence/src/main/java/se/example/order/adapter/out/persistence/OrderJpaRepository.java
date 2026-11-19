package se.example.order.adapter.out.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

/**
 * Panache stays inside the adapter. Repository style, not active record,
 * so the entity has no static persistence methods to leak.
 */
@ApplicationScoped
class OrderJpaRepository implements PanacheRepositoryBase<OrderEntity, UUID> {
}
