package app.web;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

import app.db.DBContext;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

class WebUser
{
    private static DBContext db;

    static EndpointGroup routes(DBContext db)
    {
        WebUser.db = db;
        return () -> {
            post("/user", WebUser::POST_user);
            get("/user/{id}", WebUser::GET_user_id);
            // TODO: user quiz routes
            //post("/user/quiz", WebUser::POST_user_quiz);
            //get("/user/quiz", WebUser::GET_user_quiz);
            //get("/user/quiz/{id}", WebUser::GET_user_quiz_id);
        };
    }

    static void POST_user(Context ctx)
        throws APIException
    {
        String json;

        json = ctx.body();

        ApiUser.post(db, json);

        ctx.status(201);
    }

    static void GET_user_id(Context ctx)
        throws APIException
    {
        int id;
        String json;

        try {
            id = Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.status(400);
            return;
        }

        json = ApiUser.get(db, id);

        ctx.json(json);
    }
}
