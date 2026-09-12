package com.mohammadshoubash.taskflow.reminder;

import com.mohammadshoubash.taskflow.domain.Reminder;

public class EmailDelivery implements DeliveryMechanism {

    @Override
    public void send(Reminder reminder) {
        String recipient = (reminder != null && reminder.getTask() != null && reminder.getTask().getAssignedUser() != null)
                ? reminder.getTask().getAssignedUser().getEmail()
                : "Unknown Recipient";
        String taskTitle = (reminder != null && reminder.getTask() != null)
                ? reminder.getTask().getTitle()
                : "No Title";
        System.out.println("[EMAIL] Sending reminder to " + recipient + " for task: " + taskTitle);
    }
}
