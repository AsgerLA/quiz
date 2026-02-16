package app;

import jakarta.persistence.*;

@Entity
class QuestionAnswer
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String answer;
    int points;

    @ManyToOne
    Question question;

    QuestionAnswer() {}
    QuestionAnswer(String answer, int points)
    {
        this.answer = answer;
        this.points = points;
    }
}
