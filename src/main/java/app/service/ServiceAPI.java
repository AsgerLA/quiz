package app.service;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Get questions from external API's
 * <p>
 * TODO: multiple API's
 */
public class ServiceAPI
{
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static String getApiData(String url)
    {
        try {
            HttpResponse<String> res;
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("accept", "application/json")
                    .GET()
                    .build();

            res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200)
                return null;
            return res.body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static class OpenTdbDTO
    {
        @JsonProperty("response_code")
        int responseCode;
        @JsonProperty()
        OpenTdbQuestionDTO[] results;
        static class OpenTdbQuestionDTO
        {
            @JsonProperty
            String type;
            @JsonProperty
            String difficulty;
            @JsonProperty
            String category;
            @JsonProperty
            String question;
            @JsonProperty("correct_answer")
            String correctAnswer;
            @JsonProperty("incorrect_answers")
            String[] incorrectAnswers;
        }
    }
    // translate opentdb.com question
    private static QuestionDTO opentdblate(OpenTdbDTO.OpenTdbQuestionDTO openTdbDTO)
    {
        QuestionDTO questionDTO = new QuestionDTO();
        questionDTO.category = openTdbDTO.category;
        questionDTO.question = openTdbDTO.question;
        questionDTO.answers =
                new QuestionAnswerDTO[1+openTdbDTO.incorrectAnswers.length];
        // TODO: shuffle answers
        questionDTO.answers[0] =
                new QuestionAnswerDTO(openTdbDTO.correctAnswer, 1);
        for (int i = 1; i < questionDTO.answers.length; i++) {
            questionDTO.answers[i] =
                    new QuestionAnswerDTO(openTdbDTO.incorrectAnswers[i-1], 0);
        }

        return questionDTO;
    }

    public static QuestionDTO[] randomQuestions(int num)
    {
        QuestionDTO[] questions;
        String json = getApiData("https://opentdb.com/api.php?amount="+num);
        try {
            // docs: https://opentdb.com/api_config.php
            OpenTdbDTO dto = Service.jsonToObject(json, OpenTdbDTO.class);
            if (dto.responseCode != 0) {
                // TODO: handle response codes
                throw new RuntimeException("responseCode: "+dto.responseCode);
            }
            questions = new QuestionDTO[dto.results.length];
            for (int i = 0; i < questions.length; i++)
                questions[i] = opentdblate(dto.results[i]);
            return questions;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
