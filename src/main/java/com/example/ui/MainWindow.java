package com.example.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Robot;
import java.io.IOException;
import java.time.LocalTime;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.example.config.AppSettings;
import com.example.config.DefaultSettings;
import com.example.config.TaskPresetStore;
import com.example.util.RobotActions;
import com.example.util.TelegramClient;

/**
 * The "main page" window. Stacks, top to bottom: the preset bar, a collapsible
 * Telegram section, the inline Task panel, and a collapsible Check section.
 * Owns the single preset that persists every field across the whole page.
 */
public final class MainWindow {

    private MainWindow() {
    }

    public static void show() {
        AppSettings settings = new AppSettings();

        JFrame frame = new JFrame("main page");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(540, 780);
        frame.setLayout(new BorderLayout());

        // --- Telegram section (collapsible, default closed) -----------------
        JTextField telegramTokenField = new JTextField(DefaultSettings.get("telegramToken"), 18);
        JTextField telegramChatIdField = new JTextField(DefaultSettings.get("telegramChatId"), 18);
        JButton sendButton = new JButton("SEND TELEGRAM");
        JButton readMeButton = new JButton("readMe");

        settings.bind("telegramToken", telegramTokenField);
        settings.bind("telegramChatId", telegramChatIdField);

        sendButton.addActionListener(e -> {
            String token = telegramTokenField.getText().trim();
            String chatId = telegramChatIdField.getText().trim();
            System.out.println(LocalTime.now() + " Sending Telegram message");
            TelegramClient.sendMessage(token, chatId, "message");
        });
        readMeButton.addActionListener(e -> ReadMeWindow.open());

        // Plain box (the collapsible header already titles it "Telegram").
        JPanel telegramBody = UiUtils.verticalBox();
        telegramBody.add(UiUtils.row("token:", telegramTokenField));
        telegramBody.add(UiUtils.row("chat_id:", telegramChatIdField));
        telegramBody.add(UiUtils.row(sendButton, readMeButton));

        // --- Check section (collapsible, default closed) --------------------
        JButton checkButton = new JButton("CHECK");
        JTextField check2XField = new PlaceholderTextField("X", DefaultSettings.get("check2X"));
        JTextField check2YField = new PlaceholderTextField("Y", DefaultSettings.get("check2Y"));
        JButton check2Button = new JButton("CHECK 2");

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

        // Collapsible header titles it "Check". Two grouped sub-sections so it
        // is obvious the X/Y inputs belong to CHECK 2.
        JPanel checkBody = UiUtils.verticalBox();

        JPanel checkGroup = UiUtils.category("CHECK");
        checkGroup.add(UiUtils.row(checkButton));
        checkBody.add(checkGroup);

        JPanel check2Group = UiUtils.category("CHECK 2");
        check2Group.add(UiUtils.row("X:", check2XField, "Y:", check2YField));
        check2Group.add(UiUtils.row(check2Button));
        checkBody.add(check2Group);

        // --- Content: task panel -> telegram (closed) -> check (closed) -----
        // Task panel = generator, START/STOP, settings. Telegram + Check below.
        JPanel content = UiUtils.verticalBox();
        JComponent taskPanel = TaskPanel.build(settings);
        taskPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(taskPanel);
        content.add(UiUtils.collapsible("Telegram", telegramBody, false));
        content.add(UiUtils.collapsible("Check", checkBody, false));

        // --- Preset bar (unchanged), pinned to the top ----------------------
        TaskPresetStore store = new TaskPresetStore();
        JPanel presetPanel = new JPanel(new BorderLayout(4, 0));
        JComboBox<String> presetBox = new JComboBox<>();
        JButton saveButton = new JButton("Save");
        JButton deleteButton = new JButton("Delete");
        JPanel presetButtons = new JPanel(new GridLayout(1, 2, 4, 0));
        presetButtons.add(saveButton);
        presetButtons.add(deleteButton);
        presetPanel.add(new JLabel("Presets:"), BorderLayout.WEST);
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
            String name = JOptionPane.showInputDialog(frame, "Settings name:", "Save preset",
                    JOptionPane.PLAIN_MESSAGE);
            if (name == null || name.trim().isEmpty()) return;
            name = name.trim();
            try {
                store.save(name, settings.snapshot());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Could not save: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            refreshPresets.run();
            presetBox.setSelectedItem(name);
        });

        deleteButton.addActionListener(e -> {
            Object sel = presetBox.getSelectedItem();
            if (sel == null || sel.toString().isEmpty()) return;
            String name = sel.toString();
            int ans = JOptionPane.showConfirmDialog(frame, "Delete preset \"" + name + "\"?",
                    "Delete preset", JOptionPane.YES_NO_OPTION);
            if (ans != JOptionPane.YES_OPTION) return;
            store.delete(name);
            refreshPresets.run();
        });

        // NORTH wrapper keeps content at natural height (no vertical stretch).
        JPanel northHold = new JPanel(new BorderLayout());
        northHold.add(content, BorderLayout.NORTH);

        frame.add(presetPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(northHold), BorderLayout.CENTER);
        frame.setVisible(true);
    }
}
