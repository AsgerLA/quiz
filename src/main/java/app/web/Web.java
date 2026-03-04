package app.web;

import app.db.*;
import io.javalin.Javalin;

class Web
{
    static DBContext db;

    public static void main(String[] args)
            throws DBException
    {
        String username = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");
        String url      = System.getenv("DB_URL");
        db = new DBContext(username, password, url);

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

        app.start("127.0.0.1", 7070);

    }
}
