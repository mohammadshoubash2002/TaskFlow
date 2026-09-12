package com.mohammadshoubash.taskflow.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mohammadshoubash.taskflow.domain.Reminder.DeliveryChannel;
import com.mohammadshoubash.taskflow.exception.InvalidEmailException;
import com.mohammadshoubash.taskflow.util.Validator;

public class User {
    private int id;
    private String name;
    private String email;
    private DeliveryChannel preferredChannel;
    private List<Task> tasks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User(int id, String name, String email) {
        this(id, name, email, null, LocalDateTime.now(), LocalDateTime.now());
    }

    public User(int id, String name, String email, DeliveryChannel preferredChannel) {
        this(id, name, email, preferredChannel, LocalDateTime.now(), LocalDateTime.now());
    }

    public User(int id, String name, String email, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, name, email, null, createdAt, updatedAt);
    }

    public User(int id, String name, String email, DeliveryChannel preferredChannel, LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (!Validator.isValidEmail(email)) {
            throw new InvalidEmailException(email);
        }
        this.id = id;
        this.name = name;
        this.email = email;
        this.preferredChannel = preferredChannel;
        this.tasks = new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void addTask(Task task) {
        this.tasks.add(task);
        this.updatedAt = LocalDateTime.now();
    }

    public void removeTask(Task task) {
        this.tasks.remove(task);
        this.updatedAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (!Validator.isValidEmail(email)) {
            throw new InvalidEmailException(email);
        }
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    public List<Task> getTasks() {
        // i use this to prevent callers from modify the tasks list directly
        return Collections.unmodifiableList(tasks);
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = (tasks != null) ? new ArrayList<>(tasks) : new ArrayList<>();
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public DeliveryChannel getPreferredChannel() {
        return preferredChannel;
    }

    public void setPreferredChannel(DeliveryChannel preferredChannel) {
        this.preferredChannel = preferredChannel;
        this.updatedAt = LocalDateTime.now();
    }

    // i override equals and hashcode to be able to use User in HashSets
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return this.id == user.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", preferredChannel=" + preferredChannel +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
