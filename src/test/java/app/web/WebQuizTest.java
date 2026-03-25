package app.web;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

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

    @Test
    @Disabled
    void POST_quiz()
    {
        String JSON = """
        {
            "title" : "New Quiz",
            "description" : "A new Quiz",
            "tags" : ["test", "quiz"],
            "questions" : [
                {
                    "slot" : 0,
                    "question" : "Question?",
                    "type",  "MULTI",
                    "answers" : [
                        {
                            "slot" : 0,
                            "answer" : "true",
                            "points" : 1
                        },
                        {
                            "slot" : 1,
                            "answer" : "false",
                            "points" : 0
                        }
                    ]
                }
            ]
        }
        """;

        given()
            .header("Content-Type", "application/json")
            .body(JSON)
            .when()
            .post("/api/quiz")
            .then()
            .statusCode(204);
    }


    @Test
    @Disabled
    void PUT_quiz()
    {
        given()
            .header("Content-Type", "application/json")
            .when()
            .put("/api/quiz")
            .then()
            .statusCode(204);
    }

    @Test
    @Disabled
    void DELETE_quiz()
    {

        given()
            .when()
            .delete("/api/quiz/2")
            .then()
            .statusCode(204);
    }
}
