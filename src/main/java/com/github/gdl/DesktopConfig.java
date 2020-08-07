package com.github.gdl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class DesktopConfig {
    private static final Logger log = LoggerFactory.getLogger(DesktopConfig.class);
    private TrayIcon icon;

    @Bean
    public void openTrayIcon() {
        icon = new TrayIcon(createTrayIcon());
        icon.setImageAutoSize(true);
        icon.setPopupMenu(createPopupMenu());

        try {
            SystemTray.getSystemTray().add(icon);
            icon.displayMessage("Direct Print", "Application started", TrayIcon.MessageType.INFO);
        } catch (AWTException e) {
            e.printStackTrace();
        }

    }

    private PopupMenu createPopupMenu() {
        PopupMenu popupMenu = new PopupMenu();

        MenuItem about = new MenuItem();
        about.setLabel("About");
        about.addActionListener(actionEvent -> JOptionPane.showMessageDialog(null, "Direct Print v.0.1"));
        popupMenu.add(about);
        popupMenu.addSeparator();

        MenuItem restart = new MenuItem();
        restart.setLabel("Restart Service");
        restart.addActionListener(actionEvent -> DirectPrintServiceApplication.restart());
        popupMenu.add(restart);

        MenuItem exit = new MenuItem();
        exit.setLabel("Exit");
        exit.addActionListener(actionEvent -> {
            log.info("Exiting ...");
            SystemTray.getSystemTray().remove(icon);
            SpringApplication.exit(DirectPrintServiceApplication.context);
        });
        popupMenu.add(exit);
        return popupMenu;
    }

    private Image createTrayIcon() {
        try {

            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("images/icons8-send-to-printer-100.png");
            ImageIcon icon = new ImageIcon(ImageIO.read(inputStream));
            return icon.getImage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
