package com.mohammadshoubash.taskflow.reminder;

import com.mohammadshoubash.taskflow.domain.Reminder.DeliveryChannel;
import com.mohammadshoubash.taskflow.domain.Task;
import com.mohammadshoubash.taskflow.domain.Task.Priority;
import com.mohammadshoubash.taskflow.domain.User;

public class ReminderFactory {
    public DeliveryMechanism create(User user, Task task) {
        DeliveryChannel channel = (user != null && user.getPreferredChannel() != null)
                ? user.getPreferredChannel()
                : defaultChannelFor(task != null ? task.getPriority() : null);

        if (channel == null) {
            return new EmailDelivery();
        }

        return switch (channel) {
            case EMAIL -> new EmailDelivery();
            case SMS -> new SmsDelivery();
            case PUSH -> new PushDelivery();
            default -> new EmailDelivery();
        };
    }

    public DeliveryChannel defaultChannelFor(Priority priority) {
        if (priority == null) {
            return DeliveryChannel.EMAIL;
        }
        return switch (priority) {
            case HIGH -> DeliveryChannel.SMS;
            case MEDIUM, LOW -> DeliveryChannel.EMAIL;
            default -> DeliveryChannel.EMAIL;
        };
    }
}
