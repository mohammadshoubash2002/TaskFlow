package com.mohammadshoubash.taskflow.cache;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.mohammadshoubash.taskflow.domain.Task;

public class DueTodayCache {

    private final Cache<LocalDate, List<Task>> cache;

    public DueTodayCache() {
        this(new InMemoryCache<>());
    }

    public DueTodayCache(Cache<LocalDate, List<Task>> cache) {
        this.cache = cache;
    }

    public List<Task> getDueToday(List<Task> allTasks) {
        LocalDate today = LocalDate.now();
        return cache.get(today).orElseGet(() -> {
            List<Task> filtered = (allTasks == null) ? Collections.emptyList() :
                allTasks.stream()
                    .filter(t -> t != null && t.getDueDate() != null && t.getDueDate().equals(today))
                    .collect(Collectors.toList());

            List<Task> unmodifiable = Collections.unmodifiableList(filtered);
            cache.put(today, unmodifiable);
            return unmodifiable;
        });
    }

    public void invalidate() {
        cache.remove(LocalDate.now());
    }

    public List<Task> refresh(List<Task> allTasks) {
        invalidate();
        return getDueToday(allTasks);
    }

    public boolean isCached() {
        return cache.contains(LocalDate.now());
    }

    public void clear() {
        cache.clear();
    }
}
