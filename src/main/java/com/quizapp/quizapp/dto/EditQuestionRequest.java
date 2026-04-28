package com.quizapp.quizapp.dto;

import com.quizapp.quizapp.entity.Question;

public class EditQuestionRequest {

    private String username;
    private Question question;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }
}