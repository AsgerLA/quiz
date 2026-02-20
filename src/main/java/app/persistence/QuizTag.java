package app.persistence;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class QuizTag
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    public Quiz quiz;

    @ManyToOne
    public Tag tag;

    public QuizTag() {}
    public QuizTag(Quiz quiz, Tag tag)
    {
        this.quiz = quiz;
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        QuizTag quizTag = (QuizTag) o;
        return Objects.equals(id, quizTag.id) && Objects.equals(quiz.id, quizTag.quiz.id) && Objects.equals(tag.name, quizTag.tag.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, quiz.id, tag.name);
    }
}
