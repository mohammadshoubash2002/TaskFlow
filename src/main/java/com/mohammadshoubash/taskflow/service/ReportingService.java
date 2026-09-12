package com.mohammadshoubash.taskflow.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mohammadshoubash.taskflow.domain.Task;
import com.mohammadshoubash.taskflow.domain.Task.Priority;
import com.mohammadshoubash.taskflow.domain.Task.Status;
import com.mohammadshoubash.taskflow.domain.User;

public class ReportingService {

    /**
     * Aggregation 1: Tasks completed per user this week.
     * Groups by task owner (assignedUser), filtering tasks completed within the last 7 days.
     */
    public Map<User, Long> tasksCompletedPerUserThisWeek(List<Task> tasks) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);

        return tasks.stream()
            .filter(t -> t != null && t.getAssignedUser() != null)
            .filter(t -> t.isCompleted() && t.getCompletedAt() != null)
            .filter(t -> !t.getCompletedAt().isBefore(oneWeekAgo))
            .collect(Collectors.groupingBy(
                Task::getAssignedUser,
                Collectors.counting()
            ));
    }

    /**
     * Aggregation 2: Overdue task count by priority.
     * Groups overdue tasks by priority and counts them.
     */
    public Map<Priority, Long> overdueCountByPriority(List<Task> tasks) {
        return tasks.stream()
            .filter(t -> t != null && (t.getStatus() == Status.OVERDUE || t.isOverdue()))
            .filter(t -> t.getPriority() != null)
            .collect(Collectors.groupingBy(
                Task::getPriority,
                Collectors.counting()
            ));
    }

    /**
     * Aggregation 3: Average time-to-completion.
     * Maps each completed task to the Duration between createdAt and completedAt, then averages.
     */
    public Optional<Duration> averageTimeToCompletion(List<Task> tasks) {
        return tasks.stream()
            .filter(t -> t != null && t.isCompleted() && t.getCreatedAt() != null && t.getCompletedAt() != null)
            .map(t -> Duration.between(t.getCreatedAt(), t.getCompletedAt()))
            .mapToLong(Duration::toSeconds)
            .average()
            .stream()
            .mapToObj(avgSec -> Duration.ofSeconds((long) avgSec))
            .findFirst();
    }
}
