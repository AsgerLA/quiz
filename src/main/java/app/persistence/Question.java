package app.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Question
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String question;

    public Instant created;

    @ManyToOne
    public Quiz quiz;

    @ManyToOne(fetch = FetchType.EAGER)
    public QuestionCategory category;

    @OneToMany(mappedBy = "question", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    public Set<QuestionAnswer> answers = new HashSet<>();

    public Question() {}
    public Question(String question, QuestionCategory category)
    {
        this.question = question;
        this.category = category;
    }

    public void addAnswer(QuestionAnswer answer)
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
