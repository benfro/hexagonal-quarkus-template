package se.example.order.adapter.in.rest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String customerId,
        String status,
        List<LineResponse> lines,
        BigDecimal total,
        Instant createdAt) {

    public record LineResponse(String productId, int quantity, BigDecimal unitPrice) {
    }
}
