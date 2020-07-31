package com.github.gdl;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
public class DirectPrintServiceApplication {
    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        context = SpringApplication.run(DirectPrintServiceApplication.class, args);
    }

    public static void restart() {
        ApplicationArguments args = context.getBean(ApplicationArguments.class);
        Thread thread = new Thread(() -> {
            context.close();
            context = SpringApplication.run(DirectPrintServiceApplication.class, args.getSourceArgs());
        });
        thread.setDaemon(false);
        thread.start();
    }

    @GetMapping("/restart")
    public void restarts() {
        DirectPrintServiceApplication.restart();
    }

}
