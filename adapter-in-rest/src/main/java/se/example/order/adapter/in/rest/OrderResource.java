package se.example.order.adapter.in.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import se.example.order.application.port.in.CancelOrderUseCase;
import se.example.order.application.port.in.GetOrderUseCase;
import se.example.order.application.port.in.PlaceOrderUseCase;
import se.example.order.domain.model.OrderId;

import java.net.URI;
import java.util.UUID;

/**
 * Thin: map the request, call the port, map the result. No business logic here.
 */
@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Tag(name = "Orders")
public class OrderResource {

    private final PlaceOrderUseCase placeOrder;
    private final GetOrderUseCase getOrder;
    private final CancelOrderUseCase cancelOrder;
    private final OrderRestMapper mapper;

    @POST
    @Operation(summary = "Place an order")
    @APIResponse(responseCode = "201", description = "Order placed; Location header points at it")
    public Response place(@Valid @NotNull PlaceOrderRequest request) {
        OrderId id = placeOrder.placeOrder(mapper.toCommand(request));
        return Response.created(URI.create("/orders/" + id)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get an order by id")
    @APIResponse(responseCode = "200", description = "Order found")
    @APIResponse(responseCode = "404", description = "No order with this id")
    public OrderResponse get(@PathParam("id") UUID id) {
        return mapper.toResponse(getOrder.getOrder(new OrderId(id)));
    }

    @POST
    @Path("/{id}/cancel")
    @Operation(summary = "Cancel an order")
    @APIResponse(responseCode = "204", description = "Order cancelled")
    @APIResponse(responseCode = "404", description = "No order with this id")
    public Response cancel(@PathParam("id") UUID id) {
        cancelOrder.cancelOrder(new OrderId(id));
        return Response.noContent().build();
    }
}
