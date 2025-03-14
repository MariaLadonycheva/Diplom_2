import client.UserClient;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import model.Ingredients;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static util.UserGenerator.generateRandomUser;

public class CreateOrderTest {

    private static final String INGREDIENTS_ENDPOINT = "/ingredients";
    private static final String ORDERS_ENDPOINT = "/orders";
    private final UserClient userClient = new UserClient();
    private String accessToken;
    private User testUser;
    private List<String> validIngredients;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/api";

        // Создаем пользователя
        testUser = generateRandomUser();
        Response response = userClient.createUser(testUser);
        accessToken = response.path("accessToken");

        // Получаем список ингредиентов
        validIngredients = getValidIngredients();
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            userClient.deleteUser(accessToken);
        }
    }

    @Test
    public void testCreateOrderWithAuthorizationAndIngredients() {
        Response response = createOrder(validIngredients, accessToken);
        assertSuccessfulOrderCreation(response);
    }

    @Test
    public void testCreateOrderWithoutAuthorizationAndIngredients() {
        Response response = createOrder(validIngredients, null);
        assertSuccessfulOrderCreation(response);
    }

    @Test
    public void testCreateOrderWithAuthorizationAndNoIngredients() {
        Response response = createOrder(null, accessToken);
        assertBadRequest(response, "Ingredient ids must be provided");
    }

    @Test
    public void testCreateOrderWithoutAuthorizationAndNoIngredients() {
        Response response = createOrder(null, null);
        assertBadRequest(response, "Ingredient ids must be provided");
    }

    @Test
    public void testCreateOrderWithInvalidIngredients() {
        List<String> invalidIngredients = Arrays.asList("invalid_hash_1", "invalid_hash_2");
        Response response = createOrder(invalidIngredients, accessToken);
        assertInternalServerError(response);
    }

    @Step("Get valid ingredients")
    private List<String> getValidIngredients() {
        return given()
                .get(INGREDIENTS_ENDPOINT)
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("data._id", String.class);
    }

    @Step("Create order. Ingredients: {ingredients}, Token: {accessToken}")
    private Response createOrder(List<String> ingredients, String accessToken) {
        RequestSpecification request = given()
                .header("Content-type", "application/json");

        if (accessToken != null) {
            request = request.header("Authorization", accessToken);
        }

        // Передаем список ингредиентов напрямую в body
        request = request.body(new Ingredients(ingredients)).log().body();

        return request.when().post(ORDERS_ENDPOINT);
    }

    @Step("Assert successful order creation")
    private void assertSuccessfulOrderCreation(Response response) {
        response.then()
                .assertThat()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("name", notNullValue())
                .body("order.number", notNullValue());
    }

    @Step("Assert bad request. Message: {message}")
    private void assertBadRequest(Response response, String message) {
        response.then()
                .assertThat()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo(message));
    }

    @Step("Assert internal server error")
    private void assertInternalServerError(Response response) {
        response.then()
                .assertThat()
                .statusCode(500);
    }
}