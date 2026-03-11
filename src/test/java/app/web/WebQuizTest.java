package app.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.restassured.RestAssured.given;

@ExtendWith(WebTest.class)
class WebQuizTest
{
    @Test
    public void GET_quiz()
    {
        given()
            .when()
            .get("/api/quiz")
            .then()
            .statusCode(200);
    }

    @Test
    public void GET_quiz_id()
    {

        given()
            .when()
            .get("/api/quiz/1")
            .then()
            .statusCode(200);
    }
}
