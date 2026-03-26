package app.db;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Question
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(nullable = false)
    public String question;

    @Column(nullable = false)
    public int slot;

    public enum Type
    {
        SINGLE,
        MULTI,
    }

    public Type type;

    @OneToMany(mappedBy = "question", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    public Set<Answer> answers = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    Quiz quiz;

    public Question() {}
    public Question(Quiz quiz, String question, int slot, Type type)
    {
        this.quiz = quiz;
        this.question = question;
        this.slot = slot;
        this.type = type;
    }

    @Override
    public int hashCode()
    {
        return slot;
    }
}
