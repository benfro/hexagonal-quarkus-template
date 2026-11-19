package se.example.order.adapter.in.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

/**
 * Wire format. Syntactic validation happens here; business rules stay in the domain.
 */
public record PlaceOrderRequest(
        @NotBlank String customerId,
        @NotEmpty List<@Valid @NotNull LineRequest> lines) {

    public record LineRequest(
            @NotBlank String productId,
            @Positive int quantity,
            @NotNull @PositiveOrZero BigDecimal unitPrice) {
    }
}
