package app.web;

class APIException extends Exception
{
    int code;

    APIException(int code, Exception e)
    {
        super(e);
        this.code = code;
    }

    APIException(int code, String message)
    {
        super(message);
        this.code = code;
    }
}