package app.web;

import app.db.DBContext;
import io.javalin.Javalin;

public class Web
{
    public static Javalin newJavalinApp(DBContext db)
    {
        WebSecurity webSecurity = new WebSecurity(db);
        WebQuiz webQuiz = new WebQuiz(db, webSecurity);
        WebCategory webCategory = new WebCategory(db, webSecurity);
        WebAccount webAccount = new WebAccount(db);
        WebMetric webMetric = new WebMetric(db, webSecurity);

        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableRouteOverview("/routes");
            // routes
            config.routes.apiBuilder(webSecurity.routes());
            config.routes.apiBuilder(webQuiz.routes());
            config.routes.apiBuilder(webCategory.routes());
            config.routes.apiBuilder(webAccount.routes());
            config.routes.apiBuilder(webMetric.routes());
            config.router.handlerWrapper(webMetric::wrapper);
            // error handling
            config.routes.error(404, ctx -> {
                Result.error(ctx, 404, "no endpoint");
            });
            config.routes.exception(Exception.class, (e, ctx) -> {
                e.printStackTrace();
                ctx.status(500);
            });
        });

        return app;
    }
}
