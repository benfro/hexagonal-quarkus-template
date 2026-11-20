package se.example.order.application.port.in;

import lombok.Builder;
import se.example.order.domain.model.Money;

import java.util.List;

@Builder
public record PlaceOrderCommand(String customerId, List<Line> lines) {

    public PlaceOrderCommand {
        lines = lines == null ? List.of() : List.copyOf(lines);
    }

    @Builder
    public record Line(String productId, int quantity, Money unitPrice) {
    }
}
