package com.mohammadshoubash.taskflow.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.mohammadshoubash.taskflow.algorithm.TaskSorter;
import com.mohammadshoubash.taskflow.domain.Task;
import com.mohammadshoubash.taskflow.repository.TaskRepository;

public class ReportService {

    private final TaskRepository taskRepository;
    private final TaskSorter taskSorter;

    public ReportService(TaskRepository taskRepository) {
        this(taskRepository, new TaskSorter());
    }

    public ReportService(TaskRepository taskRepository, TaskSorter taskSorter) {
        this.taskRepository = taskRepository;
        this.taskSorter = taskSorter;
    }

    public List<Task> getDueSoonReport(int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(daysAhead);

        List<Task> allTasks = taskRepository.findAll();

        List<Task> dueSoon = allTasks.stream()
                .filter(t -> t != null 
                        && t.getDueDate() != null
                        && !t.isCompleted()
                        && !t.getDueDate().isBefore(today)
                        && !t.getDueDate().isAfter(cutoff))
                .collect(Collectors.toList());

        return taskSorter.sortTasksByDueDate(dueSoon);
    }
}
