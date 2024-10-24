package testOrder;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Test;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

public class OrderListTest {

    @Test
    @DisplayName("Получить список заказов")
    public void getAllOrders() {
        Response response = ClientOrder.getAllOrders();
        response.then().assertThat().body("orders", hasSize(greaterThan(0))).and().statusCode(SC_OK);
    }
}
