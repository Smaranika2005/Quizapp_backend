package com.quizapp.quizapp.dto;

import java.util.Map;

public class SubmitRequest {

    private int testId;
    private String username; // ✅ Added
    private Map<Integer, Integer> answers;

    public int getTestId() {
        return testId;
    }

    public void setTestId(int testId) {
        this.testId = testId;
    }

    public String getUsername() {   // ✅ Getter
        return username;
    }

    public void setUsername(String username) {   // ✅ Setter
        this.username = username;
    }

    public Map<Integer, Integer> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<Integer, Integer> answers) {
        this.answers = answers;
    }
}