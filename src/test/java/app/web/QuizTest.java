package app.web;

import app.db.DBTest;
import app.web.json.JsonParser;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import io.javalin.testtools.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(DBTest.class)
class QuizTest
{
    private final Javalin app = Web.newJavalinApp(DBTest.db);

    @Test
    public void GET_quiz() {
        JavalinTest.test(app, (server, client) -> {
            Response res = client.get("/quiz");
            assertEquals(200, res.code());
            JsonParser.decodeObject(res.body().string());
        });
    }
}
