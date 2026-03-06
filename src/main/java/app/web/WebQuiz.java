package app.web;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

import app.db.DBContext;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

class WebQuiz
{
    private static DBContext db;

    static EndpointGroup routes(DBContext db)
    {
        WebQuiz.db = db;
        return () -> {
            get("/quizlist", WebQuiz::GET_quizlist);
            post("/quiz", WebQuiz::POST_quiz);
            get("/quiz/{id}", WebQuiz::GET_quiz);
        };
    }

    static void GET_quizlist(Context ctx)
            throws APIException
    {
        String tagname;

        tagname = ctx.queryParam("tag");
        ctx.json(ApiQuiz.getlist(db, tagname));
    }

    static void POST_quiz(Context ctx)
            throws APIException
    {
        ApiQuiz.post(db, ctx.body());
        ctx.status(201);
    }

    static void GET_quiz(Context ctx)
            throws APIException
    {
        int id;
        String json;

        id = Integer.parseInt(ctx.pathParam("id"));
        json = ApiQuiz.get(db, id);

        ctx.json(json);
    }
}
