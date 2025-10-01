package com.example.alerti_back.Controller;

import com.example.alerti_back.Model.Questions;
import com.example.alerti_back.Model.Quiz;
import com.example.alerti_back.Model.Reponses;
import com.example.alerti_back.Service.QuizService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {



    private  final QuizService quizService;

    // Injection du service
    public QuizController(QuizService sensorService) {
        this.quizService = sensorService;
    }

    @PostMapping("/addonlyquiz")
    public ResponseEntity<Map<String, String>> onlyquiz(@RequestBody Quiz quiz) {
        quizService.addOnlyQuiz(quiz);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Quiz ajouté avec succès");

        return ResponseEntity.ok(response);
    }




    @PostMapping("/add-quiz")
    public ResponseEntity<?> addQuiz(@RequestBody Quiz quiz) {
        quizService.addQuiz(quiz);
        return ResponseEntity.ok("Quiz ajouté avec succès");
    }



    @PostMapping("/{quizId}/add-question")
    public ResponseEntity<Map<String, String>> addQuestionToQuiz(@PathVariable int quizId, @RequestBody Questions question) {
        quizService.addQuestionToQuiz(quizId, question);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Question ajoutée avec succès !");
        return ResponseEntity.ok(response);
    }


    @GetMapping("/getAllquiz")
    public ResponseEntity<List<Quiz>>getAllquiz(){
        return ResponseEntity.ok(quizService.getAllQuiz());
    }

    @GetMapping("/{id}/question")
    public  ResponseEntity <List<Questions>> getQuestionsByQuiz(@PathVariable String id) {
        return ResponseEntity.ok(quizService.getQuestionsByQuizId(id));
    }

    @GetMapping("/{id}/reponses")
    public ResponseEntity<List<Reponses>> getReponsesByQuestions(@PathVariable String id){
        return ResponseEntity.ok(quizService.getReponsesByQuestions(id));
    }


}
