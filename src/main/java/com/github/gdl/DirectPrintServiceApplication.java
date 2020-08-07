package com.github.gdl;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;

import java.awt.*;

@SpringBootApplication
public class DirectPrintServiceApplication {
    public static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        context = new SpringApplicationBuilder(DirectPrintServiceApplication.class).headless(false).run(args);
        if(SystemTray.isSupported()){
            DesktopConfig desktopConfig = new DesktopConfig();
            desktopConfig.openTrayIcon();
        }

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
