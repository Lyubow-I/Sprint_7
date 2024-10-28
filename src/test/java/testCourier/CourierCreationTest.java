package testCourier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@Epic("Courier Management")
@Feature("Создание курьера")
public class CourierCreationTest {

    private Gson gson;
    private int courierId = -1;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @After
    public void tearDown() {
        if (courierId != -1) {
            deleteCourier(courierId);
        }
    }

    private String formatResponseBody(String responseBody) {

        JsonElement jsonElement = JsonParser.parseString(responseBody);
        return gson.toJson(jsonElement);
    }

    private void checkStatusCode(Response response, int expectedStatusCode) {
        assertThat(response.getStatusCode(), is(expectedStatusCode));
    }

    private void checkErrorMessage(Response response, String expectedMessage) {
        assertThat(response.jsonPath().getString("message"), is(expectedMessage));
    }

    private String createRequestBody(String login, String password, String firstName) {
        return "{ \"login\": \"" + login + "\", \"password\": \"" + password + "\", \"firstName\": \"" + firstName + "\" }";
    }

    private int getCourierId(String login, String password) {
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

    private void deleteCourier(int courierId) {
        RestAssured.given()
                .header("Content-Type", "application/json")
                .when()
                .delete("/api/v1/courier/" + courierId)
                .then()
                .statusCode(200); // Ожидаем успешное удаление курьера
        System.out.println("Курьер с ID " + courierId + " был удален.");
    }

    private void printResponse(Response response) {
        String responseBody = response.getBody().asString();
        String formattedJson = formatResponseBody(responseBody);
        System.out.println("Код ответа: " + response.getStatusCode());
        System.out.println("Тело ответа: " + formattedJson);
    }

    @Test
    @Story("Курьера можно создать")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that creating a new courier is possible and returns the correct response")
    public void testCreateCourierIsPossible() {
        String login = "gali";
        String password = "1234";
        String body = createRequestBody(login, password, "ytut");
        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(body)
                .when()
                .post("/api/v1/courier");
        checkStatusCode(response, 201);
        assertThat(response.jsonPath().get("ok"), is(true));

        courierId = getCourierId(login, password);
        assertThat(courierId, is(not(-1)));
    }

    @Test
    @Story("Нельзя создать двух одинаковых курьеров")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that creating a courier with the same login returns an error")
    public void testErrorCreateTheSameCourier() {
        String login = "gali"; // Логин
        String password = "1234"; // Пароль
        String body = createRequestBody(login, password, "ytut");
        Response firstResponse = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(body)
                .when()
                .post("/api/v1/courier");
        checkStatusCode(firstResponse, 201);
        System.out.println("Курьер успешно создан. Код ответа: " + firstResponse.getStatusCode());
        Response secondResponse = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(body)
                .when()
                .post("/api/v1/courier");
        printResponse(secondResponse);
        checkStatusCode(secondResponse, 409);
        String expectedMessage = "Этот логин уже используется. Попробуйте другой.";
        checkErrorMessage(secondResponse, expectedMessage);
        courierId = getCourierId(login, password);
        assertThat(courierId, is(not(-1)));
    }
    @Test
    @Story("Validate required fields for courier creation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Чтобы создать курьера, нужно передать в ручку все обязательные поля")
    public void testCreateCourierWithAllRequiredFields() {
        String login = "galy";
        String password = "1234";
        String body = createRequestBody(login, password, "ytut");
        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(body)
                .when()
                .post("/api/v1/courier");
        printResponse(response);
        checkStatusCode(response, 201);
        assertThat(response.jsonPath().get("ok"), is(true));
        courierId = getCourierId(login, password);
        assertThat(courierId, is(not(-1)));
    }
    @Test
    @Story("Validate status code 201 for successful courier creation")
    @Severity(SeverityLevel.MINOR)
    @Description("Запрос создание курьера, возвращает правильный код ответа")
    public void testCreateCourier() {
        String login = "galu"; // Логин
        String password = "1234"; // Пароль
        String body = createRequestBody(login, password, "ytut");
        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(body)
                .when()
                .post("/api/v1/courier");

        System.out.println("Код ответа: " + response.getStatusCode());
        checkStatusCode(response, 201);

        courierId = getCourierId(login, password); // Сохраняем ID курьера
        assertThat(courierId, is(not(-1))); // Убедитесь, что ID не -1
    }

    // Тест на успешный запрос создания курьера, возвращает ok: true
    @Test
    @Story("Validate 'ok: true' for successful courier creation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Успешный запрос создания курьера, возвращает ok: true")
    public void testCreateCourierOkTrue() {
        String login = "qazhof";
        String password = "1234";
        String body = createRequestBody(login, password, "saske");
        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(body)
                .when()
                .post("/api/v1/courier");
        printResponse(response);
        assertThat(response.jsonPath().get("ok"), is(true));
        courierId = getCourierId(login, password);
        assertThat(courierId, is(not(-1)));
    }
    @Test
    @Story("Validate error for missing required fields in courier creation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Если одного из полей нет, запрос возвращает ошибку. Пропущено поле login")
    public void testCreateCourierWithoutLogin() {
        String bodyWithoutLogin = "{ \"password\": \"1234\", \"firstName\": \"ytut\" }";
        String expectedMessage = "Недостаточно данных для создания учетной записи";
        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(bodyWithoutLogin)
                .when()
                .post("/api/v1/courier");
        printResponse(response);
        checkStatusCode(response, 400);
        System.out.println("Курьер не создан: пропущено поле login");
        checkErrorMessage(response, expectedMessage);
    }
    @Test
    @Story("Validate error for missing required fields in courier creation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Если одного из полей нет, запрос возвращает ошибку. Пропущено поле password")
    public void testCreateCourierWithoutPassword() {
        String login = "galy" + System.currentTimeMillis();
        String bodyWithoutPassword = "{ \"login\": \"" + login + "\", \"firstName\": \"ytut\" }";
        String expectedMessage = "Недостаточно данных для создания учетной записи";
        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(bodyWithoutPassword)
                .when()
                .post("/api/v1/courier");
        printResponse(response);
        checkStatusCode(response, 400);
        System.out.println("Курьер не создан: пропущено поле password");
        checkErrorMessage(response, expectedMessage);
    }
    @Test
    @Story("Validate error for missing required fields in courier creation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Если одного из полей нет, запрос возвращает ошибку. Пропущено поле firstName")
    public void testCreateCourierWithoutFirstName() {
        String login = "galy" + System.currentTimeMillis();
        String bodyWithoutFirstName = "{ \"login\": \"" + login + "\", \"password\": \"1234\" }";
        String expectedMessage = "Недостаточно данных для создания учетной записи";
        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(bodyWithoutFirstName)
                .when()
                .post("/api/v1/courier");
        printResponse(response);
        checkStatusCode(response, 400);
        System.out.println("Курьер не создан: пропущено поле firstName");
        checkErrorMessage(response, expectedMessage);
    }
}


