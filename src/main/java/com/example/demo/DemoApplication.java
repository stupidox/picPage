package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Scanner;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);

        // 启动一个非守护线程，监听控制台输入
        Thread inputThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            System.out.println("\n\n✅ 应用运行中... 输入 0 并回车即可关闭\n\n");
            while (context.isRunning()) {
                String line = scanner.nextLine().trim();
                if ("0".equals(line)) {
                    System.out.println("正在关闭应用...");
                    context.close(); // 优雅关闭
                    break;
                }
            }
            scanner.close();
        });

        inputThread.setDaemon(false); // 必须设为 false！否则主线程（Web 容器）跑起来后，这个线程会被杀掉
        inputThread.start();
    }

}
