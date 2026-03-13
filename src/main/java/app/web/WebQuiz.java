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
            get("/api/quiz/search", this::GET_quiz_search);
            get("/api/quiz/user/{id}", this::GET_quiz_user);
            get("/api/quiz/{id}", this::GET_quiz_id);
        };
    }

    void GET_quiz(Context ctx)
            throws APIException
    {
        /* ?category=<tag-name>
         * &tag=<tag-name>
         * &sort=<column>
         * &order=<desc|asc>
         * &page=<page-num>
         */
        Map<String, List<String>> query;
        String json;

        query = ctx.queryParamMap();
        json = ApiQuiz.get(db, query, null);

        ctx.json(json);
    }

    void GET_quiz_search(Context ctx)
            throws APIException
    {
        String json;
        String search;
        Integer page = null;

        search = ctx.queryParam("query");
        try {
            String tmp = ctx.queryParam("page");
            if (tmp != null)
                page = Integer.parseInt(tmp);
        } catch (NumberFormatException e) {
            throw new APIException(400, "bad page");
        }
        json = ApiQuiz.getSearch(db, search, page);

        ctx.json(json);
    }

    void GET_quiz_user(Context ctx)
            throws APIException
    {
        /* ?category=<tag-name>
         * &tag=<tag-name>
         * &sort=<column>
         * &order=<desc|asc>
         * &page=<page-num>
         */
        Map<String, List<String>> query;
        String json;
        int ownerId;

        try {
            ownerId = Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.status(404);
            return;
        }

        query = ctx.queryParamMap();
        json = ApiQuiz.get(db, query, ownerId);

        ctx.json(json);
    }

    void GET_quiz_id(Context ctx)
        throws APIException
    {
        int id;
        String json;

        try {
            id = Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.status(404);
            return;
        }

        json = ApiQuiz.get(db, id);

        ctx.json(json);
    }
}
