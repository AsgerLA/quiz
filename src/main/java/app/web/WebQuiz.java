package app.web;

import io.javalin.apibuilder.EndpointGroup;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;
import static io.javalin.apibuilder.ApiBuilder.delete;
import io.javalin.http.Context;

import app.db.*;
import app.web.json.*;

class WebQuiz
{
    static EndpointGroup routes()
    {
        return () ->{
            post("/quiz", WebQuiz::POST_quiz);
            get("/quiz/{id}", WebQuiz::GET_quiz);
        };
    }

    static void POST_quiz(Context ctx)
            throws APIException
    {
        ApiQuiz.post(Web.db, ctx.body());
        ctx.status(200);
    }

    static void GET_quiz(Context ctx)
            throws APIException
    {
        int id;
        String json;

        id = Integer.parseInt(ctx.pathParam("id"));

        json = ApiQuiz.get(Web.db, id);

        ctx.json(json);
    }
}
