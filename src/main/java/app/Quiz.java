package app;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
class Quiz
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String title;
    String description;

    Instant created;
    Instant updated;

    @ManyToOne
    User createdBy;

    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    Set<QuizQuestion> questions = new HashSet<>();

    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    Set<QuizTag> quiztags = new HashSet<>();

    Quiz() {}
    Quiz(String title, String description)
    {
        this.title = title;
        this.description = description;
    }

    void addQuestion(Question question)
    {
        QuizQuestion qq = new QuizQuestion(this, question);
        questions.add(qq);
    }

    void addTag(Tag tag)
    {
        QuizTag qt = new QuizTag(this, tag);
        quiztags.add(qt);
    }

    @PrePersist
    void prePersist()
    {
        created = Instant.now();
        updated = created;
    }

    @PreUpdate
    void preUpdate()
    {
        updated = Instant.now();
    }
}
