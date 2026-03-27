package app;

import app.db.DBContext;
import app.web.Web;
import io.javalin.Javalin;

public class Main
{
    public static void main(String[] args)
            throws Exception
    {
        String username = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");
        String url      = System.getenv("DB_URL");
        String secret   = System.getenv("SECRET_KEY");
        DBContext db = DBContext.create(username, password, url);
        Javalin app = Web.newJavalinApp(db, secret);
        app.start("127.0.0.1", 7070);
        System.out.println("http://localhost:7070");
    }
}
