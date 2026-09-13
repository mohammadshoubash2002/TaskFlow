package com.mohammadshoubash.taskflow.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mohammadshoubash.taskflow.domain.Task;
import com.mohammadshoubash.taskflow.domain.Task.Status;
import com.mohammadshoubash.taskflow.event.EventBus;
import com.mohammadshoubash.taskflow.event.TaskCompleted;
import com.mohammadshoubash.taskflow.event.TaskCreated;
import com.mohammadshoubash.taskflow.event.TaskOverdue;
import com.mohammadshoubash.taskflow.repository.TaskRepository;

public class TaskService {

    private final TaskRepository taskRepository;
    private final EventBus eventBus;
    private final ReportService reportService;

    public TaskService(TaskRepository taskRepository) {
        this(taskRepository, new EventBus(), new ReportService(taskRepository));
    }

    public TaskService(TaskRepository taskRepository, EventBus eventBus) {
        this(taskRepository, eventBus, new ReportService(taskRepository));
    }

    public TaskService(TaskRepository taskRepository, EventBus eventBus, ReportService reportService) {
        this.taskRepository = taskRepository;
        this.eventBus = (eventBus != null) ? eventBus : new EventBus();
        this.reportService = (reportService != null) ? reportService : new ReportService(this.taskRepository);
    }

    public Task createTask(Task task) {
        Task saved = taskRepository.save(task);
        eventBus.publish(new TaskCreated(saved));
        return saved;
    }

    public Optional<Task> completeTask(int taskId) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (optionalTask.isEmpty()) {
            return Optional.empty();
        }

        Task task = optionalTask.get();
        task.markAsDone();
        Task updated = taskRepository.save(task);
        eventBus.publish(new TaskCompleted(updated));
        return Optional.of(updated);
    }

    public List<Task> checkOverdueTasks() {
        List<Task> overdueTasks = new ArrayList<>();
        List<Task> allTasks = taskRepository.findAll();

        for (Task task : allTasks) {
            if (task != null && task.isOverdue() && task.getStatus() != Status.DONE && task.getStatus() != Status.OVERDUE) {
                task.markAsOverdue();
                Task updated = taskRepository.save(task);
                eventBus.publish(new TaskOverdue(updated));
                overdueTasks.add(updated);
            }
        }

        return overdueTasks;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Optional<Task> getTaskById(int taskId) {
        return taskRepository.findById(taskId);
    }

    public List<Task> getTasksByUserId(int userId) {
        return taskRepository.findByUserId(userId);
    }

    public List<Task> getTasksByStatus(Status status) {
        return taskRepository.findByStatus(status);
    }

    public Task updateTask(Task task) {
        return taskRepository.save(task);
    }

    public boolean deleteTask(int taskId) {
        return taskRepository.deleteById(taskId);
    }

    public List<Task> getDueSoonReport() {
        return getDueSoonReport(7);
    }

    public List<Task> getDueSoonReport(int daysAhead) {
        return reportService.getDueSoonReport(daysAhead);
    }

    public TaskRepository getTaskRepository() {
        return taskRepository;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public ReportService getReportService() {
        return reportService;
    }
}
