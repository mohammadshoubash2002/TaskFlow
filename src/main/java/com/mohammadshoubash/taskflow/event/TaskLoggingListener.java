package com.mohammadshoubash.taskflow.event;

public class TaskLoggingListener implements TaskEventListener {

    @Override
    public void onEvent(TaskEvent event) {
        if (event == null || event.getTask() == null) {
            return;
        }
        System.out.println(String.format(
            "[EVENT LOG] %s - Task #%d: '%s' (Status: %s, Priority: %s, Time: %s)",
            event.getClass().getSimpleName(),
            event.getTask().getId(),
            event.getTask().getTitle(),
            event.getTask().getStatus(),
            event.getTask().getPriority(),
            event.getTimestamp()
        ));
    }
}
