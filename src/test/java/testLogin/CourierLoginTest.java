package testLogin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Epic("Courier Management")
@Feature("Логин курьера")
public class CourierLoginTest {
    private Gson gson;
    private int courierId = -1;
    private final CourierAssistant courierAssistant = new CourierAssistant();
    @After
    public void tearDown() {
        if (courierId != -1) {
            courierAssistant.deleteCourier(courierId);
        }
    }
    @Before
    @Step("Настройка тестовой среды")
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";
        gson = new GsonBuilder().setPrettyPrinting().create();
    }
    public static class CourierAssistant {
        private final Gson gson;

        public CourierAssistant() {
            gson = new GsonBuilder().setPrettyPrinting().create();
        }
        public String formatResponseBody(String responseBody) {
            JsonElement jsonElement = JsonParser.parseString(responseBody);
            return gson.toJson(jsonElement);
        }
        public void checkStatusCode(Response response, int expectedStatusCode) {
            assertThat(response.getStatusCode(), is(expectedStatusCode));
        }
        public void checkErrorMessage(Response response, String expectedMessage) {
            assertThat(response.jsonPath().getString("message"), is(expectedMessage));
        }
        public String createRequestBody(String login, String password, String firstName) {
            return "{ \"login\": \"" + login + "\", \"password\": \"" + password + "\", \"firstName\": \"" + firstName + "\" }";
        }
        @Step("Создание курьера")
        public Response createCourier(String body) {
            return RestAssured.given()
                    .header("Content-Type", "application/json")
                    .body(body)
                    .when()
                    .post("/api/v1/courier");
        }
        public int getCourierId(String login, String password) {
            Response response = RestAssured.given()
                    .header("Content-Type", "application/json")
                    .body("{ \"login\": \"" + login + "\", \"password\": \"" + password + "\" }")
                    .when()
                    .post("/api/v1/courier/login");
            if (response.getStatusCode() == 200) {
                return response.jsonPath().getInt("id");
            } else {
                return -1;
            }
        }
        public void deleteCourier(int courierId) {
            RestAssured.given()
                    .header("Content-Type", "application/json")
                    .when()
                    .delete("/api/v1/courier/" + courierId)
                    .then()
                    .statusCode(200);
            System.out.println("Курьер с ID " + courierId + " был удален.");
        }
    }
    @Test
    @DisplayName("Courier can be created and login")
    @Step("Курьер может авторизоваться")
    public void testCourierCanBeCreatedAndLogin() {
        String login = "galy";
        String password = "1234";
        String body = courierAssistant.createRequestBody(login, password, "ytut");
        Response createResponse = courierAssistant.createCourier(body);
        courierAssistant.checkStatusCode(createResponse, 201);
        System.out.println("Курьер успешно создан. Код ответа: " + createResponse.getStatusCode());
        String loginBody = "{ \"login\": \"" + login + "\", \"password\": \"" + password + "\" }";
        Response loginResponse = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(loginBody)
                .when()
                .post("/api/v1/courier/login");
        courierAssistant.checkStatusCode(loginResponse, 200);
        assertThat(loginResponse.jsonPath().get("id"), is(notNullValue()));
        courierId = courierAssistant.getCourierId(login, password);
        assertThat(courierId, is(not(-1)));
    }
    @Test
    @DisplayName("Login with wrong credentials should fail")
    @Step("Система вернёт ошибку, если неправильно указать логин или пароль")
    public void testWithWrongLoginOrPasswordCourier() {
        String login = "qal";
        String password = "123";
        String body = courierAssistant.createRequestBody(login, password, "ytut");
        Response createResponse = courierAssistant.createCourier(body);
        courierAssistant.checkStatusCode(createResponse, 201);
        System.out.println("Курьер создан. Код ответа: " + createResponse.getStatusCode());
        //  Неверный логин и пароль
        String bodyWrongCredentials = "{ \"login\": \"User \", \"password\": \"4321\" }";
        Response responseWrongCredentials = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(bodyWrongCredentials)
                .when()
                .post("/api/v1/courier/login");
        courierAssistant.checkStatusCode(responseWrongCredentials, 404);
        courierAssistant.checkErrorMessage(responseWrongCredentials, "Учетная запись не найдена");
        courierId = courierAssistant.getCourierId(login, password);
        assertThat(courierId, is(not(-1)));
    }
    @Test
    @DisplayName("Missing required fields returns error")
    @Step("Если какого-то поля нет, запрос возвращает ошибку")
    public void testMissingRequiredFieldsCourier() {
        String login = "qly";
        String password = "1234";
        String body = courierAssistant.createRequestBody(login, password, "ytut");
        Response createResponse = courierAssistant.createCourier(body);
        courierAssistant.checkStatusCode(createResponse, 201);
        System.out.println("Курьер успешно создан. Код ответа: " + createResponse.getStatusCode());
        try {
            courierId = courierAssistant.getCourierId(login, password);
            assertThat(courierId, is(not(-1)));
            //  Отсутствует поле "login"
            String bodyWithoutLogin = "{ \"password\": \"1234\" }";
            Response responseWithoutLogin = RestAssured.given()
                    .header("Content-Type", "application/json")
                    .body(bodyWithoutLogin)
                    .when()
                    .post("/api/v1/courier/login");
            String expectedMessage = "Недостаточно данных для входа";
            courierAssistant.checkStatusCode(responseWithoutLogin, 400);
            courierAssistant.checkErrorMessage(responseWithoutLogin, expectedMessage);
            System.out.println("Тест на отсутствие логина. Код ответа: " + responseWithoutLogin.getStatusCode());
            //  Отсутствует поле "password"
            String bodyWithoutPassword = "{ \"login\": \"qaly\" }";
            Response responseWithoutPassword = RestAssured.given()
                    .header("Content-Type", "application/json")
                    .body(bodyWithoutPassword)
                    .when()
                    .post("/api/v1/courier/login");
            courierAssistant.checkStatusCode(responseWithoutPassword, 400);
            courierAssistant.checkErrorMessage(responseWithoutPassword, expectedMessage);

            System.out.println("Тест на отсутствие пароля. Код ответа: " + responseWithoutPassword.getStatusCode());
        } finally {
            if (courierId != -1) {
                courierAssistant.deleteCourier(courierId);
            }
        }
    }
    @Test
    @DisplayName("Login non-existent user returns error")
    @Step("Если авторизоваться под несуществующим пользователем, запрос возвращает ошибку")
    public void testLoginNonExistentUser () {
        String bodyNonExistentUser  = "{ \"login\": \"Reva\", \"password\": \"2345\" }";
        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(bodyNonExistentUser )
                .when()
                .post("/api/v1/courier/login");
        String expectedErrorMessage = new String("Учетная запись не найдена".getBytes(), StandardCharsets.UTF_8);
        courierAssistant.checkStatusCode(response, 404);
        courierAssistant.checkErrorMessage(response, "Учетная запись не найдена");
        System.out.println("Not existent user login:");
        System.out.println("Response Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.asString());
    }
}

