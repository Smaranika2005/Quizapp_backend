package com.quizapp.quizapp.dto;

public class DeleteTestRequest {

    private int testId;
    private String username;

    public int getTestId() {
        return testId;
    }

    public void setTestId(int testId) {
        this.testId = testId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
