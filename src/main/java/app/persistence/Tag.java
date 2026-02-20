package app.persistence;

import jakarta.persistence.*;

@Entity
public class Tag
{
    @Id
    public String name;

    public Tag() {}
    public Tag(String name)
    {
        this.name = name;
    }
}
