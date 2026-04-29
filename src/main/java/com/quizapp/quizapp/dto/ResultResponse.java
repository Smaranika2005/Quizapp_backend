package com.quizapp.quizapp.dto;

import java.time.LocalDateTime;

public class ResultResponse {

    private int id;
    private String username;
    private int testId;
    private String testName;
    private int score;
    private int total;
    private LocalDateTime submittedAt;

    public ResultResponse() {
    }

    public ResultResponse(int id, String username, int testId, String testName, int score, int total, LocalDateTime submittedAt) {
        this.id = id;
        this.username = username;
        this.testId = testId;
        this.testName = testName;
        this.score = score;
        this.total = total;
        this.submittedAt = submittedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getTestId() {
        return testId;
    }

    public void setTestId(int testId) {
        this.testId = testId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}