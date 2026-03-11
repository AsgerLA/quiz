package app.web;

import app.db.DBContext;
import io.javalin.Javalin;

public class Web {

    public static Javalin newJavalinApp(DBContext db) {
        WebQuiz webQuiz = new WebQuiz(db);
        WebUser webUser = new WebUser(db);
        Javalin app = Javalin.create(config -> {
            // routes
            config.routes.apiBuilder(webQuiz.routes());
            config.routes.apiBuilder(webUser.routes());
            // exception handling
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
