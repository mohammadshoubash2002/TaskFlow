package com.mohammadshoubash.taskflow.repository;

import java.util.List;
import com.mohammadshoubash.taskflow.domain.Task;

public interface TaskRepository extends Repository<Task, Integer> {
    List<Task> findByUserId(int userId);
    List<Task> findByStatus(Task.Status status);
}
