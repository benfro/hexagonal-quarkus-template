package se.example.order.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import se.example.order.application.OrderNotFoundException;
import se.example.order.application.port.in.GetOrderUseCase;
import se.example.order.application.port.out.OrderRepository;
import se.example.order.domain.model.Order;
import se.example.order.domain.model.OrderId;

@ApplicationScoped
@RequiredArgsConstructor
class GetOrderService implements GetOrderUseCase {

    private final OrderRepository orders;

    @Override
    @Transactional
    public Order getOrder(OrderId id) {
        return orders.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }
}
