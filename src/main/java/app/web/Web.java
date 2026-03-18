package app.web;

import app.db.DBContext;
import app.web.json.JsonBuilder;
import io.javalin.Javalin;

public class Web
{
    public static Javalin newJavalinApp(DBContext db)
            throws APIException
    {
        WebQuiz webQuiz = new WebQuiz(db);
        WebCategory webCategory = new WebCategory(db);
        WebUser webUser = new WebUser(db);
        WebAdmin webAdmin = new WebAdmin(db);

        Javalin app = Javalin.create(config -> {
            // routes
            config.routes.apiBuilder(webQuiz.routes());
            config.routes.apiBuilder(webCategory.routes());
            config.routes.apiBuilder(webUser.routes());
            config.routes.apiBuilder(webAdmin.routes());
            config.router.handlerWrapper(webAdmin::wrapper);
            // error handling
            config.routes.error(404, ctx -> {
                JsonBuilder jb = new JsonBuilder();
                jb.objectBegin();
                    jb.field("status", 404);
                    jb.field("message", "Not Found");
                jb.objectEnd();
                ctx.json(jb.build());
            });
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
