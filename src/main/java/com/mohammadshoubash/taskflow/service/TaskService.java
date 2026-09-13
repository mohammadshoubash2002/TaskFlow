package com.mohammadshoubash.taskflow.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mohammadshoubash.taskflow.cache.DueTodayCache;
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
    private final ReportingService reportingService;
    private final ReminderRepository reminderRepository;
    private final DueTodayCache dueTodayCache;

    public TaskService(TaskRepository taskRepository) {
        this(taskRepository, new EventBus(), new ReportingService(), new ReminderRepositoryJdbc(taskRepository), new DueTodayCache());
    }

    public TaskService(TaskRepository taskRepository, EventBus eventBus) {
        this(taskRepository, eventBus, new ReportingService(), new ReminderRepositoryJdbc(taskRepository), new DueTodayCache());
    }

    public TaskService(TaskRepository taskRepository, EventBus eventBus, ReportingService reportingService) {
        this(taskRepository, eventBus, reportingService, new ReminderRepositoryJdbc(taskRepository), new DueTodayCache());
    }

    public TaskService(TaskRepository taskRepository, EventBus eventBus, ReportingService reportingService,
                       ReminderRepository reminderRepository, DueTodayCache dueTodayCache) {
        this.taskRepository = taskRepository;
        this.eventBus = (eventBus != null) ? eventBus : new EventBus();
        this.reportingService = (reportingService != null) ? reportingService : new ReportingService();
        this.reminderRepository = (reminderRepository != null) ? reminderRepository : new ReminderRepositoryJdbc(this.taskRepository);
        this.dueTodayCache = (dueTodayCache != null) ? dueTodayCache : new DueTodayCache();
    }

    public Task createTask(Task task) {
        Task saved = taskRepository.save(task);
        dueTodayCache.invalidate();
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
        dueTodayCache.invalidate();
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

        if (!overdueTasks.isEmpty()) {
            dueTodayCache.invalidate();
        }

        return overdueTasks;
    }

    /**
     * Retrieves tasks due today using in-memory caching.
     * Backed by generic Cache abstraction to avoid redundant DB queries.
     */
    public List<Task> getTasksDueToday() {
        return dueTodayCache.getDueToday(taskRepository.findAll());
    }

    public boolean isDueTodayCached() {
        return dueTodayCache.isCached();
    }

    public void invalidateDueTodayCache() {
        dueTodayCache.invalidate();
    }

    public DueTodayCache getDueTodayCache() {
        return dueTodayCache;
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
        Task updated = taskRepository.save(task);
        dueTodayCache.invalidate();
        return updated;
    }

    public boolean deleteTask(int taskId) {
        boolean deleted = taskRepository.deleteById(taskId);
        if (deleted) {
            dueTodayCache.invalidate();
        }
        return deleted;
    }

    public List<Task> getDueSoonReport() {
        return getDueSoonReport(7);
    }

    public List<Task> getDueSoonReport(int daysAhead) {
        return reportingService.getDueSoonReport(taskRepository.findAll(), daysAhead);
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
     * Also invalidates the in-memory DueTodayCache.
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
        dueTodayCache.invalidate();

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

    public ReportingService getReportingService() {
        return reportingService;
    }
}
