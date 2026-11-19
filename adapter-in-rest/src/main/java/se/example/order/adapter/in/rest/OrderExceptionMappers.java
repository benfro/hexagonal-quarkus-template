package se.example.order.adapter.in.rest;

import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import se.example.order.application.OrderNotFoundException;
import se.example.order.domain.model.DomainException;
import se.example.order.domain.model.OrderNotModifiableException;

/**
 * Translates application and domain exceptions into HTTP. This is the only place that knows about status codes.
 * The most specific mapper wins, so OrderNotModifiableException gives 409, not 400.
 */
public class OrderExceptionMappers {

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> notFound(OrderNotFoundException e) {
        return RestResponse.status(Response.Status.NOT_FOUND, new ErrorResponse(e.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> notModifiable(OrderNotModifiableException e) {
        return RestResponse.status(Response.Status.CONFLICT, new ErrorResponse(e.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> ruleViolation(DomainException e) {
        return RestResponse.status(Response.Status.BAD_REQUEST, new ErrorResponse(e.getMessage()));
    }
}
