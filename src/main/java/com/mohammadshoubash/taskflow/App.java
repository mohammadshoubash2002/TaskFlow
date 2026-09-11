package com.mohammadshoubash.taskflow;

import org.h2.tools.Server;
import com.mohammadshoubash.taskflow.config.ConnectionManager;

public class App {
    public static void main(String[] args) throws Exception {
        ConnectionManager.initializeDatabase();
        
        Server webServer = Server.createWebServer("-webPort", "8082", "-tcpAllowOthers").start();
        
        System.out.println("H2 Web Console running at: http://localhost:8082");
        
        Thread.currentThread().join(); 
    }
}
