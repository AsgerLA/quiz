package app.web;

import static io.javalin.apibuilder.ApiBuilder.beforeMatched;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.delete;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import app.db.DBContext;
import app.web.json.JsonBuilder;
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
        };
    }

    static class MetricData
    {
        long time;
        int  hits;
    }
    private Map<String, MetricData> metrics = new HashMap<>();
    private ReentrantLock lock = new ReentrantLock();

    void putMetric(String key, long starttime, int status)
    {
        lock.lock();
        try {
            MetricData data = metrics.get(key);
            if (data == null)
                data = new MetricData();
            data.hits++;

            data.time += System.nanoTime() - starttime;
            metrics.put(key, data);
        } finally {
            lock.unlock();
        }
    }

    Handler wrapper(Endpoint endpoint)
    {
        if (endpoint.method.isHttpMethod()) {
            return ctx -> {
                long starttime = System.nanoTime();

                endpoint.handler.handle(ctx);

                String key = endpoint.method.toString()+" "+ctx.path();
                putMetric(key, starttime, ctx.status().getCode());
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
        Runtime rt = Runtime.getRuntime();
        OperatingSystemMXBean osbean = ManagementFactory.getOperatingSystemMXBean();
        RuntimeMXBean rtbean = ManagementFactory.getRuntimeMXBean();

        JsonBuilder jb = new JsonBuilder();

        jb.objectBegin();

        // memory
        jb.field("total", rt.totalMemory());
        jb.field("free", rt.freeMemory());

        // cpu
        jb.field("load_average", osbean.getSystemLoadAverage());

        jb.field("uptime", rtbean.getUptime());

        jb.objectBegin("routes");
        for (Map.Entry<String, MetricData> entry : metrics.entrySet()) {
            MetricData data = entry.getValue();
            jb.objectBegin(entry.getKey());
            jb.field("time", data.time/1000_000.0);
            jb.field("hits", data.hits);
            jb.field("avg", (data.time/data.hits)/1000_000.0);
            jb.objectEnd();
        }
        jb.objectEnd();

        jb.objectEnd();

        json = jb.build();

        ctx.json(json);
    }

    void DELETE_cache(Context ctx)
        throws APIException
    {
        ApiCategory.buildCache(db);
    }
}
