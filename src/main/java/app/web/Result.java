package app.web;

import app.web.json.JsonBuilder;
import io.javalin.http.Context;

/**
 * Helper class for populating javalin context
 */
class Result
{
    static void ok(Context ctx, String json)
    {
        ctx.status(200).json(json);
    }

    static void created(Context ctx, String json)
    {
        ctx.status(203).json(json);
    }

    static void noContent(Context ctx)
    {
        ctx.status(204);
    }

    static void badRequest(Context ctx)
    {
        ctx.status(400);
    }

    static void notFound(Context ctx)
    {
        ctx.status(404);
    }

    static void unauthorized(Context ctx)
    {
        ctx.status(401);
    }

    static void conflict(Context ctx)
    {
        ctx.status(409);
    }

    static void error(Context ctx,
                      int status,
                      String message)
    {
        JsonBuilder jb = new JsonBuilder();

        jb.objectBegin();
            jb.field("message", message);
            jb.field("status", status);
            jb.field("instance", ctx.path());
        jb.objectEnd();

        ctx.status(status);
        ctx.json(jb.build());
    }

}
