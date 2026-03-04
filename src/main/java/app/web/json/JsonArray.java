package app.web.json;

import java.util.List;
import java.util.ArrayList;

public class JsonArray
{
    private List<Object> elements;

    public JsonArray()
    {
        elements = new ArrayList<>();
    }

    public int size()
    {
        return elements.size();
    }

    public void add(Object o)
    {
        elements.add(o);
    }

    public Object get(int index)
    {
        return elements.get(index);
    }

    public int getInt(int index)
        throws JsonException
    {
        Object o = get(index);
        if (o instanceof Number)
            return ((Number)o).intValue();
        throw new JsonException("Expected int "+index);
    }

    public long getLong(int index)
        throws JsonException
    {
        Object o = get(index);
        if (o instanceof Number)
            return ((Number)o).longValue();
        throw new JsonException("Expected long "+index);
    }

    public float getFloat(int index)
        throws JsonException
    {
        Object o = get(index);
        if (o instanceof Number)
            return ((Number)o).floatValue();
        throw new JsonException("Expected float "+index);
    }

    public double getDouble(int index)
        throws JsonException
    {
        Object o = get(index);
        if (o instanceof Number)
            return ((Number)o).doubleValue();
        throw new JsonException("Expected double "+index);
    }

    public boolean getBoolean(int index)
        throws JsonException
    {
        Object o = get(index);
        if (o instanceof Boolean)
            return (boolean)o;
        throw new JsonException("Expected boolean "+index);
    }

    public String getString(int index)
        throws JsonException
    {
        Object o = get(index);
        if (o instanceof String)
            return (String)o;
        throw new JsonException("Expected String "+index);
    }

    public JsonObject getJsonObject(int index)
        throws JsonException
    {
        Object o = get(index);
        if (o instanceof JsonObject)
            return (JsonObject)o;
        throw new JsonException("Expected JsonObject "+index);
    }

    public JsonArray getJsonArray(int index)
        throws JsonException
    {
        Object o = get(index);
        if (o instanceof JsonArray)
            return (JsonArray)o;
        throw new JsonException("Expected JsonArray "+index);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(1024);

        sb.append('[');
        for (Object elem : elements) {
            sb.append(elem);
            sb.append(',');
        }
        sb.setLength(sb.length()-1);
        sb.append(']');

        return sb.toString();
    }
}
