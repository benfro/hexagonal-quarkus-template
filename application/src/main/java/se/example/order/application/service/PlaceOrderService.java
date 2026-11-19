package se.example.order.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import se.example.order.application.port.in.PlaceOrderCommand;
import se.example.order.application.port.in.PlaceOrderUseCase;
import se.example.order.application.port.out.OrderRepository;
import se.example.order.domain.model.Order;
import se.example.order.domain.model.OrderId;
import se.example.order.domain.model.OrderLine;

import java.time.Clock;

/**
 * Package-private: callers depend on the port, never on the implementation.
 * The use case owns the transaction boundary.
 */
@ApplicationScoped
@RequiredArgsConstructor
class PlaceOrderService implements PlaceOrderUseCase {

    private final OrderRepository orders;
    private final Clock clock;

    @Override
    @Transactional
    public OrderId placeOrder(PlaceOrderCommand command) {
        Order order = Order.create(command.customerId(), clock.instant());
        command.lines().forEach(line ->
                order.addLine(new OrderLine(line.productId(), line.quantity(), line.unitPrice())));
        order.place();
        orders.save(order);
        return order.getId();
    }
}
