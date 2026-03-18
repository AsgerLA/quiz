package app.web;

import static io.javalin.apibuilder.ApiBuilder.beforeMatched;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import app.db.DBContext;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.router.Endpoint;

class WebAdmin
{
    WebAdmin(DBContext db)
    {
        this.db = db;
    }
    private DBContext db;

    EndpointGroup routes()
    {
        return () -> {
            beforeMatched("/api/admin*", this::ensureAdmin);
            get("/api/admin/metrics", this::GET_metrics);
            delete("/api/admin/cache", this::DELETE_cache);
            post("/api/admin/category", this::POST_category);
            delete("/api/admin/category", this::DELETE_category);
        };
    }

    private Map<String, MetricData> metrics = new HashMap<>();

    synchronized void putMetric(String key, long starttime)
    {
        MetricData data = metrics.get(key);
        if (data == null)
            data = new MetricData();
        data.hits++;

        data.time += System.nanoTime() - starttime;
        metrics.put(key, data);
    }

    Handler wrapper(Endpoint endpoint)
    {
        if (endpoint.method.isHttpMethod()) {
            return ctx -> {
                long starttime = System.nanoTime();

                endpoint.handler.handle(ctx);
                String key = endpoint.method.toString()+" "+endpoint.path;
                putMetric(key, starttime);
            };
        }
        return endpoint.handler;
    }

    private int findchar(String s, char delim)
    {
        int i;

        i = 0;
        while (i++ < s.length()-1 && s.charAt(i) != delim) {}
        return i;
    }
    private void throwAuthException(Context ctx)
        throws APIException
    {
        ctx.header("WWW-Authenticate", "Basic realm=\"User Visible Realm\"");
        throw new APIException(401, "unautherized");
    }
    void ensureAdmin(Context ctx)
        throws APIException
    {
        String value = ctx.header("Authorization");
        if (value != null) {
            int i;
            i = findchar(value, ' ');
            if (i+1 >= value.length())
                throwAuthException(ctx);
            value = value.substring(i+1, value.length());
            value = new String(Base64.getDecoder().decode(value),
                    StandardCharsets.UTF_8);
            i = findchar(value, ':');
            if (i+1 >= value.length())
                throwAuthException(ctx);
            String username = value.substring(0, i);
            String password = value.substring(i+1, value.length());

            // FIXME: admin password
            if (username.equals("admin") &&
                    password.equals("1234"))
                return;
        }
        throwAuthException(ctx);
    }

    void GET_metrics(Context ctx)
    {
        String json;

        json = ApiAdmin.getMetrics(metrics);

        ctx.json(json);
    }

    void DELETE_cache(Context ctx)
            throws APIException
    {
        ApiCategory.buildCache(db);
    }

    void POST_category(Context ctx)
            throws APIException
    {
        String json;

        json = ctx.body();
        ApiAdmin.postCategory(db, json);

        ctx.status(201);
    }

    void DELETE_category(Context ctx)
            throws APIException
    {
        String json;

        json = ctx.body();
        ApiAdmin.deleteCategory(db, json);

        ctx.status(204);
    }
}
