package app;

import jakarta.persistence.*;
import lombok.ToString;

import java.time.Instant;

@Entity
@Table(name = "users")
@ToString
class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String name;
    byte[] password;
    byte[] salt;

    Instant created;

    @Column(name="last_login")
    Instant lastLogin;

    @PostPersist
    void postPersist()
    {
        created = Instant.now();
    }
}
