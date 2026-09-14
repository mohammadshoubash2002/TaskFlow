# TaskFlow

A high-performance task management and scheduling platform built in core Java. TaskFlow demonstrates solid software engineering principles, classic design patterns, custom algorithmic sorting, Java Stream aggregations, in-memory caching, direct relational persistence via JDBC, and a clean MVC architecture.

---

## 1. Setup & Run Instructions (Zero Manual Configuration)

The application runs entirely on an **embedded in-memory H2 database** (`jdbc:h2:mem:taskflow;DB_CLOSE_DELAY=-1`). The relational schema (`schema.sql`) initializes automatically on application boot via `ConnectionManager.initializeDatabase()` with zero manual database setup or external daemon required.

### Prerequisites
* Java 17+ (or Java 21)
* Apache Maven 3.8+

### Build & Run Tests
```bash
mvn clean test
```

### Launch the Application CLI
```bash
mvn compile exec:java
```

### Web Console Access
Upon startup, an embedded H2 web server is launched automatically:
* **Console URL**: `http://localhost:8082`
* **JDBC URL**: `jdbc:h2:mem:taskflow;DB_CLOSE_DELAY=-1`
* **User**: `sa`
* **Password**: *(leave blank)*

## 2. Design Patterns: 2 Required + 1 Bonus

### Required Pattern 1 (Creational): Factory Pattern (`com.mohammadshoubash.taskflow.reminder`)
* **Classes**: `ReminderFactory`, `DeliveryMechanism`, `EmailDelivery`, `SmsDelivery`, `PushDelivery`.
* **Why it is used**:  
  Isolates delivery channel instantiation logic away from domain events and listeners. Based on user communication preferences (`EMAIL`, `SMS`, `PUSH`) or task urgency/priority, `ReminderFactory` dynamically produces the appropriate polymorphic delivery strategy with fallback defaults and defensive null handling. Adding new delivery channels in the future (e.g. `SlackDelivery`) requires zero modifications to calling code, adhering to the Open-Closed Principle (OCP).

### Required Pattern 2 (Behavioral): Observer / Pub-Sub Pattern (`com.mohammadshoubash.taskflow.event`)
* **Classes**: `EventBus`, `TaskEvent` (`TaskCreated`, `TaskCompleted`, `TaskOverdue`), `TaskEventListener`.
* **Listeners**: `TaskLoggingListener` (audit logging), `TaskReminderListener` (reminder dispatch).
* **Why it is used**:  
  Completely decouples core task mutation workflows in `TaskService` from cross-cutting side effects. When tasks are created, completed, or marked overdue, `TaskService` simply publishes an immutable event to the `EventBus`. Multiple independent subscribers react without `TaskService` having any coupling or direct knowledge of console logging or notification delivery mechanisms.

### Bonus Pattern (+1 Architectural): Model-View-Controller (MVC) Pattern (`com.mohammadshoubash.taskflow.cli`)
* **Classes**: `TaskFlowCli` (Controller), `TaskService`/`UserService`/Repositories (Model), Console Output (View).
* **Why it is used**:  
  Deliberately organizes the user interface into a thin controller pattern. `TaskFlowCli` acts solely as an input dispatcher that captures user commands, invokes the corresponding service methods, and delegates data formatting to the console. No business rules, sorting algorithms, database logic, or event dispatching leak into the CLI layer.

---

## 3. In-Memory Caching & Generic Abstractions (`com.mohammadshoubash.taskflow.cache`)

TaskFlow fulfills the generic collections requirement by implementing a generic caching layer:
* **Contract**: `Cache<K, V>` implemented by `InMemoryCache<K, V>`.
* **Application**: `DueTodayCache` caches the computation of tasks due today (`Cache<LocalDate, List<Task>>`).
* **Cache Lifecycle & Invalidation**:
  * **Cache Miss**: Tasks are loaded from persistent storage via `taskRepository.findAll()`, filtered for today's deadline, and placed into `DueTodayCache`.
  * **Cache Hit**: Subsequent lookups return the cached, unmodifiable list in $O(1)$ time with zero DB I/O.
  * **Invalidation**: Any state mutation on tasks (`createTask`, `completeTask`, `checkOverdueTasks`, `updateTask`, `deleteTask`, `rescheduleTask`) proactively calls `dueTodayCache.invalidate()`, guaranteeing data consistency.

---

## 4. Design Judgment Call: Task Rescheduling & Linked Reminders Lifecycle

When a task's due date is rescheduled via `Task.reschedule(newDueDate)` and `TaskService.rescheduleTask(taskId, newDueDate)`, the system addresses the under-specified domain behavior of how linked reminders and task state should behave:

* **Clearing Obsolete Reminders**: All pending, unsent reminders tied to the old deadline are automatically invalidated and deleted.
* **Creating Fresh Reminders**: A clean, new reminder is scheduled relative to the updated deadline.
* **State Consistency (`OVERDUE` $\to$ `IN_PROGRESS`)**: If a task was previously marked as `OVERDUE` and is successfully rescheduled to a future date, its status is automatically restored to `IN_PROGRESS`, maintaining logical consistency across the domain model.

---

## 5. Custom Algorithm: Hand-Rolled Merge Sort (`com.mohammadshoubash.taskflow.algorithm.TaskSorter`)

The Due Soon reporting feature strictly avoids `Collections.sort`, `Arrays.sort`, or `java.util.Comparator`. Instead, it features an independent, hand-rolled **Merge Sort**:
* **Time Complexity**: Guaranteed $O(N \log N)$ in best, average, and worst cases.
* **Space Complexity**: $O(N)$ auxiliary space during merge phase.
* **Stability**: Preserves the original relative order of tasks with identical deadlines, resolving tiebreakers using priority levels (`HIGH` > `MEDIUM` > `LOW`).

---

## 6. CLI Command Menu

```
========================================
       Welcome to TaskFlow CLI          
========================================
---------------- Menu ------------------
1. Add a Task
2. List Tasks Due Soon (Merge Sort)
3. List Tasks Due Today (In-Memory Cache)
4. List All Tasks
5. Mark Task as Complete
6. Check & Mark Overdue Tasks
7. View Analytics & Reports
8. Create a User
9. List All Users
10. Reschedule a Task
0. Exit
----------------------------------------
```
