package app.web;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(WebTest.class)
class WebAccountTest
{
    @Test
    void GET_account()
    {
        given()
            .when()
            .get("/api/account/@Test_user")
            .then()
            .statusCode(200)
            .body("username", is("Test_user"));
    }
}
