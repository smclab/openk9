import io.openk9.connector.api.HealthResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestHTTPEndpoint(HealthResource.class)
public class HealthResourceTest {

    @Test
    public void testHealthEndpoint() {
        when().get()
                .then()
                .statusCode(200)
                .body("status", is("UP"));
    }
}