package com.mohammadshoubash.taskflow.algorithm;

import java.util.ArrayList;
import java.util.List;

import com.mohammadshoubash.taskflow.domain.Task;

public class TaskSorter {
    // Merge sort is used because it is an efficient and stable sorting algorithm.
    // It has a time complexity of O(n log n) and a space complexity of O(n).
    // Stability is important because it preserves the relative order of tasks
    // that have the same due date and priority.

    public List<Task> sortTasksByDueDate(List<Task> tasks) {
        if (tasks == null || tasks.size() <= 1) {
            return tasks == null ? new ArrayList<>() : new ArrayList<>(tasks);
        }

        int mid = tasks.size() / 2;
        List<Task> left = sortTasksByDueDate(new ArrayList<>(tasks.subList(0, mid)));
        List<Task> right = sortTasksByDueDate(new ArrayList<>(tasks.subList(mid, tasks.size())));

        return merge(left, right);
    }

    private List<Task> merge(List<Task> left, List<Task> right) {
        List<Task> merged = new ArrayList<>(left.size() + right.size());
        
        int i = 0;
        int j = 0;

        while (i < left.size() && j < right.size()) {
            Task taskA = left.get(i);
            Task taskB = right.get(j);

            // If taskA <= taskB, pick taskA first to preserve stability
            if (isBeforeOrEqual(taskA, taskB)) {
                merged.add(taskA);
                i++;
            } else {
                merged.add(taskB);
                j++;
            }
        }

        while (i < left.size()) {
            merged.add(left.get(i));
            i++;
        }

        while (j < right.size()) {
            merged.add(right.get(j));
            j++;
        }

        return merged;
    }

    private boolean isBeforeOrEqual(Task a, Task b) {
        if (a.getDueDate() == null && b.getDueDate() == null) {
            return comparePriority(a, b) >= 0;
        }
        if (a.getDueDate() == null) return false;
        if (b.getDueDate() == null) return true;

        int dateCompare = a.getDueDate().compareTo(b.getDueDate());
        if (dateCompare != 0) {
            return dateCompare < 0; // Earlier due date first
        }

        // Due dates match -> tiebreaker: priority descending (HIGH > MEDIUM > LOW)
        return comparePriority(a, b) >= 0;
    }

    private int comparePriority(Task a, Task b) {
        int priorityA = a.getPriority() != null ? a.getPriority().ordinal() : -1;
        int priorityB = b.getPriority() != null ? b.getPriority().ordinal() : -1;

        return Integer.compare(priorityA, priorityB);
    }
}
