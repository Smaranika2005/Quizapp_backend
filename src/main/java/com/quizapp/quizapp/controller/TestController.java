package com.quizapp.quizapp.controller;

import com.quizapp.quizapp.config.RoleExtractor;
import com.quizapp.quizapp.dto.ResultResponse;
import com.quizapp.quizapp.dto.QuestionResponse;
import com.quizapp.quizapp.dto.StudentNotificationResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.quizapp.quizapp.dto.RenameTestRequest;
import com.quizapp.quizapp.dto.RankRequest;
import com.quizapp.quizapp.dto.SubmitRequest;
import com.quizapp.quizapp.dto.TestRequest;
import com.quizapp.quizapp.dto.DeleteTestRequest;
import com.quizapp.quizapp.dto.TestResponse;

import com.quizapp.quizapp.entity.Question;
import com.quizapp.quizapp.entity.Test;
import com.quizapp.quizapp.entity.Result;

import com.quizapp.quizapp.repository.QuestionRepository;
import com.quizapp.quizapp.repository.TestRepository;
import com.quizapp.quizapp.repository.ResultRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tests")
public class TestController {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private RoleExtractor roleExtractor;

    @Autowired
    private com.quizapp.quizapp.repository.UserRepository userRepository;

    private String resolveStudentName(String identifier) {
        if (userRepository != null) {
            com.quizapp.quizapp.entity.User user = userRepository.findByIdentifier(identifier);
            if (user != null && user.getUsername() != null) {
                return user.getUsername();
            }
        }
        return identifier;
    }

    // ✅ Create Test (Teacher only)
    @PostMapping("/create")
    public ResponseEntity<?> createTest(@RequestBody Test test, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!roleExtractor.isTeacher(authHeader)) {
            return ResponseEntity.status(403).body("Only teachers can create tests");
        }

