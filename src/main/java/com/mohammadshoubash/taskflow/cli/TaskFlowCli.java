package com.mohammadshoubash.taskflow.cli;

import java.io.InputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import com.mohammadshoubash.taskflow.domain.Reminder.DeliveryChannel;
import com.mohammadshoubash.taskflow.domain.Task;
import com.mohammadshoubash.taskflow.domain.Task.Priority;
import com.mohammadshoubash.taskflow.domain.User;
import com.mohammadshoubash.taskflow.exception.DuplicateUserException;
import com.mohammadshoubash.taskflow.exception.InvalidEmailException;
import com.mohammadshoubash.taskflow.service.ReportingService;
import com.mohammadshoubash.taskflow.service.TaskService;
import com.mohammadshoubash.taskflow.service.UserService;

/**
 * Thin CLI Controller (MVC pattern).
 * Acts as the controller layer: captures input, delegates to services (model),
 * and prints responses (view) without containing any domain or business logic.
 */
public class TaskFlowCli {

    private final TaskService taskService;
    private final UserService userService;
    private final ReportingService reportingService;
    private final Scanner scanner;
    private final PrintStream out;

    public TaskFlowCli(TaskService taskService, UserService userService, ReportingService reportingService) {
        this(taskService, userService, reportingService, System.in, System.out);
    }

    public TaskFlowCli(TaskService taskService, UserService userService, ReportingService reportingService,
                       InputStream in, PrintStream out) {
        this.taskService = taskService;
        this.userService = userService;
        this.reportingService = reportingService;
        this.scanner = new Scanner(in);
        this.out = out;
    }

    public void start() {
        boolean running = true;
        out.println("========================================");
        out.println("       Welcome to TaskFlow CLI          ");
        out.println("========================================");

        while (running) {
            printMenu();
            out.print("Select an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> handleAddTask();
                case "2" -> handleListDueSoon();
                case "3" -> handleListAllTasks();
                case "4" -> handleMarkTaskComplete();
                case "5" -> handleCheckOverdueTasks();
                case "6" -> handleViewAnalytics();
                case "7" -> handleCreateUser();
                case "8" -> handleListAllUsers();
                case "0" -> {
                    out.println("Exiting TaskFlow. Goodbye!");
                    running = false;
                }
                default -> out.println("Invalid option. Please try again.");
            }
            out.println();
        }
    }

    private void printMenu() {
        out.println("---------------- Menu ------------------");
        out.println("1. Add a Task");
        out.println("2. List Tasks Due Soon");
        out.println("3. List All Tasks");
        out.println("4. Mark Task as Complete");
        out.println("5. Check & Mark Overdue Tasks");
        out.println("6. View Analytics & Reports");
        out.println("7. Create a User");
        out.println("8. List All Users");
        out.println("0. Exit");
        out.println("----------------------------------------");
    }

    private void handleAddTask() {
        out.print("Enter task title: ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) {
            out.println("Error: Task title cannot be empty.");
            return;
        }

        out.print("Enter task description: ");
        String description = scanner.nextLine().trim();

        out.print("Enter due date (YYYY-MM-DD) [leave empty for today + 7 days]: ");
        String dateStr = scanner.nextLine().trim();
        LocalDate dueDate;
        if (dateStr.isEmpty()) {
            dueDate = LocalDate.now().plusDays(7);
        } else {
            try {
                dueDate = LocalDate.parse(dateStr);
            } catch (DateTimeParseException e) {
                out.println("Invalid date format. Using today + 7 days as default.");
                dueDate = LocalDate.now().plusDays(7);
            }
        }

        out.print("Enter priority (LOW, MEDIUM, HIGH) [default MEDIUM]: ");
        String priorityStr = scanner.nextLine().trim().toUpperCase();
        Priority priority;
        try {
            priority = priorityStr.isEmpty() ? Priority.MEDIUM : Priority.valueOf(priorityStr);
        } catch (IllegalArgumentException e) {
            out.println("Invalid priority. Defaulting to MEDIUM.");
            priority = Priority.MEDIUM;
        }

        out.print("Enter assignee user ID (optional, press Enter to skip): ");
        String userIdStr = scanner.nextLine().trim();
        User assignedUser = null;
        if (!userIdStr.isEmpty()) {
            try {
                int userId = Integer.parseInt(userIdStr);
                assignedUser = userService.getUserById(userId).orElse(null);
                if (assignedUser == null) {
                    out.println("Warning: User with ID " + userId + " not found. Creating unassigned task.");
                }
            } catch (NumberFormatException e) {
                out.println("Invalid user ID. Creating unassigned task.");
            }
        }

        Task newTask = new Task(0, title, description, dueDate, priority, assignedUser);
        Task savedTask = taskService.createTask(newTask);
        out.println("Task created successfully with ID: #" + savedTask.getId());
    }

