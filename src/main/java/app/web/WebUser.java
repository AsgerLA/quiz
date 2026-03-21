package app.web;

import static io.javalin.apibuilder.ApiBuilder.get;

import app.db.Account;
import app.db.DBContext;
import app.web.json.JsonBuilder;
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
            get("/api/user/{username}", this::GET_user);
        };
    }

    void GET_user(Context ctx)
    {
        String username;
        Account account;
        String json;

        username = ctx.pathParam("username");
        if (username.charAt(0) != '@') {
            Result.error(ctx, 400, "path param should start with '@'");
            return;
        }
        username = username.substring(1, username.length());

        account = Account.loadByName(db, username);
        json = toJson(account);

        Result.ok(ctx, json);
    }

    static String toJson(Account account)
    {
        JsonBuilder jb = new JsonBuilder();

        jb.objectBegin();
            jb.field("id", account.id);
            jb.field("username", account.username);
        jb.objectEnd();

        return jb.build();
    }
}
