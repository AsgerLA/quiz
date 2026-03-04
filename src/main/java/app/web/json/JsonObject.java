package app.web.json;

import java.util.Map;
import java.util.HashMap;

public class JsonObject
{
    private Map<String, Object> fields;

    public JsonObject()
    {
        fields = new HashMap<>();
    }

    public void put(String key, Object o)
    {
        fields.put(key, o);
    }

    public Object get(String key)
    {
        return fields.get(key);
    }

    public int getInt(String key)
        throws JsonException
    {
        Object o = get(key);
        if (o instanceof Number)
            return ((Number)o).intValue();
        throw new JsonException("Expected int "+key);
    }

    public long getLong(String key)
        throws JsonException
    {
        Object o = get(key);
        if (o instanceof Number)
            return ((Number)o).longValue();
        throw new JsonException("Expected long "+key);
    }

    public float getFloat(String key)
        throws JsonException
    {
        Object o = get(key);
        if (o instanceof Number)
            return ((Number)o).floatValue();
        throw new JsonException("Expected float "+key);
    }

    public double getDouble(String key)
        throws JsonException
    {
        Object o = get(key);
        if (o instanceof Number)
            return ((Number)o).doubleValue();
        throw new JsonException("Expected double "+key);
    }

    public boolean getBoolean(String key)
        throws JsonException
    {
        Object o = get(key);
        if (o instanceof Boolean)
            return (boolean)o;
        throw new JsonException("Expected boolean "+key);
    }

    public String getString(String key)
        throws JsonException
    {
        Object o = get(key);
        if (o instanceof String)
            return (String)o;
        throw new JsonException("Expected String "+key);
    }

    public JsonObject getJsonObject(String key)
        throws JsonException
    {
        Object o = get(key);
        if (o instanceof JsonObject)
            return (JsonObject)o;
        throw new JsonException("Expected JsonObject "+key);
    }

    public JsonArray getJsonArray(String key)
        throws JsonException
    {
        Object o = get(key);
        if (o instanceof JsonArray)
            return (JsonArray)o;
        throw new JsonException("Expected JsonArray "+key);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(1024);
        String k;
        Object v;

        sb.append('{');
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            k = entry.getKey();
            v = entry.getValue();
            sb.append('"');
            sb.append(k);
            sb.append("\":");
            if (v == null) {
                sb.append("null");
            } else if (v instanceof String) {
                sb.append('"');
                sb.append(v);
                sb.append('"');
            } else {
                sb.append(v);
            }
            sb.append(',');
        }
        sb.setLength(sb.length()-1);
        sb.append('}');

        return sb.toString();
    }
}