        // New tests stay hidden from students until teacher publishes.
        test.setPublished("no");
        testRepository.save(test);
        return ResponseEntity.ok("Test created successfully");
    }

    // ✅ Get tests for dashboard
    // Students see only published tests.
    // Teachers see only their own tests.
    @GetMapping("/all")
    public ResponseEntity<?> getAllTests(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (roleExtractor.isStudent(authHeader)) {
            List<TestResponse> tests = testRepository.findByPublishedIgnoreCase("yes")
                .stream()
                .map(test -> new TestResponse(
                    test.getId(),
                    test.getTestName(),
                    test.getPasscode(),
                    test.getTeacherUsername(),
                    resolveStudentName(test.getTeacherUsername()),
                    test.getDuration(),
                    test.getPublished(),
                    test.getPublishedAt()
                ))
                .collect(Collectors.toList());
            return ResponseEntity.ok(tests);
        }

        if (roleExtractor.isTeacher(authHeader)) {
            String username = roleExtractor.extractUsernameFromAuthHeader(authHeader);
            List<TestResponse> tests = testRepository.findByTeacherUsername(username)
                .stream()
                .map(test -> new TestResponse(
                    test.getId(),
                    test.getTestName(),
                    test.getPasscode(),
                    test.getTeacherUsername(),
                    resolveStudentName(test.getTeacherUsername()),
                    test.getDuration(),
                    test.getPublished(),
                    test.getPublishedAt()
                ))
                .collect(Collectors.toList());
            return ResponseEntity.ok(tests);
        }

        return ResponseEntity.status(403).body("Unauthorized role");
    }

    // ✅ Get Teacher's Tests (for teacher dashboard)
    @GetMapping("/my-tests")
    public ResponseEntity<?> getTeacherTests(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!roleExtractor.isTeacher(authHeader)) {
            return ResponseEntity.status(403).body("Only teachers can view their tests");
        }
        String username = roleExtractor.extractUsernameFromAuthHeader(authHeader);
        List<TestResponse> tests = testRepository.findByTeacherUsername(username)
            .stream()
            .map(test -> new TestResponse(
                test.getId(),
                test.getTestName(),
                test.getPasscode(),
                test.getTeacherUsername(),
                resolveStudentName(test.getTeacherUsername()),
                test.getDuration(),
                test.getPublished(),
                test.getPublishedAt()
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(tests);
    }

    // ✅ Student dashboard tests (published only)
    @GetMapping("/published")
    public ResponseEntity<?> getPublishedTests(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!roleExtractor.isStudent(authHeader)) {
            return ResponseEntity.status(403).body("Only students can view published tests");
        }
        List<TestResponse> tests = testRepository.findByPublishedIgnoreCase("yes")
            .stream()
            .map(test -> new TestResponse(
                test.getId(),
                test.getTestName(),
                test.getPasscode(),
                test.getTeacherUsername(),
                resolveStudentName(test.getTeacherUsername()),
                test.getDuration(),
                test.getPublished(),
                test.getPublishedAt()
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(tests);
    }

    // ✅ Publish test (Teacher only)
    @PutMapping("/{testId}/publish")
    public ResponseEntity<?> publishTest(@PathVariable int testId,
                                         @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!roleExtractor.isTeacher(authHeader)) {
            return ResponseEntity.status(403).body("Only teachers can publish tests");
        }

        Test test = testRepository.findById(testId).orElse(null);
        if (test == null) {
            return ResponseEntity.badRequest().body("Test not found");
        }

        String username = roleExtractor.extractUsernameFromAuthHeader(authHeader);
        if (!test.getTeacherUsername().equals(username)) {
            return ResponseEntity.status(403).body("You cannot publish this test");
        }

        List<Question> questions = questionRepository.findByTestId(testId);
        if (questions.isEmpty()) {
            return ResponseEntity.badRequest().body("Add at least one question before publishing");
        }

        if ("yes".equalsIgnoreCase(test.getPublished())) {
            return ResponseEntity.ok("Test is already published");
        }

        test.setPublished("yes");
        test.setPublishedAt(java.time.LocalDateTime.now());
        testRepository.save(test);
        return ResponseEntity.ok("Test published successfully");
    }

    // ✅ Student notification feed for newly published tests (polling endpoint)
    @GetMapping("/student/notifications")
    public ResponseEntity<?> getStudentNotifications(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!roleExtractor.isStudent(authHeader)) {
            return ResponseEntity.status(403).body("Only students can view notifications");
        }

        List<StudentNotificationResponse> notifications = testRepository
                .findByPublishedIgnoreCase("yes")
                .stream()
                .map(test -> new StudentNotificationResponse(
                        test.getId(),
                        "New test published",
                        "A new test is available: " + test.getTestName(),
                        test.getPublishedAt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(notifications);
    }

    // ✅ Validate Passcode
    @PostMapping("/validate")
    public ResponseEntity<?> validateTest(@RequestBody TestRequest request, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!roleExtractor.isStudent(authHeader)) {
            return ResponseEntity.status(403).body("Only students can attempt tests");
        }

        String username = roleExtractor.extractUsernameFromAuthHeader(authHeader);
        if (resultRepository.findByUsernameAndTestId(username, request.getTestId()).isPresent()) {
            return ResponseEntity.badRequest().body("You have already attempted the test. No further changes are allowed");
        }

        Test test = testRepository.findByIdAndPasscode(
                request.getTestId(),
                request.getPasscode()
        );

        if (test != null && "yes".equalsIgnoreCase(test.getPublished())) {
            return ResponseEntity.ok("Access granted");
        } else {
            return ResponseEntity.badRequest().body("Invalid passcode or test is not published");
        }
    }

    // ✅ Start Test (Student only - with role validation)
    @PostMapping("/start")
    public ResponseEntity<?> startTest(@RequestBody TestRequest request, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!roleExtractor.isStudent(authHeader)) {
            return ResponseEntity.status(403).body("Only students can attempt tests");
        }

        String username = roleExtractor.extractUsernameFromAuthHeader(authHeader);
        if (resultRepository.findByUsernameAndTestId(username, request.getTestId()).isPresent()) {
            return ResponseEntity.badRequest().body("You have already attempted the test. No further changes are allowed");
        }

        Test test = testRepository.findByIdAndPasscode(
                request.getTestId(),
                request.getPasscode()
        );

        if (test == null) {
            return ResponseEntity.badRequest().body("Invalid passcode");
        }

        if (!"yes".equalsIgnoreCase(test.getPublished())) {
            return ResponseEntity.badRequest().body("This test is not published yet");
        }

        List<Question> questions = questionRepository.findByTestId(request.getTestId());

        List<QuestionResponse> responseList = new ArrayList<>();

        for (Question q : questions) {
            responseList.add(new QuestionResponse(
                    q.getId(),
                    q.getQuestion(),
                    q.getOpt1(),
                    q.getOpt2(),
                    q.getOpt3(),
                    q.getOpt4()
            ));
        }

        return ResponseEntity.ok(responseList);
    }

    // ✅ Submit Test (Student only)
    @PostMapping("/submit")
    public ResponseEntity<?> submitTest(@RequestBody SubmitRequest request, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!roleExtractor.isStudent(authHeader)) {
            return ResponseEntity.status(403).body("Only students can submit tests");
        }

        if (resultRepository.findByUsernameAndTestId(request.getUsername(), request.getTestId()).isPresent()) {
            return ResponseEntity.badRequest().body("You have already attempted the test. No further changes are allowed");
        }

        Test test = testRepository.findById(request.getTestId()).orElse(null);
        if (test == null) {
            return ResponseEntity.badRequest().body("Test not found");
        }

        if (!"yes".equalsIgnoreCase(test.getPublished())) {
            return ResponseEntity.badRequest().body("Cannot submit an unpublished test");
        }

        int score = 0;

        List<Question> questions = questionRepository.findByTestId(request.getTestId());

        for (Question q : questions) {
            int questionId = q.getId();

            if (request.getAnswers().containsKey(questionId)) {
                int studentAnswer = request.getAnswers().get(questionId);

                if (studentAnswer == q.getCorrect()) {
                    score++;
                }
            }
        }

        Result result = new Result();
        result.setUsername(request.getUsername());
        result.setTestId(request.getTestId());
        result.setScore(score);
        result.setTotal(questions.size());
        result.setSubmittedAt(java.time.LocalDateTime.now());
        result.setTimeTaken(request.getTimeTaken());

        resultRepository.save(result);

        Map<String, Object> response = new HashMap<>();
        response.put("testId", request.getTestId());
        response.put("username", request.getUsername());
        response.put("score", score);
        response.put("total", questions.size());
        response.put("correctCount", score);
        response.put("submittedAt", result.getSubmittedAt());
        response.put("timeTaken", result.getTimeTaken());
        response.put("testName", test.getTestName());
        response.put("message", "Your Score: " + score + "/" + questions.size());

        return ResponseEntity.ok(response);
    }

    // ✅ Unpublish test (Teacher only)
    @PutMapping("/{testId}/unpublish")
    public ResponseEntity<?> unpublishTest(@PathVariable int testId,
                                           @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!roleExtractor.isTeacher(authHeader)) {
            return ResponseEntity.status(403).body("Only teachers can unpublish tests");
        }

        Test test = testRepository.findById(testId).orElse(null);
        if (test == null) {
            return ResponseEntity.badRequest().body("Test not found");
        }

        String username = roleExtractor.extractUsernameFromAuthHeader(authHeader);
        if (!test.getTeacherUsername().equals(username)) {
            return ResponseEntity.status(403).body("You cannot unpublish this test");
        }

        test.setPublished("no");
        testRepository.save(test);
        return ResponseEntity.ok("Test unpublished successfully");
    }


    // ✅ DELETE TEST (with cascade-like behavior) - Teacher only
    @Transactional
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteTest(@RequestBody DeleteTestRequest request, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!roleExtractor.isTeacher(authHeader)) {
            return ResponseEntity.status(403).body("Only teachers can delete tests");
        }

        Test test = testRepository.findById(request.getTestId()).orElse(null);

        if (test == null) {
            return ResponseEntity.badRequest().body("Test not found");
        }

        if (!test.getTeacherUsername().equals(request.getUsername())) {
            return ResponseEntity.status(403)
                    .body("You cannot delete this test");
        }

        // ✅ Delete all related questions first
        questionRepository.deleteAllByTestId(request.getTestId());

        // ✅ Then delete test
        testRepository.deleteById(request.getTestId());

        return ResponseEntity.ok("Test deleted successfully");
    }

    // ✅ Rename Test (Teacher only)
    @PutMapping("/rename")
    public ResponseEntity<String> renameTest(@RequestBody RenameTestRequest request, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!roleExtractor.isTeacher(authHeader)) {
            return ResponseEntity.status(403).body("Only teachers can rename tests");
        }

        // Step 1: Get test
        Test test = testRepository.findById(request.getTestId()).orElse(null);

        if (test == null) {
            return ResponseEntity.badRequest().body("Test not found");
        }

        // Step 2: Ownership check
        if (!test.getTeacherUsername().equals(request.getUsername())) {
            return ResponseEntity.status(403)
                    .body("You cannot rename this test");
        }

        // Step 3: Update name
        test.setTestName(request.getNewName());

        testRepository.save(test);

        return ResponseEntity.ok("Test renamed successfully");
    }

    // ✅ Get Ranking (Teacher only)
    @PostMapping("/ranking")
    public ResponseEntity<?> getRanking(@RequestBody RankRequest request, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!roleExtractor.isTeacher(authHeader)) {
            return ResponseEntity.status(403).body("Only teachers can view rankings");
        }

        // Step 1: Get test
        Test test = testRepository.findById(request.getTestId()).orElse(null);

        if (test == null) {
            return ResponseEntity.badRequest().body("Test not found");
        }

        // Step 2: Ownership check
        if (!test.getTeacherUsername().equals(request.getUsername())) {
            return ResponseEntity.status(403)
                    .body("You cannot view ranking for this test");
        }

        // Step 3: Get results sorted by score
        List<ResultResponse> results = resultRepository
            .findByTestIdOrderByScoreDesc(request.getTestId())
            .stream()
            .map(result -> new ResultResponse(
                result.getId(),
                result.getUsername(),
                result.getTestId(),
                test.getTestName(),
                result.getScore(),
                result.getTotal(),
                result.getSubmittedAt(),
                result.getTimeTaken(),
                resolveStudentName(result.getUsername())
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    // ✅ Get Test Results (Teacher endpoint - for teacher dashboard)
    @GetMapping("/{testId}/results")
    public ResponseEntity<?> getTestResults(@PathVariable int testId, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!roleExtractor.isTeacher(authHeader) && !roleExtractor.isStudent(authHeader)) {
            return ResponseEntity.status(403).body("Unauthorized access");
        }

        Test test = testRepository.findById(testId).orElse(null);
        if (test == null) {
            return ResponseEntity.badRequest().body("Test not found");
        }

        if (roleExtractor.isTeacher(authHeader)) {
            String username = roleExtractor.extractUsernameFromAuthHeader(authHeader);
            if (!test.getTeacherUsername().equals(username)) {
                return ResponseEntity.status(403).body("You cannot view results for this test");
            }
        } else if (roleExtractor.isStudent(authHeader)) {
            if (!"yes".equalsIgnoreCase(test.getPublished())) {
                return ResponseEntity.status(403).body("Test is not published");
            }
        }

        List<ResultResponse> results = resultRepository.findByTestId(testId)
            .stream()
            .map(result -> new ResultResponse(
                result.getId(),
                result.getUsername(),
                result.getTestId(),
                test.getTestName(),
                result.getScore(),
                result.getTotal(),
                result.getSubmittedAt(),
                result.getTimeTaken(),
                resolveStudentName(result.getUsername())
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

    // ✅ Get Student's Own Results (Student endpoint)
    @GetMapping("/results/my-results")
    public ResponseEntity<?> getMyResults(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!roleExtractor.isStudent(authHeader)) {
            return ResponseEntity.status(403).body("Only students can view their results");
        }

        String username = roleExtractor.extractUsernameFromAuthHeader(authHeader);
        List<ResultResponse> results = resultRepository.findByUsername(username)
            .stream()
            .map(result -> {
                Test test = testRepository.findById(result.getTestId()).orElse(null);
                String testName = test != null ? test.getTestName() : "Test " + result.getTestId();
                return new ResultResponse(
                    result.getId(),
                    result.getUsername(),
                    result.getTestId(),
                    testName,
                    result.getScore(),
                    result.getTotal(),
                    result.getSubmittedAt(),
                    result.getTimeTaken(),
                    resolveStudentName(result.getUsername())
                );
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }
}