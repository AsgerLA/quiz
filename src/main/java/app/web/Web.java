package app.web;

import app.db.DBContext;
import io.javalin.Javalin;

public class Web {

    public static Javalin newJavalinApp(DBContext db) {
        Javalin app = Javalin.create(config -> {
            // routes
            config.routes.apiBuilder(WebQuiz.routes(db));
            config.routes.apiBuilder(WebUser.routes(db));
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
