package com.mohammadshoubash.taskflow;

import org.h2.tools.Server;

import com.mohammadshoubash.taskflow.cli.TaskFlowCli;
import com.mohammadshoubash.taskflow.config.ConnectionManager;
import com.mohammadshoubash.taskflow.event.EventBus;
import com.mohammadshoubash.taskflow.event.TaskLoggingListener;
import com.mohammadshoubash.taskflow.event.TaskReminderListener;
import com.mohammadshoubash.taskflow.repository.TaskRepository;
import com.mohammadshoubash.taskflow.repository.TaskRepositoryJdbc;
import com.mohammadshoubash.taskflow.repository.UserRepository;
import com.mohammadshoubash.taskflow.repository.UserRepositoryJdbc;
import com.mohammadshoubash.taskflow.service.ReportingService;
import com.mohammadshoubash.taskflow.service.TaskService;
import com.mohammadshoubash.taskflow.service.UserService;

public class App {
    public static void main(String[] args) throws Exception {
        ConnectionManager.initializeDatabase();
        
        Server webServer = Server.createWebServer("-webPort", "8082", "-tcpAllowOthers").start();
        System.out.println("H2 Web Console running at: http://localhost:8082\n");

        // Event-driven pub/sub wiring (Observer Pattern)
        EventBus eventBus = new EventBus();
        eventBus.subscribe(new TaskLoggingListener());
        eventBus.subscribe(new TaskReminderListener());

        // Infrastructure & Services (Model layer)
        TaskRepository taskRepository = new TaskRepositoryJdbc();
        UserRepository userRepository = new UserRepositoryJdbc();

        ReportingService reportingService = new ReportingService();
        TaskService taskService = new TaskService(taskRepository, eventBus, reportingService);
        UserService userService = new UserService(userRepository);

        // CLI Controller (MVC Pattern)
        TaskFlowCli cli = new TaskFlowCli(taskService, userService, reportingService);
        cli.start();

        webServer.stop();
    }
}
