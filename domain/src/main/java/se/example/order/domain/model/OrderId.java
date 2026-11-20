package se.example.order.domain.model;

import java.util.Objects;

public record OrderId(Long value) {

    public OrderId {
        Objects.requireNonNull(value, "value");
    }

    public static OrderId of(long value) {
        return new OrderId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
