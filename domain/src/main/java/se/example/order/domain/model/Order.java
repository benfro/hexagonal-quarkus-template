package se.example.order.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate root. All state changes go through behaviour methods, so there are no setters.
 */
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Order {

    @EqualsAndHashCode.Include
    private final OrderId id;
    private final String customerId;
    private final Instant createdAt;
    @Getter(AccessLevel.NONE)
    private final List<OrderLine> lines;
    private OrderStatus status;

    /**
     * Rebuilds an existing aggregate, e.g. from the database. Creation rules are not re-run.
     * Exposed as {@code Order.reconstitute()...build()}; MapStruct picks this builder up automatically.
     */
    @Builder(builderMethodName = "reconstitute")
    private Order(OrderId id, String customerId, List<OrderLine> lines, OrderStatus status, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.customerId = Objects.requireNonNull(customerId, "customerId");
        this.lines = new ArrayList<>(lines == null ? List.of() : lines);
        this.status = Objects.requireNonNull(status, "status");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public static Order create(String customerId, Instant now) {
        if (customerId == null || customerId.isBlank()) {
            throw new DomainException("customerId is required");
        }
        return new Order(OrderId.newId(), customerId, List.of(), OrderStatus.DRAFT, now);
    }

    public void addLine(OrderLine line) {
        requireStatus(OrderStatus.DRAFT);
        lines.add(Objects.requireNonNull(line, "line"));
    }

    public void place() {
        requireStatus(OrderStatus.DRAFT);
        if (lines.isEmpty()) {
            throw new DomainException("An order needs at least one line");
        }
        status = OrderStatus.PLACED;
    }

    public void cancel() {
        if (status == OrderStatus.CANCELLED) {
            throw new OrderNotModifiableException(id, status);
        }
        status = OrderStatus.CANCELLED;
    }

    public List<OrderLine> getLines() {
        return List.copyOf(lines);
    }

    public Money getTotal() {
        return lines.stream()
                .map(OrderLine::lineTotal)
                .reduce(Money.ZERO, Money::plus);
    }

    private void requireStatus(OrderStatus expected) {
        if (status != expected) {
            throw new OrderNotModifiableException(id, status);
        }
    }
}
