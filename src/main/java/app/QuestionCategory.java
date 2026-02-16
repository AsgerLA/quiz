package app;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
class QuestionCategory
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    QuestionCategory() {}
    QuestionCategory(String name)
    {
        this.name = name;
    }
}
