package app.service;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QuestionDTO
{
    @JsonProperty("answers")
    public QuestionAnswerDTO[] answers;
    @JsonProperty("category")
    public String category;
    @JsonProperty("question")
    public String question;
}
