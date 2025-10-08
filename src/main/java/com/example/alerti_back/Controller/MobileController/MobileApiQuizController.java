package com.example.alerti_back.Controller.MobileController;

import com.example.alerti_back.Service.MobileService.MobileApiQuizService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequestMapping("/quiz")
@RestController
public class MobileApiQuizController {
    private final MobileApiQuizService mobileApiQuizService;

    public MobileApiQuizController(MobileApiQuizService mobileApiQuizService) {
        this.mobileApiQuizService = mobileApiQuizService;
    }
    @GetMapping("/full-quiz-data")
    public List<Map<String, Object>> getFullQuizData() {
        return mobileApiQuizService.getAllQuizWithQuestions();
    }
    @GetMapping("/questions-by-quiz")
    public List<Map<String, Object>> getQuestionsByQuizId(Long quizId) {
        return mobileApiQuizService.getQuestionsWithReponsesByQuizId(quizId);
    }

}
