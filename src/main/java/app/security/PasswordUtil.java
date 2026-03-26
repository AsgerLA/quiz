package app.security;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil
{
    public static String hashpw(String password)
    {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public static boolean verifypw(String plaintext, String hashed)
    {
        return BCrypt.checkpw(plaintext, hashed);
    }
}
