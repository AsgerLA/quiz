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
    public void POST_user()
    {
        String json;
        JsonBuilder jb = new JsonBuilder();

        jb.objectBegin();
        jb.field("username", "Test user2");
        jb.objectEnd();
        json = jb.build();

        given()
            .header("Content-type", "application/json")
            .body(json)
            .when()
            .post("/user")
            .then()
            .statusCode(201);
    }

    @Test
    public void GET_user()
    {
        given()
            .when()
            .get("/user/1")
            .then()
            .statusCode(200)
            .body("username", is("Test user"));
    }
}
