package app.web;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

import java.util.List;
import java.util.Map;

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
            post("/quiz", WebQuiz::POST_quiz);
            put("/quiz/", WebQuiz::PUT_quiz);
            get("/quiz", WebQuiz::GET_quiz);
            get("/quiz/{id}", WebQuiz::GET_quiz_id);
        };
    }

    static void POST_quiz(Context ctx)
            throws APIException
    {
        ApiQuiz.post(db, ctx.body());
        ctx.status(201);
    }

    static void PUT_quiz(Context ctx)
            throws APIException
    {
        ApiQuiz.put(db, ctx.body());
        ctx.status(200);
    }

    static void GET_quiz(Context ctx)
            throws APIException
    {
        /* ?category=<tag-name>
         * &tag=<tag-name>
         * &sort-order=<desc|asc>
         * &sort-key=<column>
         * &page=<page-num>
         */
        Map<String, List<String>> query;
        String json;

        query = ctx.queryParamMap();
        json = ApiQuiz.get(db, query);

        ctx.json(json);
    }

    static void GET_quiz_id(Context ctx)
        throws APIException
    {
        int id;
        String json;

        id = Integer.parseInt(ctx.pathParam("id"));

        json = ApiQuiz.get(db, id);

        ctx.json(json);
    }
}
