package app.web;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;
import static io.javalin.apibuilder.ApiBuilder.delete;

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
            get("/api/user/quiz/{id}", this::GET_user_quiz);
            post("/api/user/quiz", this::POST_user_quiz);
            put("/api/user/quiz", this::PUT_user_quiz);
            delete("/api/user/quiz/{id}", this::DELETE_user_quiz);
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
            throw new APIException(404, "Not Found");
        }
        username = username.substring(1, username.length());

        json = ApiUser.get(db, username);

        ctx.json(json);
    }

    void GET_user_quiz(Context ctx)
        throws APIException
    {
        Account account;
        Integer id;
        String json;

        try { // FIXME: authenticate user
            account = Account.loadByName(db, "Test_user");
            id = Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException|DBException e) {
            throw new APIException(400, e);
        }

        json = ApiUser.getQuiz(db, account, id);

        ctx.json(json);
    }

    void POST_user_quiz(Context ctx)
        throws APIException
    {
        String json;
        Account account;

        try { // FIXME: authenticate user
            account = Account.loadByName(db, "Test_user");
        } catch (DBException e) {
            throw new APIException(500, e);
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
            account = Account.loadByName(db, "Test_user");
        } catch (DBException e) {
            throw new APIException(500, e);
        }

        json = ctx.body();
        ApiUser.putQuiz(db, account, json);

        ctx.status(204);
    }

    void DELETE_user_quiz(Context ctx)
        throws APIException
    {
        Account account;
        Integer id;

        try { // FIXME: authenticate user
            account = Account.loadByName(db, "Test_user");
            id = Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException|DBException e) {
            throw new APIException(400, e);
        }

        ApiUser.deleteQuiz(db, account, id);

        ctx.status(204);
    }
}
