package app;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Objects;

@Entity
class Tag
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    Tag() {}
    Tag(String name)
    {
        this.name = name;
    }
}
