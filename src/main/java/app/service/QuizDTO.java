package app.service;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QuizDTO
{
    @JsonProperty
    public Long id;
    @JsonProperty("description")
    public String description;
    @JsonProperty("title")
    public String title;
    @JsonProperty("tags")
    public String[] tags;
    @JsonProperty("questions")
    public QuestionDTO[] questions;

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof QuizDTO b))
            return false;
        QuizDTO a = this;
        return a.id.equals(b.id);
    }
}
