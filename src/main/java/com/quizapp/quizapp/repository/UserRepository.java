package com.quizapp.quizapp.repository;

import com.quizapp.quizapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByIdentifier(String identifier);
    User findByIdentifierAndPassword(String identifier, String password);

}