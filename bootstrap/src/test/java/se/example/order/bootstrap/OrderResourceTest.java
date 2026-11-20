package se.example.order.bootstrap;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import se.example.order.application.OrderNotFoundException;
import se.example.order.application.port.in.GetOrderUseCase;
import se.example.order.application.port.in.PlaceOrderUseCase;
import se.example.order.domain.model.OrderId;
import se.example.order.domain.model.OrderNotModifiableException;
import se.example.order.domain.model.OrderStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the REST adapter in isolation: in-ports are mocked, so only HTTP mapping,
 * validation and error translation are exercised.
 */
@QuarkusTest
class OrderResourceTest {

    @InjectMock
    PlaceOrderUseCase placeOrder;

    @InjectMock
    GetOrderUseCase getOrder;

    @Test
    void post_returns_201_with_location() {
        OrderId id = OrderId.of(1L);
        when(placeOrder.placeOrder(any())).thenReturn(id);

        given().contentType(ContentType.JSON)
                .body("""
                        {"customerId":"c-1","lines":[{"productId":"p-1","quantity":1,"unitPrice":10.00}]}
                        """)
                .when().post("/orders")
                .then().statusCode(201)
                .header("Location", endsWith("/orders/" + id));
    }

    @Test
    void invalid_request_is_rejected_before_reaching_the_use_case() {
        given().contentType(ContentType.JSON)
                .body("""
                        {"customerId":"","lines":[]}
                        """)
                .when().post("/orders")
                .then().statusCode(400);

        verifyNoInteractions(placeOrder);
    }

    @Test
    void unknown_order_gives_404() {
        OrderId id = OrderId.of(2L);
        when(getOrder.getOrder(any())).thenThrow(new OrderNotFoundException(id));

        given().when().get("/orders/" + id).then().statusCode(404);
    }

    @Test
    void domain_conflict_gives_409() {
        OrderId id = OrderId.of(3L);
        when(placeOrder.placeOrder(any())).thenThrow(new OrderNotModifiableException(id, OrderStatus.PLACED));

        given().contentType(ContentType.JSON)
                .body("""
                        {"customerId":"c-1","lines":[{"productId":"p-1","quantity":1,"unitPrice":10.00}]}
                        """)
                .when().post("/orders")
                .then().statusCode(409);
    }
}
