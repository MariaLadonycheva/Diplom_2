package util;

import model.User;
import org.apache.commons.lang3.RandomStringUtils;
import io.qameta.allure.Step;

public class UserGenerator {
    @Step("Generate random user")
    public static User generateRandomUser() {
        String email = generateRandomEmail();
        String password = "password";
        String name = "Username";
        return new User(email, password, name);
    }

    @Step("Generate random email")
    private static String generateRandomEmail() {
        String randomString = RandomStringUtils.randomAlphanumeric(10).toLowerCase();
        return "test-" + randomString + "@example.com";
    }
}