package app.web.json;

public class JsonBuilder
{
    StringBuilder sb = new StringBuilder();

    public void objectBegin()
    {
        sb.append('{');
    }

    public void objectBegin(String key)
    {
        sb.append(key);
        sb.append(":{");
    }

    public void field(String key, Object value)
    {
        sb.append('"');
        sb.append(key);
        sb.append("\":");
        if (value == null) {
            sb.append("null");
        } else {
            sb.append(value);
        }
        sb.append(',');
    }

    public void field(String key, String value)
    {
        sb.append('"');
        sb.append(key);
        sb.append("\":");
        if (value == null) {
            sb.append("null");
        } else {
            sb.append('"');
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if (c == '\n')
                    sb.append("\\n");
                else if (c == '"')
                    sb.append("\\\"");
                else
                    sb.append(c);
            }
            sb.append('"');
        }
        sb.append(',');
    }

    public void objectEnd()
    {
        if (sb.charAt(sb.length()-1) != '{')
            sb.setLength(sb.length()-1);
        sb.append("},");
    }

    public void arrayBegin()
    {
        sb.append('[');
    }

    public void arrayBegin(String key)
    {
        sb.append(key);
        sb.append(":[");
    }

    public void arrayEnd()
    {
        if (sb.charAt(sb.length()-1) != '[')
            sb.setLength(sb.length()-1);
        sb.append("],");
    }

    public void key(String key)
    {
        sb.append(key);
        sb.append(':');
    }

    public void value(Object value)
    {
        sb.append(value);
        sb.append(',');
    }

    public void append(String json)
    {
        sb.append(json);
        sb.append(',');
    }

    public String build()
    {
        sb.setLength(sb.length()-1);
        return sb.toString();
    }

    @Override
    public String toString()
    {
        return sb.toString();
    }

}
