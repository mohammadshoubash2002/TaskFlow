package com.mohammadshoubash.taskflow.service;

import java.util.List;
import java.util.Optional;

import com.mohammadshoubash.taskflow.domain.User;
import com.mohammadshoubash.taskflow.exception.DuplicateUserException;
import com.mohammadshoubash.taskflow.repository.UserRepository;
import com.mohammadshoubash.taskflow.repository.UserRepositoryJdbc;

public class UserService {

    private final UserRepository userRepository;

    public UserService() {
        this(new UserRepositoryJdbc());
    }

    public UserService(UserRepository userRepository) {
        this.userRepository = (userRepository != null) ? userRepository : new UserRepositoryJdbc();
    }

    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateUserException(user.getEmail());
        }
        return userRepository.save(user);
    }

    public Optional<User> getUserById(int id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return userRepository.save(user);
    }

    public boolean deleteUser(int id) {
        return userRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }
}
