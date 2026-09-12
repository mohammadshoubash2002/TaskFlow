package com.mohammadshoubash.taskflow.repository;

import java.util.List;
import com.mohammadshoubash.taskflow.domain.Reminder;

public interface ReminderRepository extends Repository<Reminder, Integer> {
    List<Reminder> findPendingReminders();
    List<Reminder> findByTaskId(int taskId);
}
