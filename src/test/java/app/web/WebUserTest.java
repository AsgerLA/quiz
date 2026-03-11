package app.web;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import app.web.json.JsonBuilder;

@ExtendWith(WebTest.class)
class WebUserTest
{
    @Test
    void POST_user()
    {
        String json;
        JsonBuilder jb = new JsonBuilder();

        jb.objectBegin();
        jb.field("username", "Test_user2");
        jb.objectEnd();
        json = jb.build();

        given()
            .header("Content-Type", "application/json")
            .body(json)
            .when()
            .post("/api/user")
            .then()
            .statusCode(201);
    }

    @Test
    void GET_user()
    {
        given()
            .when()
            .get("/api/user/@Test_user")
            .then()
            .statusCode(200)
            .body("username", is("Test_user"));
    }
}
