package com.quizapp.quizapp.repository;

import com.quizapp.quizapp.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultRepository extends JpaRepository<Result, Integer> {

    List<Result> findByTestIdOrderByScoreDesc(int testId);

    List<Result> findByUsername(String username);

    List<Result> findByTestId(int testId);
}
