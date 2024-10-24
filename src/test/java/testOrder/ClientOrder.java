package testOrder;

import io.qameta.allure.Step;
import io.qameta.allure.internal.shadowed.jackson.databind.ObjectMapper;
import io.qameta.allure.internal.shadowed.jackson.databind.SerializationFeature;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class ClientOrder {
    private static final String CREATE_ORDERS = "/api/v1/orders";
    private static final String CANCEL_ORDER = "/api/v1/orders/finish";
    private static final String GET_ORDER_BY_TRACK = "/api/v1/orders/track";

    @Step("Создание заказа")
    public static Response createNewOrder(CreateOrder createOrder) {
        Response response = given()
                .spec(Specific.requestSpec())
                .header("Content-type", "application/json")
                .body(createOrder)
                .post(CREATE_ORDERS);
        if (response.getStatusCode() != 201) {
            throw new RuntimeException("Ошибка при создании заказа. Код ответа: " + response.getStatusCode() + ", Тело ответа: " + response.asString());
        }
        JsonPath jsonPath = new JsonPath(response.asString());
        Integer trackNumber = jsonPath.get("track");
        if (trackNumber == null) {
            throw new RuntimeException("Ошибка при создании заказа. Поле 'track' отсутствует в ответе. Тело ответа: " + response.asString());
        }
        System.out.println("Заказ создан. Код ответа: " + response.getStatusCode() + ", Тело ответа: " + response.asString() + ", номер заказа: " + trackNumber);
        return response;
    }
    @Step("Закрытие ордера по ID")
    public static Response deleteOrder(String id) {
        Response response = given()
                .spec(Specific.requestSpec())
                .header("Content-type", "application/json")
                .put(CANCEL_ORDER + "?id=" + id);
        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Ошибка при удалении заказа. Код ответа: " + response.getStatusCode() + ", Тело ответа: " + response.asString());
        }
        System.out.println("Заказ удалён. Код ответа: " + response.getStatusCode() + ", Тело ответа: " + response.asString());
        return response;
    }
    @Step("Получение Id заказа по трек-номеру заказа")
    public static String getOrderId(Response response) {
        String trackNumber = response.then().extract().body().asString();
        JsonPath jsonPath = new JsonPath(trackNumber);
        Response trackResponse = given()
                .spec(Specific.requestSpec())
                .header("Content-type", "application/json")
                .get(GET_ORDER_BY_TRACK + "?track=" + jsonPath.getString("track"));
        if (trackResponse.getStatusCode() != 200) {
            throw new RuntimeException("Ошибка при получении ID заказа. Код ответа: " + trackResponse.getStatusCode() + ", Тело ответа: " + trackResponse.asString());
        }
        JsonPath orderJson = new JsonPath(trackResponse.asString());
        String orderId = orderJson.getString("id");
        if (orderId == null) {
            throw new RuntimeException("Ошибка: ID заказа отсутствует в ответе. Тело ответа: " + trackResponse.asString());
        }
        return orderId;
    }
    @Step("Проверка успешного завершения заказа")
    public static void comparingSuccessfulOrderCancel(Response response, int expectedResponseCode) {
        if (response.getStatusCode() != expectedResponseCode) {
            throw new AssertionError("Ожидаемый код ответа: " + expectedResponseCode + ", Фактический код ответа: " + response.getStatusCode());
        }
        response.then().assertThat().body("ok", equalTo(true)).and().statusCode(expectedResponseCode);
    }
    @Step("Сравнение ожидаемого кода ответа с фактическим")
    public static void comparingSuccessfulOrderSet(Response response, int responseCode) {
        response.then().assertThat().body("track", not(0)).and().statusCode(responseCode);
    }
    @Step("Получить список заказов")
    public static Response getAllOrders() {
        Response response = given()
                .spec(Specific.requestSpec())
                .header("Content-type", "application/json")
                .get(CREATE_ORDERS);
        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Ошибка при получении списка заказов. Код ответа: " + response.getStatusCode() + ", Тело ответа: " + response.asString());
        }
        JsonPath jsonPath = new JsonPath(response.asString());
        List<Map<String, Object>> orders = jsonPath.getList("orders");
        if (orders == null || orders.isEmpty()) {
            throw new RuntimeException("Ошибка: список заказов пуст.");
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            String ordersJson = objectMapper.writeValueAsString(orders);
            System.out.println("Список заказов получен. Код ответа: " + response.getStatusCode() + ", Тело ответа: " + response.asString());
            System.out.println(ordersJson);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при преобразовании списка заказов в JSON: " + e.getMessage());
        }
        return response;
    }
}



