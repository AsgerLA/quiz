package app.db;

import jakarta.persistence.*;

@Entity
public class Answer
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(nullable = false)
    public String answer;
    public int points;
    public int slot;

    @ManyToOne
    @JoinColumn(nullable = false)
    Question question;

    public Answer() {}
    public Answer(Question question, String answer, int points, int slot)
    {
        this.question = question;
        this.answer = answer;
        this.points = points;
        this.slot = slot;
    }

    @Override
    public int hashCode()
    {
        return slot;
    }
}
