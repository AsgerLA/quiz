package app;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
class QuizTag
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    Quiz quiz;

    @ManyToOne
    Tag tag;

    QuizTag() {}
    QuizTag(Quiz quiz, Tag tag)
    {
        this.quiz = quiz;
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        QuizTag quizTag = (QuizTag) o;
        return Objects.equals(id, quizTag.id) && Objects.equals(quiz.id, quizTag.quiz.id) && Objects.equals(tag.id, quizTag.tag.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, quiz.id, tag.id);
    }
}
