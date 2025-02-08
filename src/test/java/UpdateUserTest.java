import client.UserClient;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import io.restassured.response.Response;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static util.UserGenerator.generateRandomUser;

public class UpdateUserTest {

    private final UserClient userClient = new UserClient();
    private String accessToken;
    private User testUser;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/api/auth";
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
    public void testUpdateUserWithAuthorization() {
        // Обновляем имя пользователя
        String newName = "New Username";
        User updatedUser = new User(testUser.getEmail(), testUser.getPassword(), newName);

        Response response = updateUser(updatedUser, accessToken);
        assertSuccessfulUpdate(response, newName);

        // Проверяем, что данные пользователя действительно обновились
        Response getUserResponse = getUser(accessToken);
        assertSuccessfulGetUser(getUserResponse, updatedUser.getEmail(), newName);
    }

    @Test
    public void testUpdateUserWithoutAuthorization() {
        // Обновляем имя пользователя
        String newName = "New Username";
        User updatedUser = new User(testUser.getEmail(), testUser.getPassword(), newName);

        Response response = updateUser(updatedUser, null);
        assertUnauthorizedUpdate(response);
    }

    @Step("Update user with API. Token: {accessToken}")
    private Response updateUser(User user, String accessToken) {
        RequestSpecification request = given()
                .header("Content-type", "application/json")
                .body(user);

        if (accessToken != null) {
            request = request.header("Authorization", accessToken);
        }

        return request.when()
                .patch("/user");
    }


    @Step("Get user with API. Token: {accessToken}")
    private Response getUser(String accessToken) {
        return given()
                .header("Authorization", accessToken)
                .when()
                .get("/user");
    }


    @Step("Assert successful update. New name: {newName}")
    private void assertSuccessfulUpdate(Response response, String newName) {
        response.then()
                .assertThat()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(testUser.getEmail()))
                .body("user.name", equalTo(newName));
    }


    @Step("Assert unauthorized update")
    private void assertUnauthorizedUpdate(Response response) {
        response.then()
                .assertThat()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Step("Assert successful get user. Email: {email}, Name: {name}")
    private void assertSuccessfulGetUser(Response response, String email, String name) {
        response.then()
                .assertThat()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(email))
                .body("user.name", equalTo(name));
    }
}