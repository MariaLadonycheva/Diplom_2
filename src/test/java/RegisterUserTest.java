import client.UserClient;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static util.UserGenerator.generateRandomUser;

public class RegisterUserTest {

    private final UserClient userClient = new UserClient();
    private String accessToken;
    private User testUser;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/api/auth";
    }

    @Test
    public void testCreateUniqueUser() {
        testUser = generateRandomUser();
        Response response = userClient.createUser(testUser);
        assertSuccessfulUserCreation(response);
        accessToken = response.path("accessToken");
    }

    @Test
    public void testCreateExistingUser() {
        testUser = generateRandomUser();
        userClient.createUser(testUser);
        Response secondResponse = userClient.createUser(testUser);
        assertUserAlreadyExists(secondResponse);
    }

    @Test
    public void testCreateUserWithMissingField() {
        testUser = generateRandomUser();
        testUser.setName(null);
        Response response = userClient.createUser(testUser);
        assertMissingRequiredField(response);
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            userClient.deleteUser(accessToken);
        }
    }

    @Step("Assert successful user creation")
    private void assertSuccessfulUserCreation(Response response) {
        response.then()
                .assertThat()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Step("Assert user already exists")
    private void assertUserAlreadyExists(Response response) {
        response.then()
                .assertThat()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Step("Assert missing required field")
    private void assertMissingRequiredField(Response response) {
        response.then()
                .assertThat()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }
}