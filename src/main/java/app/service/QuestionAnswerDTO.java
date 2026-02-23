package app.service;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QuestionAnswerDTO
{
    @JsonProperty("answer")
    public String answer;
    @JsonProperty("points")
    public  int points;

    public QuestionAnswerDTO() {}
    public QuestionAnswerDTO(String answer, int points)
    {
        this.answer = answer;
        this.points = points;
    }
}
