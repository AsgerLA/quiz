package app.web;

import static io.javalin.apibuilder.ApiBuilder.get;

import java.util.List;
import java.util.Map;

import app.db.DBContext;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

class WebQuiz
{
    WebQuiz(DBContext db)
    {
        this.db = db;
    }
    private DBContext db;

    EndpointGroup routes()
    {
        return () -> {
            get("/api/quiz", this::GET_quiz);
            get("/api/quiz/{id}", this::GET_quiz_id);
        };
    }

    void GET_quiz(Context ctx)
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

    void GET_quiz_id(Context ctx)
        throws APIException
    {
        int id;
        String json;

        id = Integer.parseInt(ctx.pathParam("id"));

        json = ApiQuiz.get(db, id);

        ctx.json(json);
    }
}
