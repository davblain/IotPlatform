package com.gemini.iot.repository;

import com.gemini.iot.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserDao extends JpaRepository<User,UUID> {
    boolean existsByUsername(String username);
    User findUserByUsername(String name);
}
