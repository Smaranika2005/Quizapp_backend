package com.quizapp.quizapp.repository;

import com.quizapp.quizapp.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestRepository extends JpaRepository<Test, Integer> {

    Test findByIdAndPasscode(int id, String passcode);

    List<Test> findByTeacherUsername(String teacherUsername);

    List<Test> findByPublishedIgnoreCase(String published);
}