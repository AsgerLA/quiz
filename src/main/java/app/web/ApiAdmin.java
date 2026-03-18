package app.web;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.Map;

import app.db.Category;
import app.db.DBContext;
import app.db.DBException;
import app.db.Tag;
import app.web.json.JsonBuilder;
import app.web.json.JsonException;
import app.web.json.JsonObject;
import app.web.json.JsonParser;

class ApiAdmin
{
    static String getMetrics(Map<String, MetricData> metrics)
    {
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

        return jb.build();
    }

    static void postCategory(DBContext db, String json)
            throws APIException
    {
        try {
            JsonObject jo;
            Category cat;
            Tag tag;
            String name;

            jo = JsonParser.decodeObject(json);
            name = jo.getString("name");

            tag = new Tag(name);
            Tag.create(db, tag);
            cat = new Category(tag);
            Category.create(db, cat);

            ApiCategory.buildCache(db);
        } catch (JsonException e) {
            throw new APIException(400, e);
        } catch (DBException e) {
            throw new APIException(500, e);
        }
    }

    static void deleteCategory(DBContext db, String json)
            throws APIException
    {
        try {
            JsonObject jo;
            Category cat;
            String name;

            jo = JsonParser.decodeObject(json);
            name = jo.getString("name");

            cat = Category.loadByName(db, name);
            if (cat == null)
                throw new APIException(400, "unknown category");
            Category.delete(db, cat);

            Tag.gc(db);

            ApiCategory.buildCache(db);
        } catch (JsonException e) {
            throw new APIException(400, e);
        } catch (DBException e) {
            throw new APIException(500, e);
        }
    }
}
