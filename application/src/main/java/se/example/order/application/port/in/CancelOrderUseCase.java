package se.example.order.application.port.in;

import se.example.order.domain.model.OrderId;

public interface CancelOrderUseCase {

    void cancelOrder(OrderId id);
}
