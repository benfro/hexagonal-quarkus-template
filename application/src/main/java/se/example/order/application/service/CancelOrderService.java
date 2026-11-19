package se.example.order.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import se.example.order.application.OrderNotFoundException;
import se.example.order.application.port.in.CancelOrderUseCase;
import se.example.order.application.port.out.OrderRepository;
import se.example.order.domain.model.Order;
import se.example.order.domain.model.OrderId;

@ApplicationScoped
@RequiredArgsConstructor
class CancelOrderService implements CancelOrderUseCase {

    private final OrderRepository orders;

    @Override
    @Transactional
    public void cancelOrder(OrderId id) {
        Order order = orders.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        order.cancel();
        orders.save(order);
    }
}
