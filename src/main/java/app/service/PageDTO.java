package app.service;

public class PageDTO<T>
{
    public int page;
    public int totalPages;
    public int totalResults;

    public T[] results;
}
