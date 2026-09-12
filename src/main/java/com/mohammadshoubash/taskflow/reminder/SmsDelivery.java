package com.mohammadshoubash.taskflow.reminder;

import com.mohammadshoubash.taskflow.domain.Reminder;

public class SmsDelivery implements DeliveryMechanism {

    @Override
    public void send(Reminder reminder) {
        String userName = (reminder != null && reminder.getTask() != null && reminder.getTask().getAssignedUser() != null)
                ? reminder.getTask().getAssignedUser().getName()
                : "Unknown User";
        String taskTitle = (reminder != null && reminder.getTask() != null)
                ? reminder.getTask().getTitle()
                : "No Title";
        System.out.println("[SMS] Sending SMS reminder to " + userName + " for task: " + taskTitle);
    }
}
