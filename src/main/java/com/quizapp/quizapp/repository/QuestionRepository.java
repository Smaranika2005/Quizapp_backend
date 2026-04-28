package com.quizapp.quizapp.repository;

import com.quizapp.quizapp.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Integer> {

    List<Question> findByTestId(int testId);
    void deleteAllByTestId(int testId);
}