    private void handleListDueSoon() {
        out.println("\n--- Tasks Due Soon (Sorted by Due Date) ---");
        List<Task> dueSoon = taskService.getDueSoonReport();
        if (dueSoon.isEmpty()) {
            out.println("No tasks due in the next 7 days.");
        } else {
            dueSoon.forEach(t -> out.printf("#%d | %s | Due: %s | Priority: %s | Status: %s%n",
                    t.getId(), t.getTitle(), t.getDueDate(), t.getPriority(), t.getStatus()));
        }
    }

    private void handleListAllTasks() {
        out.println("\n--- All Tasks ---");
        List<Task> allTasks = taskService.getAllTasks();
        if (allTasks.isEmpty()) {
            out.println("No tasks found.");
        } else {
            allTasks.forEach(t -> {
                String assignee = (t.getAssignedUser() != null) ? t.getAssignedUser().getName() : "Unassigned";
                out.printf("#%d | %s | Due: %s | Priority: %s | Status: %s | Assigned: %s%n",
                        t.getId(), t.getTitle(), t.getDueDate(), t.getPriority(), t.getStatus(), assignee);
            });
        }
    }

    private void handleMarkTaskComplete() {
        out.print("Enter task ID to mark as complete: ");
        String idStr = scanner.nextLine().trim();
        try {
            int taskId = Integer.parseInt(idStr);
            Optional<Task> completed = taskService.completeTask(taskId);
            if (completed.isPresent()) {
                out.println("Task #" + taskId + " marked as DONE!");
            } else {
                out.println("Task with ID #" + taskId + " not found.");
            }
        } catch (NumberFormatException e) {
            out.println("Invalid task ID format.");
        }
    }

    private void handleCheckOverdueTasks() {
        out.println("\n--- Checking for Overdue Tasks ---");
        List<Task> overdue = taskService.checkOverdueTasks();
        if (overdue.isEmpty()) {
            out.println("No new overdue tasks found.");
        } else {
            out.println("Identified and marked " + overdue.size() + " overdue task(s):");
            overdue.forEach(t -> out.printf("#%d | %s | Due: %s%n", t.getId(), t.getTitle(), t.getDueDate()));
        }
    }

    private void handleViewAnalytics() {
        out.println("\n--- TaskFlow Analytics & Aggregations ---");
        List<Task> tasks = taskService.getAllTasks();

        out.println("\n1. Completed Tasks Per User This Week:");
        Map<User, Long> userCompletions = reportingService.tasksCompletedPerUserThisWeek(tasks);
        if (userCompletions.isEmpty()) {
            out.println("   No tasks completed this week.");
        } else {
            userCompletions.forEach((u, count) -> out.printf("   - %s (%s): %d task(s)%n", u.getName(), u.getEmail(), count));
        }

        out.println("\n2. Overdue Task Count by Priority:");
        Map<Priority, Long> overdueByPriority = reportingService.overdueCountByPriority(tasks);
        if (overdueByPriority.isEmpty()) {
            out.println("   No overdue tasks.");
        } else {
            overdueByPriority.forEach((p, count) -> out.printf("   - %s: %d task(s)%n", p, count));
        }

        out.println("\n3. Average Time-to-Completion:");
        Optional<Duration> avgDuration = reportingService.averageTimeToCompletion(tasks);
        if (avgDuration.isPresent()) {
            long totalSeconds = avgDuration.get().getSeconds();
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            long seconds = totalSeconds % 60;
            out.printf("   Average: %d hours, %d minutes, %d seconds%n", hours, minutes, seconds);
        } else {
            out.println("   Insufficient data (no completed tasks with completion timestamps).");
        }
    }

    private void handleCreateUser() {
        out.print("Enter user name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            out.println("Error: Name cannot be empty.");
            return;
        }

        out.print("Enter user email: ");
        String email = scanner.nextLine().trim();

        out.print("Enter preferred channel (EMAIL, SMS, PUSH) [default EMAIL]: ");
        String channelStr = scanner.nextLine().trim().toUpperCase();
        DeliveryChannel channel;
        try {
            channel = channelStr.isEmpty() ? DeliveryChannel.EMAIL : DeliveryChannel.valueOf(channelStr);
        } catch (IllegalArgumentException e) {
            out.println("Invalid delivery channel. Defaulting to EMAIL.");
            channel = DeliveryChannel.EMAIL;
        }

        try {
            User user = new User(0, name, email, channel);
            User saved = userService.createUser(user);
            out.println("User created successfully with ID: #" + saved.getId());
        } catch (InvalidEmailException e) {
            out.println("Error: " + e.getMessage());
        } catch (DuplicateUserException e) {
            out.println("Error: " + e.getMessage());
        }
    }

    private void handleListAllUsers() {
        out.println("\n--- All Users ---");
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) {
            out.println("No users found.");
        } else {
            users.forEach(u -> out.printf("#%d | %s | Email: %s | Preferred: %s%n",
                    u.getId(), u.getName(), u.getEmail(), u.getPreferredChannel()));
        }
    }
}
