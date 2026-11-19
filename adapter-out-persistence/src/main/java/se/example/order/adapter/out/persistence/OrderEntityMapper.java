package se.example.order.adapter.out.persistence;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import se.example.order.domain.model.Money;
import se.example.order.domain.model.Order;
import se.example.order.domain.model.OrderId;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Domain <-> JPA mapping.
 * <ul>
 *   <li>toDomain builds the aggregate through Lombok's {@code Order.reconstitute()} builder, which the
 *       lombok-mapstruct-binding makes visible to MapStruct.</li>
 *   <li>OrderLine is a record, so MapStruct uses its canonical constructor.</li>
 *   <li>Enum to String and back for status is handled automatically.</li>
 * </ul>
 */
@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
interface OrderEntityMapper {

    OrderEntity toEntity(Order order);

    Order toDomain(OrderEntity entity);

    default UUID toUuid(OrderId id) {
        return id == null ? null : id.value();
    }

    default OrderId toOrderId(UUID value) {
        return value == null ? null : new OrderId(value);
    }

    default BigDecimal toAmount(Money money) {
        return money == null ? null : money.amount();
    }

    default Money toMoney(BigDecimal amount) {
        return amount == null ? null : new Money(amount);
    }
}
