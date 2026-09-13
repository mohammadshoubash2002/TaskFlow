package com.mohammadshoubash.taskflow.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.mohammadshoubash.taskflow.domain.Task.Priority;
import com.mohammadshoubash.taskflow.exception.InvalidTaskStateException;

public class Task {
    private int id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private User assignedUser;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Reminder> reminders = new ArrayList<>();
    
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
        this(id, title, description, dueDate, Status.TODO, priority, assignedUser, null, LocalDateTime.now(), LocalDateTime.now());
    }

    public Task(int id, String title, String description, LocalDate dueDate, 
                Status status, Priority priority, User assignedUser, 
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, title, description, dueDate, status, priority, assignedUser, 
            null, createdAt, updatedAt);
    }

    public Task(int id, String title, String description, LocalDate dueDate, 
                Status status, Priority priority, User assignedUser, 
                LocalDateTime completedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = (title != null) ? title : "";
        this.description = (description != null) ? description : "";
        this.dueDate = dueDate;
        this.status = (status != null) ? status : Status.TODO;
        this.priority = priority;
        this.assignedUser = assignedUser;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsInProgress() {
        this.status = Status.IN_PROGRESS;
        this.completedAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsTodo() {
        this.status = Status.TODO;
        this.completedAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsOverdue() {
        if (this.status == Status.DONE) {
            throw new InvalidTaskStateException(this.id, this.status.toString(), Status.OVERDUE.toString());
        }
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

    public LocalDateTime getCompletedAt() {
        if (this.completedAt != null) {
            return this.completedAt;
        }

        if (this.status == Status.DONE) {
            return this.updatedAt;
        }

        return null;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
        this.updatedAt = LocalDateTime.now();
    }

    public List<Reminder> getReminders() {
        return new ArrayList<>(reminders);
    }

    public void setReminders(List<Reminder> reminders) {
        this.reminders = (reminders != null) ? new ArrayList<>(reminders) : new ArrayList<>();
    }

    public void addReminder(Reminder reminder) {
        if (reminder != null) {
            this.reminders.add(reminder);
        }
    }

    /**
     * Reschedules the task to a new due date.
     * 
     * Judgment Call:
     * When a task's due date is rescheduled, all linked pending reminders are cleared.
     * Trigger times calculated relative to the old due date are no longer meaningful and
     * would produce premature or confusing notifications. Fresh reminders should be recreated
     * relative to the new due date.
     * If the task was previously marked OVERDUE and the new due date is in the future, its
     * status is automatically reverted to IN_PROGRESS.
     *
     * @param newDueDate the new target completion date
     * @throws IllegalArgumentException if newDueDate is null
     * @throws InvalidTaskStateException if the task is already completed (DONE)
     */
    public void reschedule(LocalDate newDueDate) {
        if (newDueDate == null) {
            throw new IllegalArgumentException("New due date cannot be null");
        }
        if (this.status == Status.DONE) {
            throw new InvalidTaskStateException(this.id, this.status.toString(), "RESCHEDULE");
        }
        this.dueDate = newDueDate;
        this.reminders.clear(); // Judgment call: old reminder trigger times are invalidated
        if (this.status == Status.OVERDUE && !isOverdue()) {
            this.status = Status.IN_PROGRESS;
        }
        this.updatedAt = LocalDateTime.now();
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
                    ", completedAt=" + completedAt +
                    ", priority=" + priority +
                    ", status=" + status +
                    ", assignedUserName=" + (assignedUser != null ? assignedUser.getName() : "None") +
                    ", assignedUserEmail=" + (assignedUser != null ? assignedUser.getEmail() : "None") +
                '}';
    }
}