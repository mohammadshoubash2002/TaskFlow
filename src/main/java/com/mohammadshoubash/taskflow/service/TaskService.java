package com.mohammadshoubash.taskflow.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mohammadshoubash.taskflow.domain.Reminder;
import com.mohammadshoubash.taskflow.domain.Reminder.DeliveryChannel;
import com.mohammadshoubash.taskflow.domain.Task;
import com.mohammadshoubash.taskflow.domain.Task.Status;
import com.mohammadshoubash.taskflow.event.EventBus;
import com.mohammadshoubash.taskflow.event.TaskCompleted;
import com.mohammadshoubash.taskflow.event.TaskCreated;
import com.mohammadshoubash.taskflow.event.TaskOverdue;
import com.mohammadshoubash.taskflow.repository.ReminderRepository;
import com.mohammadshoubash.taskflow.repository.ReminderRepositoryJdbc;
import com.mohammadshoubash.taskflow.repository.TaskRepository;

public class TaskService {

    private final TaskRepository taskRepository;
    private final EventBus eventBus;
    private final ReportService reportService;
    private final ReminderRepository reminderRepository;

    public TaskService(TaskRepository taskRepository) {
        this(taskRepository, new EventBus(), new ReportService(taskRepository), new ReminderRepositoryJdbc(taskRepository));
    }

    public TaskService(TaskRepository taskRepository, EventBus eventBus) {
        this(taskRepository, eventBus, new ReportService(taskRepository), new ReminderRepositoryJdbc(taskRepository));
    }

    public TaskService(TaskRepository taskRepository, EventBus eventBus, ReportService reportService) {
        this(taskRepository, eventBus, reportService, new ReminderRepositoryJdbc(taskRepository));
    }

    public TaskService(TaskRepository taskRepository, EventBus eventBus, ReportService reportService, ReminderRepository reminderRepository) {
        this.taskRepository = taskRepository;
        this.eventBus = (eventBus != null) ? eventBus : new EventBus();
        this.reportService = (reportService != null) ? reportService : new ReportService(this.taskRepository);
        this.reminderRepository = (reminderRepository != null) ? reminderRepository : new ReminderRepositoryJdbc(this.taskRepository);
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

    /**
     * Reschedules a task to a new due date.
     *
     * Judgment Call:
     * Rescheduling invalidates all previous pending reminders for this task.
     * Any unsent reminders are canceled/deleted, and fresh reminders aligned with the
     * new due date are scheduled.
     *
     * @param taskId the ID of the task to reschedule
     * @param newDueDate the new deadline
     * @return Optional containing the updated Task, or Optional.empty() if not found
     */
    public Optional<Task> rescheduleTask(int taskId, LocalDate newDueDate) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (optionalTask.isEmpty()) {
            return Optional.empty();
        }

        Task task = optionalTask.get();
        task.reschedule(newDueDate);
        Task updated = taskRepository.save(task);

        // Cancel all pending reminders in the repository for this task
        List<Reminder> existingReminders = reminderRepository.findByTaskId(taskId);
        for (Reminder r : existingReminders) {
            if (!r.isSent()) {
                reminderRepository.deleteById(r.getId());
            }
        }

        // Recreate a fresh pending reminder aligned with the new due date if assigned user exists
        if (updated.getAssignedUser() != null) {
            DeliveryChannel channel = (updated.getAssignedUser().getPreferredChannel() != null)
                    ? updated.getAssignedUser().getPreferredChannel()
                    : DeliveryChannel.EMAIL;
            LocalDateTime newRemindAt = newDueDate.atTime(9, 0); // 9:00 AM on the new due date
            Reminder newReminder = new Reminder(0, updated, newRemindAt, channel);
            reminderRepository.save(newReminder);
            updated.addReminder(newReminder);
        }

        return Optional.of(updated);
    }

    public ReminderRepository getReminderRepository() {
        return reminderRepository;
    }

    public ReportService getReportService() {
        return reportService;
    }
}
