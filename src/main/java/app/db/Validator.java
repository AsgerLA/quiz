package app.db;

import java.nio.charset.StandardCharsets;

public class Validator
{
    public static boolean verifyString(String s)
    {
        int at;
        char c;

        if (s.isEmpty() || s.length() >= 256)
            return false;

        s = new String(s.getBytes(), StandardCharsets.UTF_8);
        for (at = 0; at < s.length(); at++) {
            c = s.charAt(at);
            if (!isalpha(c) && !isdigit(c) && c != ' ') {
                return false;
            }
        }
        return true;
    }

    public static boolean verifyTag(String s)
    {
        int at;
        char c;

        if (s.isEmpty() || s.length() >= 256)
            return false;

        s = new String(s.getBytes(), StandardCharsets.UTF_8);
        if (!isalpha(s.charAt(0)))
            return false;
        for (at = 1; at < s.length(); at++) {
            c = s.charAt(at);
            if (!isalpha(c) && !isdigit(c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isdigit(char c)
    {
        return (c >= '0' && c <= '9');
    }

    private static boolean isalpha(char c)
    {
        return ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
                (c == '_') ||
                (c >= 0xc0 && c <= 0xff && c != 0xf7 && c != 0xd7));
    }
}
