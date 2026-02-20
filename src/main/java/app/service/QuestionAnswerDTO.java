package app.service;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QuestionAnswerDTO
{
    @JsonProperty("answer")
    public String answer;
    @JsonProperty("points")
    public  int points;
}
