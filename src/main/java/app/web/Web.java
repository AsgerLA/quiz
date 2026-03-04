package app.web;

import app.db.*;
import io.javalin.Javalin;

public class Web
{
    static DBContext db = null;

    public static Javalin newJavalinApp(DBContext db)
    {
        Web.db = db;
        Javalin app = Javalin.create(config -> {
            config.routes.apiBuilder(WebQuiz.routes());
            config.routes.exception(Exception.class, (e, ctx) -> {
                e.printStackTrace();
                ctx.status(500);
            });
            config.routes.exception(APIException.class, (e, ctx) -> {
                System.out.println(e.getMessage());
                ctx.status(e.code);
            });
        });

        return app;
    }
}
