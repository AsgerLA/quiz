package app.web;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(WebTest.class)
class WebCategoryTest
{
    @Test
    public void GET_category()
    {
        given()
            .when()
            .get("/api/category")
            .then()
            .statusCode(200);
    }

    @Test
    public void GET_category_name()
    {

        given()
            .when()
            .get("/api/category/trivia")
            .then()
            .statusCode(200);
    }
}
