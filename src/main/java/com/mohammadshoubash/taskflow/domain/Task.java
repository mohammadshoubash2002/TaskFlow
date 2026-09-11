package com.mohammadshoubash.taskflow.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Task {
    private int id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private User assignedUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public enum Status {
        TODO,
        IN_PROGRESS,
        DONE,
        OVERDUE
    }

    private Status status;

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }

    private Priority priority;

    public Task(int id, String title, String description, LocalDate dueDate, Priority priority, User assignedUser) {
        this.id = id;
        this.title = (title != null) ? title : "";
        this.description = (description != null) ? description : "";
        this.dueDate = dueDate;
        this.priority = priority;
        this.assignedUser = assignedUser;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = Status.TODO;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return this.status == Status.DONE;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        this.updatedAt = LocalDateTime.now();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsDone() {
        this.status = Status.DONE;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsInProgress() {
        this.status = Status.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsTodo() {
        this.status = Status.TODO;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsOverdue() {
        this.status = Status.OVERDUE;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOverdue() {
        return this.dueDate != null && this.dueDate.isBefore(LocalDate.now()) && this.status != Status.DONE;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsLowPriority() {
        this.priority = Priority.LOW;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsMediumPriority() {
        this.priority = Priority.MEDIUM;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsHighPriority() {
        this.priority = Priority.HIGH;
        this.updatedAt = LocalDateTime.now();
    }

    public User getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(User assignedUser) {
        this.assignedUser = assignedUser;
        this.updatedAt = LocalDateTime.now();
    }

    public void removeAssignedUser() {
        this.assignedUser = null;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task task = (Task) obj;
        return this.id == task.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                    "id=" + id +
                    ", title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", dueDate=" + dueDate +
                    ", createdAt=" + createdAt +
                    ", updatedAt=" + updatedAt +
                    ", priority=" + priority +
                    ", status=" + status +
                    ", assignedUserName=" + (assignedUser != null ? assignedUser.getName() : "None") +
                    ", assignedUserEmail=" + (assignedUser != null ? assignedUser.getEmail() : "None") +
                '}';
    }
}