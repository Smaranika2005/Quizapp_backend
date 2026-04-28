package com.quizapp.quizapp.controller;

import java.util.List;

import com.quizapp.quizapp.config.RoleExtractor;
import com.quizapp.quizapp.dto.AddQuestionRequest;
import com.quizapp.quizapp.dto.DeleteQuestionRequest;
import com.quizapp.quizapp.entity.Question;
import com.quizapp.quizapp.entity.Test;
import com.quizapp.quizapp.repository.QuestionRepository;
import com.quizapp.quizapp.repository.TestRepository;
import com.quizapp.quizapp.dto.EditQuestionRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private RoleExtractor roleExtractor;

    // ✅ ADD QUESTION (Teacher only)
    @PostMapping("/add")
    public ResponseEntity<String> addQuestion(@RequestBody AddQuestionRequest request, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!roleExtractor.isTeacher(authHeader)) {
            return ResponseEntity.status(403).body("Only teachers can add questions");
        }

        Question question = request.getQuestion();

        Test test = testRepository.findById(question.getTestId()).orElse(null);

        if (test == null) {
            return ResponseEntity.badRequest().body("Test not found");
        }

        if (!test.getTeacherUsername().equals(request.getUsername())) {
            return ResponseEntity.status(403)
                    .body("You cannot add questions to this test");
        }

        questionRepository.save(question);

        return ResponseEntity.ok("Question added successfully");
    }

    // ✅ GET QUESTIONS BY TEST
    @GetMapping("/test/{testId}")
    public List<Question> getQuestionsByTest(@PathVariable int testId) {
        return questionRepository.findByTestId(testId);
    }

    // ✅ DELETE QUESTION (Teacher only)
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteQuestion(@RequestBody DeleteQuestionRequest request, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!roleExtractor.isTeacher(authHeader)) {
            return ResponseEntity.status(403).body("Only teachers can delete questions");
        }

        // Step 1: Get question
        Question question = questionRepository.findById(request.getQuestionId()).orElse(null);

        if (question == null) {
            return ResponseEntity.badRequest().body("Question not found");
        }

        // Step 2: Get test
        Test test = testRepository.findById(question.getTestId()).orElse(null);

        if (test == null) {
            return ResponseEntity.badRequest().body("Test not found");
        }

        // Step 3: Ownership check
        if (!test.getTeacherUsername().equals(request.getUsername())) {
            return ResponseEntity.status(403)
                    .body("You cannot delete this question");
        }

        // Step 4: Delete
        questionRepository.deleteById(request.getQuestionId());

        return ResponseEntity.ok("Question deleted successfully");
    }

    // ✅ EDIT QUESTION (Teacher only)
    @PutMapping("/edit")
    public ResponseEntity<String> editQuestion(@RequestBody EditQuestionRequest request, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!roleExtractor.isTeacher(authHeader)) {
            return ResponseEntity.status(403).body("Only teachers can edit questions");
        }

        Question updatedQuestion = request.getQuestion();

        // Step 1: Check if question exists
        Question existing = questionRepository.findById(updatedQuestion.getId()).orElse(null);

        if (existing == null) {
            return ResponseEntity.badRequest().body("Question not found");
        }

        // Step 2: Get test
        Test test = testRepository.findById(existing.getTestId()).orElse(null);

        if (test == null) {
            return ResponseEntity.badRequest().body("Test not found");
        }

        // Step 3: Ownership check
        if (!test.getTeacherUsername().equals(request.getUsername())) {
            return ResponseEntity.status(403)
                    .body("You cannot edit this question");
        }

        // Step 4: Update fields
        existing.setQuestion(updatedQuestion.getQuestion());
        existing.setOpt1(updatedQuestion.getOpt1());
        existing.setOpt2(updatedQuestion.getOpt2());
        existing.setOpt3(updatedQuestion.getOpt3());
        existing.setOpt4(updatedQuestion.getOpt4());
        existing.setCorrect(updatedQuestion.getCorrect());

        questionRepository.save(existing);

        return ResponseEntity.ok("Question updated successfully");
    }
}