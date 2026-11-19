package se.example.order.domain.model;

import java.util.Objects;

public record OrderLine(String productId, int quantity, Money unitPrice) {

    public OrderLine {
        if (productId == null || productId.isBlank()) {
            throw new DomainException("productId is required");
        }
        if (quantity <= 0) {
            throw new DomainException("quantity must be positive, was " + quantity);
        }
        Objects.requireNonNull(unitPrice, "unitPrice");
    }

    public Money lineTotal() {
        return unitPrice.times(quantity);
    }
}
