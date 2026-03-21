package app.web;

import static io.javalin.apibuilder.ApiBuilder.get;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

import app.db.DBContext;
import app.web.json.JsonBuilder;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.router.Endpoint;

class WebMetric
{
    WebMetric(DBContext db, WebSecurity security)
    {
        this.db = db;
        this.security = security;
    }
    private DBContext db;
    private WebSecurity security;

    EndpointGroup routes()
    {
        return () -> {
            get("/api/metrics", this::GET_metrics);
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

    void GET_metrics(Context ctx)
    {
        String json;

        if (!security.authorize(ctx, true))
            return;

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

        jb.arrayBegin("routes");
        for (Map.Entry<String, MetricData> entry : metrics.entrySet()) {
            MetricData data = entry.getValue();
            jb.objectBegin();
                jb.field("endpoint", entry.getKey());
                jb.field("time", data.time/1000_000.0);
                jb.field("hits", data.hits);
                jb.field("avg", (data.time/data.hits)/1000_000.0);
            jb.objectEnd();
        }
        jb.arrayEnd();

        jb.objectEnd();

        json = jb.build();

        Result.ok(ctx, json);
    }
}
