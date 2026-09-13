package com.mohammadshoubash.taskflow.event;

@FunctionalInterface
public interface TaskEventListener {
    void onEvent(TaskEvent event);
}
