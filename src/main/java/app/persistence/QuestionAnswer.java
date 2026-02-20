package app.persistence;

import jakarta.persistence.*;

@Entity
public class QuestionAnswer
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String answer;
    public int points;

    @ManyToOne
    public Question question;

    public QuestionAnswer() {}
    public QuestionAnswer(String answer, int points)
    {
        this.answer = answer;
        this.points = points;
    }
}
