package se.example.order.application;

import se.example.order.domain.model.OrderId;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(OrderId id) {
        super("Order not found: " + id);
    }
}
