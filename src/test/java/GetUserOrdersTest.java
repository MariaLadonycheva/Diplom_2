import client.UserClient;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static util.UserGenerator.generateRandomUser;

public class GetUserOrdersTest {

    private static final String ORDERS_ENDPOINT = "/orders";
    private final UserClient userClient = new UserClient();
    private String accessToken;
    private User testUser;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/api";

        // Создаем пользователя
        testUser = generateRandomUser();
        Response response = userClient.createUser(testUser);
        accessToken = response.path("accessToken");
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            userClient.deleteUser(accessToken);
        }
    }

    @Test
    public void testGetUserOrdersWithAuthorization() {
        Response response = getUserOrders(accessToken);
        assertSuccessfulGetUserOrders(response);
    }

    @Test
    public void testGetUserOrdersWithoutAuthorization() {
        Response response = getUserOrders(null);
        assertUnauthorized(response);
    }

    @Step("Get user orders. Token: {accessToken}")
    private Response getUserOrders(String accessToken) {
        RequestSpecification request = given()
                .header("Content-type", "application/json");

        if (accessToken != null) {
            request = request.header("Authorization", accessToken);
        }

        return request.when().get(ORDERS_ENDPOINT);
    }

    @Step("Assert successful get user orders")
    private void assertSuccessfulGetUserOrders(Response response) {
        response.then()
                .assertThat()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders", notNullValue())
                .body("total", notNullValue())
                .body("totalToday", notNullValue());

        // Дополнительные проверки:
        List<Object> orders = response.path("orders");
        assertNotNull(orders);
        assertTrue(orders.size() <= 50);
    }

    @Step("Assert unauthorized")
    private void assertUnauthorized(Response response) {
        response.then()
                .assertThat()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
}