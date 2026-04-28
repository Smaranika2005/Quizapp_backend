package com.quizapp.quizapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "results")
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String username;

    @Column(name = "test_id")
    private int testId;

    private int score;
    private int total;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    // Constructors
    public Result() {}

    public Result(String username, int testId, int score, int total, LocalDateTime submittedAt) {
        this.username = username;
        this.testId = testId;
        this.score = score;
        this.total = total;
        this.submittedAt = submittedAt;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getTestId() { return testId; }
    public void setTestId(int testId) { this.testId = testId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
