package se.example.order.adapter.in.rest;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import se.example.order.application.port.in.PlaceOrderCommand;
import se.example.order.domain.model.Money;
import se.example.order.domain.model.Order;
import se.example.order.domain.model.OrderId;

import java.math.BigDecimal;

/**
 * DTO <-> command/domain mapping. The generated implementation is an @ApplicationScoped bean.
 * Nested types (LineRequest -> Line, OrderLine -> LineResponse) are mapped implicitly.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
interface OrderRestMapper {

    PlaceOrderCommand toCommand(PlaceOrderRequest request);

    OrderResponse toResponse(Order order);

    default Money toMoney(BigDecimal amount) {
        return amount == null ? null : new Money(amount);
    }

    default BigDecimal toAmount(Money money) {
        return money == null ? null : money.amount();
    }

    default Long toLong(OrderId id) {
        return id == null ? null : id.value();
    }
}
