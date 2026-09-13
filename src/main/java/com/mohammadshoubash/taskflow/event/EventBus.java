package com.mohammadshoubash.taskflow.event;

import java.util.ArrayList;
import java.util.List;

public class EventBus {
    private final List<TaskEventListener> listeners = new ArrayList<>();

    public synchronized void subscribe(TaskEventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public synchronized void unsubscribe(TaskEventListener listener) {
        listeners.remove(listener);
    }

    public synchronized void publish(TaskEvent event) {
        if (event == null) {
            return;
        }
        for (TaskEventListener listener : new ArrayList<>(listeners)) {
            listener.onEvent(event);
        }
    }

    public synchronized List<TaskEventListener> getListeners() {
        return List.copyOf(listeners);
    }
}
