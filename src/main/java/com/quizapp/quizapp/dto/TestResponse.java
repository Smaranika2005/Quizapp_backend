package com.quizapp.quizapp.dto;

import java.time.LocalDateTime;

public class TestResponse {
    private int id;
    private String testName;
    private String passcode;
    private String teacherUsername;
    private String teacherName;
    private int duration;
    private String published;
    private LocalDateTime publishedAt;

    public TestResponse() {}

    public TestResponse(int id, String testName, String passcode, String teacherUsername, String teacherName, int duration, String published, LocalDateTime publishedAt) {
        this.id = id;
        this.testName = testName;
        this.passcode = passcode;
        this.teacherUsername = teacherUsername;
        this.teacherName = teacherName;
        this.duration = duration;
        this.published = published;
        this.publishedAt = publishedAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getPasscode() {
        return passcode;
    }

    public void setPasscode(String passcode) {
        this.passcode = passcode;
    }

    public String getTeacherUsername() {
        return teacherUsername;
    }

    public void setTeacherUsername(String teacherUsername) {
        this.teacherUsername = teacherUsername;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
}
