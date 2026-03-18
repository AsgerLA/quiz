package app.web.json;

public class JsonBuilder
{
    StringBuilder sb = new StringBuilder(1024);

    public void objectBegin()
    {
        sb.append('{');
    }

    public void objectBegin(String key)
    {
        sb.append('"');
        sb.append(key);
        sb.append("\":{");
    }

    public void field(String key, Object value)
    {
        sb.append('"');
        sb.append(key);
        sb.append("\":");
        value(value);
    }

    public void field(String key, String value)
    {
        sb.append('"');
        sb.append(key);
        sb.append("\":");
        value(value);
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

    public void value(Object value)
    {
        if (value == null) {
            sb.append("null");
        } else {
            sb.append(value);
        }
        sb.append(',');
    }

    public void value(String value)
    {
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

    public void arrayBegin(String key)
    {
        sb.append('"');
        sb.append(key);
        sb.append("\":[");
    }

    public void arrayEnd()
    {
        if (sb.charAt(sb.length()-1) != '[')
            sb.setLength(sb.length()-1);
        sb.append("],");
    }

    public void json(String json)
    {
        sb.append(json);
        sb.append(',');
    }

    public String build()
    {
        sb.setLength(sb.length()-1);
        return sb.toString();
    }
}
