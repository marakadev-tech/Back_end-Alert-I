package com.example.alerti_back.Controller;

import com.example.alerti_back.Model.Questions;
import com.example.alerti_back.Model.Quiz;
import com.example.alerti_back.Model.Reponses;
import com.example.alerti_back.Service.QuizService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {



    private  final QuizService quizService;

    // Injection du service
    public QuizController(QuizService sensorService) {
        this.quizService = sensorService;
    }

    @PostMapping("/add-quiz")
    public ResponseEntity<?> addQuiz(@RequestBody Quiz quiz) {
        quizService.addQuiz(quiz);
        return ResponseEntity.ok("Quiz ajouté avec succès");
    }

    @PostMapping("/{quizId}/add-question")
    public ResponseEntity<String> addQuestionToQuiz(@PathVariable int quizId, @RequestBody Questions question) {
        quizService.addQuestionToQuiz(quizId, question);
        return ResponseEntity.ok("Question ajoutée avec succès !");
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
