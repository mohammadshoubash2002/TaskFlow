package com.mohammadshoubash.taskflow.repository;

import java.util.Optional;
import com.mohammadshoubash.taskflow.domain.User;

public interface UserRepository extends Repository<User, Integer> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}