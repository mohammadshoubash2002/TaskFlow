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

---

## 2. Persistence Architecture: JDBC vs. JPA Justification

TaskFlow deliberately chose **direct JDBC with raw SQL** (via `TaskRepositoryJdbc`, `UserRepositoryJdbc`, `ReminderRepositoryJdbc`) instead of an ORM like JPA/Hibernate for the following reasons:

1. **Zero Runtime Overhead & Deterministic Execution**:  
   JPA/Hibernate introduces heavy entity state management (`transient`, `managed`, `detached`), first/second-level caches, reflection proxies, and hidden SQL generation. Direct JDBC guarantees exact, predictable query execution with negligible memory footprint and sub-second startup times.
2. **Elimination of Common ORM Hazards**:  
   ORM frameworks frequently introduce the notorious **N+1 select query problem**, subtle lazy initialization exceptions outside active persistence contexts, and cascade side-effects. With JDBC, every query is explicitly authored, optimized, and controlled.
3. **Architectural Purity & Portability**:  
   By abstracting data access behind generic interface contracts (`Repository<T, ID>`, `TaskRepository`, `UserRepository`), the domain and service layers stay completely decoupled from database internals. Connection safety and resource leak prevention are enforced cleanly using Java's `try-with-resources`.

---

## 3. Design Patterns: 2 Required + 1 Bonus

### Required Pattern 1 (Creational): Factory Pattern (`com.mohammadshoubash.taskflow.reminder`)
* **Classes**: `ReminderFactory`, `DeliveryMechanism`, `EmailDelivery`, `SmsDelivery`, `PushDelivery`.
* **Why it is used**:  
  Isolates delivery channel instantiation logic away from domain events and listeners. Based on user communication preferences (`EMAIL`, `SMS`, `PUSH`) or task urgency/priority, `ReminderFactory` dynamically produces the appropriate polymorphic delivery strategy with fallback defaults and defensive null handling. Adding new delivery channels in the future (e.g. `SlackDelivery`) requires zero modifications to calling code, adhering to the Open-Closed Principle (OCP).

### Required Pattern 2 (Behavioral): Observer / Pub-Sub Pattern (`com.mohammadshoubash.taskflow.event`)
* **Classes**: `EventBus`, `TaskEvent` (`TaskCreated`, `TaskCompleted`, `TaskOverdue`), `TaskEventListener`.
* **Listeners**: `TaskLoggingListener`, `TaskReminderListener`, `TaskStatsListener`.
* **Why it is used**:  
  Completely decouples core task mutation workflows in `TaskService` from cross-cutting side effects. When tasks are created, completed, or marked overdue, `TaskService` simply publishes an immutable event to the `EventBus`. Multiple independent subscribers react asynchronously or synchronously without `TaskService` having any coupling or direct knowledge of logging, notification systems, or real-time metrics counters.

### Bonus Pattern (+1 Architectural): Model-View-Controller (MVC) Pattern (`com.mohammadshoubash.taskflow.cli`)
* **Classes**: `TaskFlowCli` (Controller), `TaskService`/`UserService`/Repositories (Model), Console Output (View).
* **Why it is used**:  
  Deliberately organizes the user interface into a thin controller pattern. `TaskFlowCli` acts solely as an input dispatcher that captures user commands, invokes the corresponding service methods, and delegates data formatting to the console. No business rules, sorting algorithms, database logic, or event dispatching leak into the CLI layer.

---

## 4. Design Judgment Call: Task Rescheduling & Linked Reminders Lifecycle

When a task's due date is rescheduled via `Task.reschedule(newDueDate)` and `TaskService.rescheduleTask(taskId, newDueDate)`, an under-specified behavior in the domain is how linked, pending reminders should be handled. We considered three alternatives: leaving existing reminders untouched, shifting their trigger times by the calendar delta, or canceling and recreating them relative to the new date. Leaving reminders untouched causes alert fatigue and premature notifications for tasks that are no longer urgent, while shifting by delta risks preserving obsolete trigger intervals that may no longer make sense. We deliberately chose to invalidate and delete all pending, unsent reminders upon rescheduling, immediately scheduling a fresh reminder aligned with the updated deadline. Furthermore, if a task was previously marked `OVERDUE` and is rescheduled into the future, its status is automatically restored to `IN_PROGRESS` to maintain state consistency across the system.

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
2. List Tasks Due Soon
3. List All Tasks
4. Mark Task as Complete
5. Check & Mark Overdue Tasks
6. View Analytics & Reports
7. Create a User
8. List All Users
9. Reschedule a Task
0. Exit
----------------------------------------
```
