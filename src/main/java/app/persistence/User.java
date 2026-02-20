package app.persistence;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "users")
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String name;
    byte[] password;
    byte[] salt;

    public Instant created;

    public Instant lastLogin;

    @PrePersist
    void prePersist()
    {
        created = Instant.now();
    }
}

