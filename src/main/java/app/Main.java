package app;

import app.db.DBContext;
import app.db.DBException;
import app.web.Web;
import io.javalin.Javalin;

public class Main
{
    public static void main(String[] args)
            throws DBException
    {
        String username = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");
        String url      = System.getenv("DB_URL");
        DBContext db = new DBContext(username, password, url);
        Javalin app = Web.newJavalinApp(db);
        app.start("127.0.0.1", 7070);
    }
}
