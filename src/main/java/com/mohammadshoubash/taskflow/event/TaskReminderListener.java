package com.mohammadshoubash.taskflow.event;

import java.time.LocalDateTime;

import com.mohammadshoubash.taskflow.domain.Reminder;
import com.mohammadshoubash.taskflow.domain.Reminder.DeliveryChannel;
import com.mohammadshoubash.taskflow.domain.Task;
import com.mohammadshoubash.taskflow.domain.User;
import com.mohammadshoubash.taskflow.reminder.DeliveryMechanism;
import com.mohammadshoubash.taskflow.reminder.ReminderFactory;

public class TaskReminderListener implements TaskEventListener {

    private final ReminderFactory reminderFactory;

    public TaskReminderListener() {
        this(new ReminderFactory());
    }

    public TaskReminderListener(ReminderFactory reminderFactory) {
        this.reminderFactory = (reminderFactory != null) ? reminderFactory : new ReminderFactory();
    }

    @Override
    public void onEvent(TaskEvent event) {
        if (event == null || event.getTask() == null) {
            return;
        }

        // Trigger reminder for TaskCreated or TaskOverdue events
        if (event instanceof TaskCreated || event instanceof TaskOverdue) {
            
            Task task = event.getTask();
            User user = task.getAssignedUser();

            DeliveryMechanism delivery = reminderFactory.create(user, task);
            DeliveryChannel channel = (user != null && user.getPreferredChannel() != null)
                    ? user.getPreferredChannel()
                    : reminderFactory.defaultChannelFor(task.getPriority());

            Reminder reminder = new Reminder(0, task, LocalDateTime.now(), channel);
            delivery.send(reminder);
        }
    }
}
