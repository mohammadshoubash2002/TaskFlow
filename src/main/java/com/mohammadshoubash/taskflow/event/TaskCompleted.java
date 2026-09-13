package com.mohammadshoubash.taskflow.event;

import java.time.LocalDateTime;

import com.mohammadshoubash.taskflow.domain.Task;

public record TaskCompleted(Task task, LocalDateTime timestamp) implements TaskEvent {
    public TaskCompleted(Task task) {
        this(task, LocalDateTime.now());
    }

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
