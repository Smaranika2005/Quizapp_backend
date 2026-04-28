package com.quizapp.quizapp.dto;

public class StudentNotificationResponse {

    private int testId;
    private String title;
    private String message;

    public StudentNotificationResponse(int testId, String title, String message) {
        this.testId = testId;
        this.title = title;
        this.message = message;
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
}
