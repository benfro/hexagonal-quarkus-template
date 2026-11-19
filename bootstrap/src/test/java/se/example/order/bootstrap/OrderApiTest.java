package se.example.order.bootstrap;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * End to end through all layers against PostgreSQL (Dev Services). Keep these few.
 */
@QuarkusTest
class OrderApiTest {

    @Test
    void place_get_and_cancel_an_order() {
        String location = given().contentType(ContentType.JSON)
                .body("""
                        {
                          "customerId": "c-42",
                          "lines": [
                            {"productId": "p-1", "quantity": 2, "unitPrice": 100.50},
                            {"productId": "p-2", "quantity": 1, "unitPrice": 49.00}
                          ]
                        }
                        """)
                .when().post("/orders")
                .then().statusCode(201)
                .extract().header("Location");

        JsonPath order = given().when().get(location)
                .then().statusCode(200)
                .body("status", equalTo("PLACED"))
                .body("lines", hasSize(2))
                .extract().jsonPath();
        assertEquals(0, new BigDecimal("250.00").compareTo(order.getObject("total", BigDecimal.class)));

        given().when().post(location + "/cancel").then().statusCode(204);
        given().when().get(location).then().statusCode(200).body("status", equalTo("CANCELLED"));

        // Domain rule surfaces as 409 through the exception mapper
        given().when().post(location + "/cancel").then().statusCode(409);
    }
}
