package app;

public class APIException extends RuntimeException
{
    public int code;

    public APIException(int code, String msg)
    {
        super(msg);
        this.code = code;
    }
}
