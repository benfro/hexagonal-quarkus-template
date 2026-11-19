package se.example.order.domain.model;

public class OrderNotModifiableException extends DomainException {

    public OrderNotModifiableException(OrderId id, OrderStatus status) {
        super("Order %s cannot be modified in status %s".formatted(id, status));
    }
}
