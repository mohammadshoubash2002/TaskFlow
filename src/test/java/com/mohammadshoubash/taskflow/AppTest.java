package com.mohammadshoubash.taskflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import com.mohammadshoubash.taskflow.algorithm.TaskSorter;
import com.mohammadshoubash.taskflow.cache.DueTodayCache;
import com.mohammadshoubash.taskflow.domain.Reminder.DeliveryChannel;
import com.mohammadshoubash.taskflow.domain.Task;
import com.mohammadshoubash.taskflow.domain.Task.Priority;
import com.mohammadshoubash.taskflow.domain.Task.Status;
import com.mohammadshoubash.taskflow.domain.User;
import com.mohammadshoubash.taskflow.event.EventBus;
import com.mohammadshoubash.taskflow.event.TaskCreated;
import com.mohammadshoubash.taskflow.event.TaskEvent;
import com.mohammadshoubash.taskflow.event.TaskEventListener;
import com.mohammadshoubash.taskflow.exception.InvalidEmailException;
import com.mohammadshoubash.taskflow.exception.InvalidTaskStateException;
import com.mohammadshoubash.taskflow.reminder.DeliveryMechanism;
import com.mohammadshoubash.taskflow.reminder.EmailDelivery;
import com.mohammadshoubash.taskflow.reminder.PushDelivery;
import com.mohammadshoubash.taskflow.reminder.ReminderFactory;
import com.mohammadshoubash.taskflow.reminder.SmsDelivery;
import com.mohammadshoubash.taskflow.service.ReportingService;
import com.mohammadshoubash.taskflow.util.Validator;

public class AppTest {

    // 1. Validation & Exceptions
    @Test
    public void testEmailValidationRegex() {
        assertTrue(Validator.isValidEmail("user@example.com"));
        assertTrue(Validator.isValidEmail("first.last@domain.co.uk"));
        assertFalse(Validator.isValidEmail("invalid-email"));
        assertFalse(Validator.isValidEmail("@missingusername.com"));
        assertFalse(Validator.isValidEmail(""));
        assertFalse(Validator.isValidEmail(null));
    }

    @Test(expected = InvalidEmailException.class)
    public void testUserCreationInvalidEmailThrowsException() {
        new User(1, "John", "not-an-email");
    }

    // 2. Domain Encapsulation & State Transitions
    @Test
    public void testTaskStateTransitions() {
        Task task = new Task(1, "Feature", "Desc", LocalDate.now().plusDays(2), Priority.HIGH, null);
        assertEquals(Status.TODO, task.getStatus());

        task.markAsDone();
        assertEquals(Status.DONE, task.getStatus());
        assertTrue(task.isCompleted());
        assertNotNull(task.getCompletedAt());
    }

    @Test(expected = InvalidTaskStateException.class)
    public void testMarkAsOverdueWhenDoneThrowsException() {
        Task task = new Task(1, "Feature", "Desc", LocalDate.now().minusDays(1), Priority.MEDIUM, null);
        task.markAsDone();
        task.markAsOverdue();
    }

    @Test
    public void testTaskRescheduleJudgmentCall() {
        Task task = new Task(1, "Feature", "Desc", LocalDate.now().minusDays(2), Status.OVERDUE, Priority.MEDIUM, null, null, LocalDateTime.now(), LocalDateTime.now());
        task.reschedule(LocalDate.now().plusDays(5));
        assertEquals(LocalDate.now().plusDays(5), task.getDueDate());
        assertEquals(Status.IN_PROGRESS, task.getStatus());
    }

    // 3. Hand-Rolled Merge Sort (Algorithm)
    @Test
    public void testTaskSorterMergeSortOrderingAndTieBreaker() {
        LocalDate today = LocalDate.now();
        Task t1 = new Task(1, "Task 1", "", today.plusDays(3), Priority.LOW, null);
        Task t2 = new Task(2, "Task 2", "", today.plusDays(1), Priority.MEDIUM, null);
        Task t3 = new Task(3, "Task 3", "", today.plusDays(1), Priority.HIGH, null);

        TaskSorter sorter = new TaskSorter();
        List<Task> sorted = sorter.sortTasksByDueDate(List.of(t1, t2, t3));

        assertEquals(3, sorted.size());
        assertEquals(3, sorted.get(0).getId()); // today + 1, HIGH
        assertEquals(2, sorted.get(1).getId()); // today + 1, MEDIUM
        assertEquals(1, sorted.get(2).getId()); // today + 3, LOW
    }

