package com.mohammadshoubash.taskflow.domain;

import java.time.LocalDateTime;


public class Reminder {
    private int id;
    private Task task;
    private LocalDateTime remindAt;
    private boolean isSent;
    private LocalDateTime sentAt;

    public enum DeliveryChannel {
        EMAIL,
        SMS,
        PUSH
    }

    private DeliveryChannel channel;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Reminder(int id, Task task, LocalDateTime remindAt, DeliveryChannel channel) {
        this(id, task, remindAt, channel, false, null, LocalDateTime.now(), LocalDateTime.now());
    }

    public Reminder(int id, Task task, LocalDateTime remindAt, DeliveryChannel channel, 
                    boolean isSent, LocalDateTime sentAt, 
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.task = task;
        this.remindAt = remindAt;
        this.channel = channel;
        this.isSent = isSent;
        this.sentAt = sentAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getRemindAt() {
        return remindAt;
    }

    public void setRemindAt(LocalDateTime remindAt) {
        this.remindAt = remindAt;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void markAsSent() {
        this.isSent = true;
        this.sentAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsNotSent() {
        this.isSent = false;
        this.sentAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOverdue() {
        // i add this.remindAt != null to prevent the NullPointerException
        return this.remindAt != null && this.remindAt.isBefore(LocalDateTime.now());
    }

    public DeliveryChannel getChannel() {
        return channel;
    }

    public void setChannel(DeliveryChannel channel) {
        this.channel = channel;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Reminder reminder = (Reminder) obj;
        return this.id == reminder.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "id=" + id +
                ", taskId=" + (task != null ? task.getId() : null) +
                ", remindAt=" + remindAt +
                ", isSent=" + isSent +
                ", sentAt=" + sentAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
