package app;

import jakarta.persistence.*;
import lombok.ToString;

import java.time.Instant;

@ToString
@Entity
@Table(name = "users")
class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;
    byte[] password;
    byte[] salt;

    Instant created;

    Instant lastLogin;

    @PrePersist
    void prePersist()
    {
        created = Instant.now();
    }
}

