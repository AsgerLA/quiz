package app.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Quiz
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String title;
    public String description;

    public Instant created;
    public Instant updated;

    @ManyToOne
    public User createdBy;

    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    public Set<Question> questions = new HashSet<>();

    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public Set<QuizTag> quiztags = new HashSet<>();

    public Quiz() {}
    public Quiz(String title, String description)
    {
        this.title = title;
        this.description = description;
    }

    public void addQuestion(Question question)
    {
        question.quiz = this;
        questions.add(question);
    }

    public void addTag(Tag tag)
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
