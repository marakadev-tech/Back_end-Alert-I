package com.example.alerti_back.Service;

import com.example.alerti_back.Model.Questions;
import com.example.alerti_back.Model.Quiz;
import com.example.alerti_back.Model.Reponses;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuizService {

    @Value("https://wpmowqykjelftkptiquf.supabase.co")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    private final RestTemplate restTemplate = new RestTemplate();

     public void addOnlyQuiz(Quiz quiz){

         String quizUrl = supabaseUrl + "/rest/v1/quiz";

         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON);
         headers.set("apikey", supabaseKey);
         headers.set("Authorization", "Bearer " + supabaseKey);
         headers.set("Prefer", "return=representation");

         Map<String, Object> quizbody=new HashMap<>();
         quizbody.put("description", quiz.getDescription());

         HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(quizbody, headers);

         ResponseEntity<Quiz[]> response= restTemplate.exchange(
                 quizUrl,
                 HttpMethod.POST,
                 requestEntity,
                 Quiz[].class
         );


    }

    public void addQuiz(Quiz quiz) {
        // 1. Ajouter le quiz
        String quizUrl = supabaseUrl + "/rest/v1/quiz";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("Prefer", "return=representation");


        Map<String, Object> quizBody = new HashMap<>();
        quizBody.put("description", quiz.getDescription());

        HttpEntity<Map<String, Object>> quizRequest = new HttpEntity<>(quizBody, headers);

        ResponseEntity<List<Map<String, Object>>> quizResponse = restTemplate.exchange(
                quizUrl,
                HttpMethod.POST,
                quizRequest,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {
                }
        );

        int quizId = (Integer) quizResponse.getBody().get(0).get("id");

        // 2. Ajouter chaque question
        for (Questions question : quiz.getQuestions()) {
            String questionUrl = supabaseUrl + "/rest/v1/questions";

            Map<String, Object> questionBody = new HashMap<>();
            questionBody.put("textequestion", question.getTextequestion());
            questionBody.put("quiz_id", quizId);

            HttpEntity<Map<String, Object>> questionRequest = new HttpEntity<>(questionBody, headers);

            ResponseEntity<List<Map<String, Object>>> questionResponse = restTemplate.exchange(
                    questionUrl,
                    HttpMethod.POST,
                    questionRequest,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    }
            );

            int questionId = (Integer) questionResponse.getBody().get(0).get("id");

            // 3. Ajouter les réponses associées
            for (Reponses reponse : question.getReponses()) {
                String reponseUrl = supabaseUrl + "/rest/v1/reponses";

                Map<String, Object> reponseBody = new HashMap<>();
                reponseBody.put("reponse1", reponse.getReponse1());
                reponseBody.put("reponse2", reponse.getReponse2());
                reponseBody.put("reponse3", reponse.getReponse3());
                reponseBody.put("estCorrect", reponse.getEstCorrect());
                reponseBody.put("question_id", questionId);

                HttpEntity<Map<String, Object>> reponseRequest = new HttpEntity<>(reponseBody, headers);

                restTemplate.exchange(
                        reponseUrl,
                        HttpMethod.POST,
                        reponseRequest,
                        new ParameterizedTypeReference<List<Map<String, Object>>>() {
                        }
                );
            }
        }
    }

    public void addQuestionToQuiz(int quizId, Questions question) {
        String questionUrl = supabaseUrl + "/rest/v1/questions";
        String reponseUrl = supabaseUrl + "/rest/v1/reponses";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("Prefer", "return=representation");

        // 1. Ajouter la question liée au quiz
        Map<String, Object> questionBody = new HashMap<>();
        questionBody.put("textequestion", question.getTextequestion());
        questionBody.put("quiz_id", quizId);

        HttpEntity<Map<String, Object>> questionRequest = new HttpEntity<>(questionBody, headers);

        ResponseEntity<List<Map<String, Object>>> questionResponse = restTemplate.exchange(
                questionUrl,
                HttpMethod.POST,
                questionRequest,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {
                }
        );

        int questionId = (Integer) questionResponse.getBody().get(0).get("id");

        // 2. Ajouter les réponses liées à cette question
        for (Reponses reponse : question.getReponses()) {
            Map<String, Object> reponseBody = new HashMap<>();
            reponseBody.put("reponse1", reponse.getReponse1());
            reponseBody.put("reponse2", reponse.getReponse2());
            reponseBody.put("reponse3", reponse.getReponse3());
            reponseBody.put("estCorrect", reponse.getEstCorrect());
            reponseBody.put("question_id", questionId);

            HttpEntity<Map<String, Object>> reponseRequest = new HttpEntity<>(reponseBody, headers);

            restTemplate.exchange(
                    reponseUrl,
                    HttpMethod.POST,
                    reponseRequest,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    }
            );
        }
    }

    // Ma méthode pour récuperer les quiz
    public List<Quiz> getAllQuiz() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer" + supabaseKey);
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = supabaseUrl + "/rest/v1/quiz?select=*";

        ResponseEntity<Quiz[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Quiz[].class
        );
        return Arrays.asList(response.getBody());
    }

    public List<Questions> getQuestionsByQuizId (String quizId){
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer" + supabaseKey);
        headers.set("Accept", "application/json" );

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = supabaseUrl +"/rest/v1/questions?quiz_id=eq." + quizId + "&select=*";

        ResponseEntity<Questions[]> response= restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Questions[].class
        );
        return Arrays.asList(response.getBody());

    }

    public List<Reponses> getReponsesByQuestions (String questionId) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer" + supabaseKey);
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = supabaseUrl + "/rest/v1/reponses?question_id=eq." + questionId + "&select=*";

        ResponseEntity<Reponses[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Reponses[].class
        );
    return Arrays.asList(response.getBody());

    }



}
