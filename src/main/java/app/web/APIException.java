package app.web;

class APIException extends Exception
{
    int code;
    public APIException(int code, Exception e)
    {
        super(e);
        this.code = code;
    }
}
