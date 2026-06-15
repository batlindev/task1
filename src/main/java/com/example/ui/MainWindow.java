package com.example.ui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Robot;
import java.time.LocalTime;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.example.bot.BotController;
import com.example.config.AppConfig;
import com.example.config.BotSettings;
import com.example.util.RobotActions;
import com.example.util.TelegramClient;

/** The main "Bot Control" window. */
public final class MainWindow {

    private MainWindow() {
    }

    public static void show() {
        BotController controller = new BotController();

        JFrame frame = new JFrame("Bot Control");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 300);
        frame.setLayout(new GridLayout(20, 2));

        JLabel targetXLabel = new JLabel("TARGET_X:");
        JTextField targetXField = new JTextField("1767");
        JLabel targetYLabel = new JLabel("TARGET_Y:");
        JTextField targetYField = new JTextField("1492");
        JLabel healXLabel = new JLabel("HEAL_X:");
        JTextField healXField = new JTextField("1822");
        JLabel healYLabel = new JLabel("HEAL_Y:");
        JTextField healYField = new JTextField("1242");
        JLabel foodLabel = new JLabel("FOOD:");
        JTextField foodField = new JTextField("0");
        JLabel healColorLabel = new JLabel("HEAL_COLOR (R,G,B):");
        JTextField healColorField = new JTextField("241,97,97");
        JLabel valuableLootXLabel = new JLabel("Loot X:");
        JTextField valuableLootXField = new JTextField("0");
        JLabel valuableLootYLabel = new JLabel("Loot Y:");
        JTextField valuableLootYField = new JTextField("0");
        JCheckBox lootCheckBox = new JCheckBox("Track Loot Messages");
        JLabel check2XLabel = new JLabel("X:");
        JTextField check2XField = new JTextField("888");
        JLabel check2YLabel = new JLabel("Y:");
        JTextField check2YField = new JTextField("2074");
        JButton check2Button = new JButton("CHECK 2");

        JButton startButton = new JButton("START");
        JButton stopButton = new JButton("STOP");
        JButton checkButton = new JButton("CHECK");
        stopButton.setBackground(Color.RED);

        JLabel telegramTokenLabel = new JLabel("Telegram token:");
        JTextField telegramTokenField = new JTextField(AppConfig.telegramToken());
        JLabel telegramChatIdLabel = new JLabel("Telegram chat_id:");
        JTextField telegramChatIdField = new JTextField(AppConfig.telegramChatId());
        JLabel redThresholdLabel = new JLabel("Red rule (minR,maxG,maxB):");
        JTextField redThresholdField = new JTextField("240,240,0");
        JButton sendIfRedButton = new JButton("SEND TELEGRAM");
        JButton atrapa = new JButton("gili gili");
        JButton waspButton = new JButton("WASP");
        JButton bearButton = new JButton("BEAR");

        startButton.addActionListener(e -> {
            BotSettings settings;
            try {
                settings = BotSettings.builder()
                        .target(UiUtils.parseInt(targetXField), UiUtils.parseInt(targetYField))
                        .heal(UiUtils.parseInt(healXField), UiUtils.parseInt(healYField),
                                UiUtils.parseColor(healColorField.getText()))
                        .food(UiUtils.parseInt(foodField))
                        .telegramCheck(UiUtils.parseInt(check2XField), UiUtils.parseInt(check2YField),
                                UiUtils.parseColor(redThresholdField.getText()))
                        .telegram(telegramTokenField.getText().trim(), telegramChatIdField.getText().trim())
                        .build();
            } catch (NumberFormatException ex) {
                System.out.println("Please enter valid numbers.");
                return;
            }

            Runnable onAlert = () -> {
                controller.stop();
                System.exit(0);
            };
            controller.start(settings, onAlert);
            startButton.setBackground(Color.GREEN);
            stopButton.setBackground(null);
        });

        stopButton.addActionListener(e -> {
            controller.stop();
            startButton.setBackground(null);
            stopButton.setBackground(Color.RED);
        });

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

        waspButton.addActionListener(e -> WaspWindow.open(
                check2XField.getText(), check2YField.getText(), redThresholdField.getText(),
                telegramTokenField.getText(), telegramChatIdField.getText()));

        bearButton.addActionListener(e -> BearWindow.open(
                telegramTokenField.getText(), telegramChatIdField.getText()));

        frame.add(targetXLabel);
        frame.add(targetXField);
        frame.add(targetYLabel);
        frame.add(targetYField);
        frame.add(healXLabel);
        frame.add(healXField);
        frame.add(healYLabel);
        frame.add(healYField);
        frame.add(foodLabel);
        frame.add(foodField);
        frame.add(healColorLabel);
        frame.add(healColorField);
        frame.add(valuableLootXLabel);
        frame.add(valuableLootXField);
        frame.add(valuableLootYLabel);
        frame.add(valuableLootYField);
        frame.add(lootCheckBox);
        frame.add(startButton);
        frame.add(stopButton);
        frame.add(checkButton);
        frame.add(telegramTokenLabel);
        frame.add(telegramTokenField);
        frame.add(telegramChatIdLabel);
        frame.add(telegramChatIdField);
        frame.add(redThresholdLabel);
        frame.add(redThresholdField);
        frame.add(sendIfRedButton);
        frame.add(waspButton);
        frame.add(bearButton);
        frame.add(atrapa);
        frame.add(check2XLabel);
        frame.add(check2XField);
        frame.add(check2YLabel);
        frame.add(check2YField);
        frame.add(check2Button);
        frame.setVisible(true);
    }
}
