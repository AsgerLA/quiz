package app;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
class Question
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String question;

    Instant created;

    @ManyToOne(fetch = FetchType.EAGER)
    QuestionCategory category;

    @OneToMany(mappedBy = "question", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    Set<QuestionAnswer> answers = new HashSet<>();

    Question() {}
    Question(String question, QuestionCategory category)
    {
        this.question = question;
        this.category = category;
    }

    void addAnswer(QuestionAnswer answer)
    {
        answers.add(answer);
        answer.question = this;
    }

    @PrePersist
    void prePersist()
    {
        created = Instant.now();
    }
}
