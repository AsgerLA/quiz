package app.web;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.contains;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(WebTest.class)
class WebUserTest
{
    @Test
    void POST_user()
    {
        String JSON = """
        {
            "username" : "Test_user2"
        }
        """;

        given()
            .header("Content-Type", "application/json")
            .body(JSON)
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

    @Test
    void GET_user_quiz()
    {
        given()
            .when()
            .get("/api/user/quiz/1")
            .then()
            .statusCode(200);
    }

    @Test
    void POST_user_quiz()
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
            .post("/api/user/quiz")
            .then()
            .statusCode(201);
    }


    @Test
    void PUT_user_quiz()
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
            .put("/api/user/quiz")
            .then()
            .statusCode(204);
    }

    @Test
    void DELETE_user_quiz()
    {

        given()
            .when()
            .delete("/api/user/quiz/2")
            .then()
            .statusCode(204);
    }
}
