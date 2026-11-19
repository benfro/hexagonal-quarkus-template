package se.example.order.application.port.in;

import se.example.order.domain.model.OrderId;

public interface PlaceOrderUseCase {

    OrderId placeOrder(PlaceOrderCommand command);
}
