package app.web;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import app.db.DBContext;
import app.db.TestData;
import app.security.JWTToken;
import io.javalin.Javalin;
import io.restassured.RestAssured;

/**
 * Base Web test.
 * Custom junit extension
 */
public class WebTest
        implements BeforeAllCallback, AutoCloseable
{
    static String JWT_TOKEN;

    private static DBContext db;
    private Javalin app;

    private static boolean started = false;

    @Override
    public void beforeAll(ExtensionContext context)
            throws Exception
    {
        if (started)
            return;
        started = true;

        String username = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");
        String url      = System.getenv("DB_URL");
        String secret   = System.getenv("SECRET_KEY");
        db = DBContext.createTEST(username, password, url);
        TestData.populate(db);
        app = Web.newJavalinApp(db, secret);

        // register callback
        context.getRoot().getStore(GLOBAL).put("WebTest", this);

        app.start("127.0.0.1", 7080);
        RestAssured.baseURI = "http://localhost:7080";

        JWT_TOKEN = JWTToken.create("1", secret);

    }

    @Override
    public void close()
    {
        app.stop();
    }
}
