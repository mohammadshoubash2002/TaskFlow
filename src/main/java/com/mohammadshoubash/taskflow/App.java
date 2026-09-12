package com.mohammadshoubash.taskflow;

import org.h2.tools.Server;

import com.mohammadshoubash.taskflow.cache.DueTodayCache;
import com.mohammadshoubash.taskflow.config.ConnectionManager;
import com.mohammadshoubash.taskflow.repository.TaskRepository;
import com.mohammadshoubash.taskflow.repository.TaskRepositoryJdbc;

public class App {
    public static void main(String[] args) throws Exception {
        ConnectionManager.initializeDatabase();
        
        Server webServer = Server.createWebServer("-webPort", "8082", "-tcpAllowOthers").start();
        
        System.out.println("H2 Web Console running at: http://localhost:8082");
        
        // TaskRepository taskRepo = new TaskRepositoryJdbc();
        // DueTodayCache dueTodayCache = new DueTodayCache();
        // dueTodayCache.getDueToday(taskRepo.findAll());

        Thread.currentThread().join();
    }
}
