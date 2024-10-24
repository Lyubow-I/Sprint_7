package testOrder;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.apache.http.HttpStatus.*;

@RunWith(Parameterized.class)
public class OrderCreatTest {
    private String firstName;
    private String lastName;
    private String address;
    private String metroStation;
    private String phone;
    private String deliveryDate;
    private String comment;
    private String[] color;
    private int rentTime;
    String orderId;
    @After
    public void tearDown() {
        ClientOrder.deleteOrder(orderId);
    }

    public OrderCreatTest(String firstName, String lastName, String address, String metroStation,
                           String phone, int rentTime, String deliveryDate, String comment, String[] color) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.metroStation = metroStation;
        this.phone = phone;
        this.rentTime = rentTime;
        this.deliveryDate = deliveryDate;
        this.comment = comment;
        this.color = color;
    }

    @Parameterized.Parameters
    public static Object[][] getOrderData() {
        return new Object[][]{
                { "Света", "Светова", "Светлая, 1", "5", "+7 777 777 77 77", 5, "01.11.2024", "Звоните", new String[] { "BLACK" } },
                { "Света", "Светова", "Светлая, 1", "5", "+7 777 777 77 77", 5, "01.11.2024", "Звоните", new String[] { "GRAY", "BLACK" } },
                { "Света", "Светова", "Светлая, 1", "5", "+7 777 777 77 77", 5, "01.11.2024", "Звоните", new String[] { } },
                { "Света", "Светова", "Светлая, 1", "5", "+7 777 777 77 77", 5, "01.11.2024", "Звоните", new String[] { "GRAY" } },
        };
    }

    @Test
    @DisplayName("Создание заказа с использованием разных цветов")
    public void createOrderParameterizedColorScooterTest() {
        CreateOrder createOrder = new CreateOrder(firstName, lastName, address,
                metroStation, phone, deliveryDate, comment, color, rentTime);
        Response createResponse = ClientOrder.createNewOrder(createOrder);
        ClientOrder.comparingSuccessfulOrderSet(createResponse, SC_CREATED);
        orderId = ClientOrder.getOrderId(createResponse);
        Response deleteResponse = ClientOrder.deleteOrder(orderId);
        ClientOrder.comparingSuccessfulOrderCancel(deleteResponse, SC_OK);
            }
}
