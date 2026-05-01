package com.quizapp.quizapp.dto;

public class StudentNotificationResponse {

    private int testId;
    private String title;
    private String message;
    private java.time.LocalDateTime createdAt;

    public StudentNotificationResponse(int testId, String title, String message, java.time.LocalDateTime createdAt) {
        this.testId = testId;
        this.title = title;
        this.message = message;
        this.createdAt = createdAt;
    }

    public int getTestId() {
        return testId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
