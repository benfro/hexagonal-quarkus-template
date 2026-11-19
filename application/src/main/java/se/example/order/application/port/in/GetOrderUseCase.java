package se.example.order.application.port.in;

import se.example.order.domain.model.Order;
import se.example.order.domain.model.OrderId;

public interface GetOrderUseCase {

    /**
     * @throws se.example.order.application.OrderNotFoundException if no order has the id
     */
    Order getOrder(OrderId id);
}
