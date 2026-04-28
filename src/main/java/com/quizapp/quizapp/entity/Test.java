package com.quizapp.quizapp.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tests")
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "test_name")
    private String testName;

    private String passcode;

    @Column(name = "teacher_username")
    private String teacherUsername;

    private int duration;

    private String published;

    // Constructors
    public Test() {}

    public Test(String testName, String passcode, String teacherUsername, int duration, String published) {
        this.testName = testName;
        this.passcode = passcode;
        this.teacherUsername = teacherUsername;
        this.duration = duration;
        this.published = published;
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
}