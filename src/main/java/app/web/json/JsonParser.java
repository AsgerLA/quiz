package app.web.json;


public class JsonParser
{
    public static Object decode(String json)
            throws JsonException
    {
        JsonParser jp = new JsonParser(json);
        Object root = null;

        jp.next();
        jp.trim();

        if (jp.ch == '{')
            root = jp.object();
        else if (jp.ch == '[')
            root = jp.array();
        else
            jp.error("expected '{' or '[' at root");

        return root;
    }

    public static JsonObject decodeObject(String json)
            throws JsonException
    {
        JsonParser jp = new JsonParser(json);
        JsonObject root;

        jp.next();
        jp.trim();

        if (jp.ch != '{')
            jp.error("expected '{' at root");
        root = jp.object();

        return root;
    }

    public static JsonArray decodeArray(String json)
            throws JsonException
    {
        JsonParser jp = new JsonParser(json);
        JsonArray root;

        jp.next();
        jp.trim();

        if (jp.ch != '[')
            jp.error("expected '[' at root");
        root = jp.array();

        return root;
    }

    private final String text;
    private int at;
    private char ch;
    private int depth;
    private static final int MAX_DEPTH = 8;

    private final StringBuilder sb;

    private JsonParser(String json)
        throws JsonException
    {
        if (json == null)
            throw new JsonException("JSON text is null");
        sb = new StringBuilder();
        text = json;
        at = 0;
    }

    private void error(String msg)
        throws JsonException
    {
        msg += " at "+at;
        throw new JsonException(msg);
    }

    private void next()
    {
        if (at >= text.length()) {
            ch = 0;
            return;
        }
        ch = text.charAt(at++);
    }

    private void trim()
    {
        while (ch != 0 && ch <= ' ') {
            next();
        }
    }

    private String string()
        throws JsonException
    {
        sb.setLength(0);

        if (ch != '"')
            error("expected opening '\"'");
        while (ch != 0) {
            next();
            if (ch == '\\') {
                sb.append(ch);
                next();
                sb.append(ch);
                continue;
            }
            if (ch == '"' || ch == '\n')
                break;
            sb.append(ch);
        }
        if (ch != '"')
            error("expected closing '\"'");
        return sb.toString();
    }

    private Number number()
        throws JsonException
    {
        boolean isReal = false;

        sb.setLength(0);

        sb.append(ch);
        while (ch != 0) {
            next();
            trim();
            if (ch == '.') {
                isReal = true;
                sb.append(ch);
                continue;
            } else if (ch >= '0' && ch <= '9') {
                sb.append(ch);
                continue;
            }
            at--;

            String s = sb.toString();
            if (isReal)
                return Double.parseDouble(s);
            return Long.parseLong(s);
        }

        error("unexpected end of number");
        return null;
    }

    // boolean or null
    private Object other()
        throws JsonException
    {
        String word;

        if (at+4 >= text.length())
            error("unexpected token");

        word = text.substring(at-1, at+3);
        at += 3;
        switch (word) {
            case "true": {
                return true;
            }
            case "fals": {
                next();
                if (ch == 'e')
                    return false;
                break;
            }
            case "null": {
                return null;
            }
        }

        error("unexpected value "+word);
        return null;
    }

    private Object value()
        throws JsonException
    {
        next();
        trim();
        if (ch == '"') {
            return string();
        } else if (ch == '{') {
            return object();
        } else if (ch == '[') {
            return array();
        } else if ((ch >= '0' && ch <= '9') || ch == '-') {
            return number();
        } else {
            return other();
        }
    }

    private JsonArray array()
        throws JsonException
    {
        JsonArray a = new JsonArray();
        Object v;

        next();
        if (ch == ']')
            return a;
        at--;

        depth++;
        if (depth > MAX_DEPTH)
            error("json is too deeply nested");

        while (ch != 0) {
            v = value();
            a.add(v);
            next();
            trim();
            if (ch != ',')
                break;
        }
        if (ch != ']')
            error("expected ',' or ']'");

        depth--;

        return a;
    }

    private JsonObject object()
        throws JsonException
    {
        JsonObject o = new JsonObject();
        String k;
        Object v;

        next();
        if (ch == '}')
            return o;
        at--;

        depth++;
        if (depth > MAX_DEPTH)
            error("json is too deeply nested");

        while (ch != 0) {
            next();
            trim();
            k = string();

            next();
            trim();
            if (ch != ':')
                error("expected ':'");

            v = value();

            o.put(k, v);

            next();
            trim();
            if (ch != ',')
                break;
        }
        if (ch != '}')
            error("expected ',' or '}'");

        depth--;

        return o;
    }

}
