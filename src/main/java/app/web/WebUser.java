package app.web;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

import app.db.Account;
import app.db.DBContext;
import app.db.DBException;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

class WebUser
{
    WebUser(DBContext db)
    {
        this.db = db;
    }
    private DBContext db;

    EndpointGroup routes()
    {
        return () -> {
            post("/api/user", this::POST_user);
            get("/api/user/{username}", this::GET_user);
            post("/api/user/quiz", this::POST_user_quiz);
            put("/api/user/quiz", this::PUT_user_quiz);
            //get("/{username}/quiz", WebUser::GET_user_quiz);
            //get("/user/quiz/{id}", WebUser::GET_user_quiz_id);
        };
    }

    void POST_user(Context ctx)
            throws APIException
    {
        String json;

        json = ctx.body();

        ApiUser.post(db, json);

        ctx.status(201);
    }

    void GET_user(Context ctx)
            throws APIException
    {
        String username;
        String json;

        username = ctx.pathParam("username");
        if (username.charAt(0) != '@') {
            ctx.status(404);
            return;
        }
        username = username.substring(1, username.length());

        json = ApiUser.get(db, username);

        ctx.json(json);
    }

    void POST_user_quiz(Context ctx)
        throws APIException
    {
        String json;
        Account account;

        try { // FIXME: authenticate user
            account = Account.read(db, "Test_user");
        } catch (DBException e) {
            ctx.status(400);
            return;
        }

        json = ctx.body();
        ApiUser.postQuiz(db, account, json);

        ctx.status(201);
    }

    void PUT_user_quiz(Context ctx)
        throws APIException
    {
        String json;
        Account account;

        try { // FIXME: authenticate user
            account = Account.read(db, "Test_user");
        } catch (DBException e) {
            ctx.status(400);
            return;
        }

        json = ctx.body();
        ApiUser.putQuiz(db, account, json);

        ctx.status(201);
    }
}
