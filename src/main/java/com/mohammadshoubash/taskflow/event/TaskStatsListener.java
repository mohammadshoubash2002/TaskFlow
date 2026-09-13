package com.mohammadshoubash.taskflow.event;

import java.util.concurrent.atomic.AtomicInteger;

public class TaskStatsListener implements TaskEventListener {

    private final AtomicInteger createdCount = new AtomicInteger(0);
    private final AtomicInteger completedCount = new AtomicInteger(0);
    private final AtomicInteger overdueCount = new AtomicInteger(0);
    private final AtomicInteger totalEventsCount = new AtomicInteger(0);

    @Override
    public void onEvent(TaskEvent event) {
        if (event == null) {
            return;
        }

        totalEventsCount.incrementAndGet();

        if (event instanceof TaskCreated) {
            createdCount.incrementAndGet();
        } else if (event instanceof TaskCompleted) {
            completedCount.incrementAndGet();
        } else if (event instanceof TaskOverdue) {
            overdueCount.incrementAndGet();
        }
    }

    public int getCreatedCount() {
        return createdCount.get();
    }

    public int getCompletedCount() {
        return completedCount.get();
    }

    public int getOverdueCount() {
        return overdueCount.get();
    }

    public int getTotalEventsCount() {
        return totalEventsCount.get();
    }

    public void reset() {
        createdCount.set(0);
        completedCount.set(0);
        overdueCount.set(0);
        totalEventsCount.set(0);
    }
}
