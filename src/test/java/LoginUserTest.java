import client.UserClient;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import model.User;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static util.UserGenerator.generateRandomUser;

public class LoginUserTest {

    private final UserClient userClient = new UserClient();

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/api/auth";
    }

    @Test
    public void testLoginWithExistingUser() {
        // Сначала создаем пользователя
        User user = generateRandomUser();
        userClient.createUser(user);

        // Затем пытаемся залогиниться
        Response response = loginUser(user);
        assertSuccessfulLogin(response, user);
    }

    @Test
    public void testLoginWithInvalidCredentials() {
        // Создаем пользователя
        User user = generateRandomUser();
        userClient.createUser(user);

        // Пытаемся залогиниться с неверным паролем
        User invalidUser = new User(user.getEmail(), "wrongPassword", user.getName());
        Response response = loginUser(invalidUser);
        assertInvalidCredentials(response);
    }

    @Step("Login user with API")
    private Response loginUser(User user) {
        return given()
                .header("Content-type", "application/json")
                .body(user)
                .when()
                .post("/login");
    }

    @Step("Assert successful login")
    private void assertSuccessfulLogin(Response response, User expectedUser) {
        response.then()
                .assertThat()
                .statusCode(200);

        String actualEmail = response.path("user.email");
        assertEquals(expectedUser.getEmail(), actualEmail);
    }

    @Step("Assert invalid credentials")
    private void assertInvalidCredentials(Response response) {
        response.then()
                .assertThat()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }
}