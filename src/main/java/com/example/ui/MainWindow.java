package com.example.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Robot;
import java.io.IOException;
import java.time.LocalTime;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.example.config.AppConfig;
import com.example.config.AppSettings;
import com.example.config.TaskPresetStore;
import com.example.util.RobotActions;
import com.example.util.TelegramClient;

/** The main "Bot Control" window. Owns the single preset bar for both windows. */
public final class MainWindow {

    private MainWindow() {
    }

    public static void show() {
        AppSettings settings = new AppSettings();

        JFrame frame = new JFrame("Bot Control");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(320, 340);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(0, 2));

        JButton checkButton = new JButton("CHECK");

        JLabel telegramTokenLabel = new JLabel("Telegram token:");
        JTextField telegramTokenField = new JTextField(AppConfig.telegramToken());
        JLabel telegramChatIdLabel = new JLabel("Telegram chat_id:");
        JTextField telegramChatIdField = new JTextField(AppConfig.telegramChatId());
        JLabel redThresholdLabel = new JLabel("Red rule (minR,maxG,maxB):");
        JTextField redThresholdField = new JTextField("240,240,0");
        JButton sendIfRedButton = new JButton("SEND TELEGRAM");
        JButton readMeButton = new JButton("readMe");
        JButton taskButton = new JButton("TASK");

        JLabel check2XLabel = new JLabel("X:");
        JTextField check2XField = new JTextField("888");
        JLabel check2YLabel = new JLabel("Y:");
        JTextField check2YField = new JTextField("2074");
        JButton check2Button = new JButton("CHECK 2");

        // Bind Bot Control inputs into the shared registry so the preset covers them.
        settings.bind("telegramToken", telegramTokenField);
        settings.bind("telegramChatId", telegramChatIdField);
        settings.bind("redThreshold", redThresholdField);
        settings.bind("check2X", check2XField);
        settings.bind("check2Y", check2YField);

        checkButton.addActionListener(e -> CheckWindow.open());

        check2Button.addActionListener(e -> {
            final int x;
            final int y;
            try {
                x = UiUtils.parseInt(check2XField);
                y = UiUtils.parseInt(check2YField);
            } catch (NumberFormatException ex) {
                System.out.println("Please enter valid X and Y values.");
                return;
            }
            new Thread(() -> {
                try {
                    System.out.println("CHECK 2 START - click in 3 seconds...");
                    RobotActions.sleep(3000);
                    Robot robot = new Robot();
                    RobotActions.clickMouse(robot, x, y);
                    Color pixelColor = robot.getPixelColor(x, y);
                    System.out.println("Clicked at: x = " + x + ", y = " + y);
                    System.out.println("Color at (" + x + ", " + y + "): " + pixelColor);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        sendIfRedButton.addActionListener(e -> {
            String token = telegramTokenField.getText().trim();
            String chatId = telegramChatIdField.getText().trim();
            System.out.println(LocalTime.now() + " Sending Telegram message");
            TelegramClient.sendMessage(token, chatId, "wiadomosc");
        });

        taskButton.addActionListener(e -> TaskWindow.open(settings));

        readMeButton.addActionListener(e -> ReadMeWindow.open());

        panel.add(checkButton);
        panel.add(new JLabel());
        panel.add(telegramTokenLabel);
        panel.add(telegramTokenField);
        panel.add(telegramChatIdLabel);
        panel.add(telegramChatIdField);
        panel.add(redThresholdLabel);
        panel.add(redThresholdField);
        panel.add(sendIfRedButton);
        panel.add(taskButton);
        panel.add(readMeButton);
        panel.add(new JLabel());
        panel.add(check2XLabel);
        panel.add(check2XField);
        panel.add(check2YLabel);
        panel.add(check2YField);
        panel.add(check2Button);

        // Preset bar: one preset persists every Bot Control + Task Control field.
        TaskPresetStore store = new TaskPresetStore();
        JPanel presetPanel = new JPanel(new BorderLayout(4, 0));
        JComboBox<String> presetBox = new JComboBox<>();
        JButton saveButton = new JButton("Zapisz");
        JButton deleteButton = new JButton("Usuń");
        JPanel presetButtons = new JPanel(new GridLayout(1, 2, 4, 0));
        presetButtons.add(saveButton);
        presetButtons.add(deleteButton);
        presetPanel.add(new JLabel("Presety:"), BorderLayout.WEST);
        presetPanel.add(presetBox, BorderLayout.CENTER);
        presetPanel.add(presetButtons, BorderLayout.EAST);

        // Suppress the load-on-select handler while we repopulate the dropdown.
        final boolean[] loading = new boolean[1];
        Runnable refreshPresets = () -> {
            loading[0] = true;
            presetBox.removeAllItems();
            presetBox.addItem("");
            for (String name : store.list()) {
                presetBox.addItem(name);
            }
            loading[0] = false;
        };
        refreshPresets.run();

        presetBox.addActionListener(e -> {
            if (loading[0]) return;
            Object sel = presetBox.getSelectedItem();
            if (sel == null || sel.toString().isEmpty()) return;
            settings.apply(store.load(sel.toString()));
        });

        saveButton.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(frame, "Nazwa ustawień:", "Zapisz preset",
                    JOptionPane.PLAIN_MESSAGE);
            if (name == null || name.trim().isEmpty()) return;
            name = name.trim();
            try {
                store.save(name, settings.snapshot());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Nie udało się zapisać: " + ex.getMessage(),
                        "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }
            refreshPresets.run();
            presetBox.setSelectedItem(name);
        });

        deleteButton.addActionListener(e -> {
            Object sel = presetBox.getSelectedItem();
            if (sel == null || sel.toString().isEmpty()) return;
            String name = sel.toString();
            int ans = JOptionPane.showConfirmDialog(frame, "Usunąć preset \"" + name + "\"?",
                    "Usuń preset", JOptionPane.YES_NO_OPTION);
            if (ans != JOptionPane.YES_OPTION) return;
            store.delete(name);
            refreshPresets.run();
        });

        frame.add(presetPanel, BorderLayout.NORTH);
        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }
}
