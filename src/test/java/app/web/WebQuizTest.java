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

    @Test
    void POST_quiz()
    {
        String JSON = """
        {
            "title" : "New Quiz",
            "tags" : ["test", "quiz"],
            "questions" : [
                {
                    "slot" : 0,
                    "question" : "Question?",
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
    void PUT_quiz()
    {
        String JSON = """
        {
            "id" : 1,
            "title" : "Updated Quiz",
            "tags" : [
                {
                    "action" : "create",
                    "name" : "newtag"
                }
            ],
            "questions" : [
                {
                    "id" : 1,
                    "action" : "delete"
                },
                {
                    "action" : "create",
                    "question" : "new question",
                    "slot" : 0,
                    "answers" : [
                        {
                            "action" : "create",
                            "answer" : "new answer",
                            "slot" : 1,
                            "points" : 1
                        }
                    ]
                },
                {
                    "action" : "update",
                    "id" : 2,
                    "question" : "updated question",
                    "slot" : 1,
                    "answers" : [
                        {
                            "action" : "create",
                            "answer" : "new answer",
                            "slot" : 1,
                            "points" : 1
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
            .put("/api/quiz")
            .then()
            .statusCode(204);
    }

    @Test
    void DELETE_quiz()
    {

        given()
            .when()
            .delete("/api/quiz/2")
            .then()
            .statusCode(204);
    }
}
