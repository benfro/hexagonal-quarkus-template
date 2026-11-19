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
public class OrderResource {

    private final PlaceOrderUseCase placeOrder;
    private final GetOrderUseCase getOrder;
    private final CancelOrderUseCase cancelOrder;
    private final OrderRestMapper mapper;

    @POST
    public Response place(@Valid @NotNull PlaceOrderRequest request) {
        OrderId id = placeOrder.placeOrder(mapper.toCommand(request));
        return Response.created(URI.create("/orders/" + id)).build();
    }

    @GET
    @Path("/{id}")
    public OrderResponse get(@PathParam("id") UUID id) {
        return mapper.toResponse(getOrder.getOrder(new OrderId(id)));
    }

    @POST
    @Path("/{id}/cancel")
    public Response cancel(@PathParam("id") UUID id) {
        cancelOrder.cancelOrder(new OrderId(id));
        return Response.noContent().build();
    }
}
