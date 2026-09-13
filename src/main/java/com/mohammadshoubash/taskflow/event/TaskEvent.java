package com.mohammadshoubash.taskflow.event;

import java.time.LocalDateTime;

import com.mohammadshoubash.taskflow.domain.Task;

public interface TaskEvent {
    Task getTask();
    LocalDateTime getTimestamp();
}
