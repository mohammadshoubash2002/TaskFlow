package com.mohammadshoubash.taskflow.reminder;

import com.mohammadshoubash.taskflow.domain.Reminder;

public interface DeliveryMechanism {
    void send(Reminder reminder);
}