    // 4. In-Memory Generic Cache
    @Test
    public void testDueTodayCacheHitMissAndInvalidate() {
        DueTodayCache cache = new DueTodayCache();
        LocalDate today = LocalDate.now();

        Task dueToday = new Task(1, "Today Task", "", today, Priority.HIGH, null);
        Task dueLater = new Task(2, "Later Task", "", today.plusDays(2), Priority.LOW, null);
        List<Task> allTasks = List.of(dueToday, dueLater);

        assertFalse(cache.isCached());
        List<Task> result1 = cache.getDueToday(allTasks);
        assertEquals(1, result1.size());
        assertEquals(1, result1.get(0).getId());
        assertTrue(cache.isCached());

        // Cache hit
        List<Task> result2 = cache.getDueToday(List.of()); // Returns cached data without DB query
        assertEquals(1, result2.size());

        // Invalidate
        cache.invalidate();
        assertFalse(cache.isCached());
    }

    // 5. Factory Pattern
    @Test
    public void testReminderFactoryDeliveryMechanism() {
        ReminderFactory factory = new ReminderFactory();

        User smsUser = new User(1, "Alice", "alice@example.com", DeliveryChannel.SMS);
        DeliveryMechanism mechanism1 = factory.create(smsUser, null);
        assertTrue(mechanism1 instanceof SmsDelivery);

        User pushUser = new User(2, "Bob", "bob@example.com", DeliveryChannel.PUSH);
        DeliveryMechanism mechanism2 = factory.create(pushUser, null);
        assertTrue(mechanism2 instanceof PushDelivery);

        User emailUser = new User(3, "Charlie", "charlie@example.com", DeliveryChannel.EMAIL);
        DeliveryMechanism mechanism3 = factory.create(emailUser, null);
        assertTrue(mechanism3 instanceof EmailDelivery);
    }

    // 6. Observer / Pub-Sub Pattern
    @Test
    public void testEventBusPublishAndUnsubscribe() {
        EventBus bus = new EventBus();
        List<TaskEvent> received = new ArrayList<>();
        TaskEventListener listener = received::add;

        bus.subscribe(listener);
        assertEquals(1, bus.getListeners().size());

        Task task = new Task(1, "Task", "", LocalDate.now(), Priority.MEDIUM, null);
        bus.publish(new TaskCreated(task));
        assertEquals(1, received.size());

        bus.unsubscribe(listener);
        assertEquals(0, bus.getListeners().size());

        bus.publish(new TaskCreated(task));
        assertEquals(1, received.size()); // No new events received
    }

    // 7. Java Streams & Aggregations
    @Test
    public void testReportingServiceAggregations() {
        ReportingService reporting = new ReportingService();
        User user = new User(1, "Alice", "alice@example.com");

        LocalDateTime now = LocalDateTime.now();
        Task completed1 = new Task(1, "Task 1", "", LocalDate.now(), Status.DONE, Priority.HIGH, user, now, now.minusHours(4), now);
        Task overdueTask = new Task(2, "Task 2", "", LocalDate.now().minusDays(2), Status.OVERDUE, Priority.HIGH, user, null, now.minusDays(3), now);

        List<Task> tasks = List.of(completed1, overdueTask);

        // Aggregation 1: Completed per user
        Map<User, Long> perUser = reporting.tasksCompletedPerUserThisWeek(tasks);
        assertEquals(1L, (long) perUser.get(user));

        // Aggregation 2: Overdue by priority
        Map<Priority, Long> overdueByPriority = reporting.overdueCountByPriority(tasks);
        assertEquals(1L, (long) overdueByPriority.get(Priority.HIGH));

        // Aggregation 3: Average time to completion
        Optional<Duration> avgTime = reporting.averageTimeToCompletion(tasks);
        assertTrue(avgTime.isPresent());
        assertEquals(4 * 3600, avgTime.get().getSeconds());
    }
}
