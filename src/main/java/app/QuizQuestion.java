package app;

import jakarta.persistence.*;

@Entity
class QuizQuestion
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    Quiz quiz;

    @ManyToOne(cascade = CascadeType.PERSIST)
    Question question;

    QuizQuestion() {}
    QuizQuestion(Quiz quiz, Question question)
    {
        this.quiz = quiz;
        this.question = question;
    }
}
