package com.example.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.example.bot.wasp.WaspController;
import com.example.config.WaspConfig;

/** The "Wasp Control" window: a big coordinate form plus START/STOP/CLOSE. */
public final class WaspWindow {

    private WaspWindow() {
    }

    /**
     * @param telegramX, telegramY, telegramColor, token, chatId defaults copied
     *        from the main window so the fields start pre-filled.
     */
    public static void open(String telegramX, String telegramY, String telegramColor,
            String token, String chatId) {
        JFrame frame = new JFrame("Wasp Control");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(520, 760);
        frame.setLayout(new BorderLayout());

        WaspController controller = new WaspController();

        JPanel panel = new JPanel(new GridLayout(0, 2));
        JTextField food = new JTextField("4");
        JTextField targetX = new JTextField("1767");
        JTextField targetY = new JTextField("1492");
        JTextField targetColor = new JTextField("255,0,0");
        JTextField robakX = new JTextField("1759");
        JTextField robakY = new JTextField("1493");
        JTextField robakColor = new JTextField("70,70,70");
        JTextField rope1X = new JTextField("1433");
        JTextField rope1Y = new JTextField("1542");
        JTextField rope2X = new JTextField("1209");
        JTextField rope2Y = new JTextField("1372");
        JTextField drop1X = new JTextField("1433");
        JTextField drop1Y = new JTextField("1427");
        JTextField drop2X = new JTextField("1208");
        JTextField drop2Y = new JTextField("1262");
        JTextField healX = new JTextField("1822");
        JTextField healY = new JTextField("1242");
        JTextField healColor = new JTextField("241,97,97");
        JTextField lootX = new JTextField("923");
        JTextField lootY = new JTextField("2061");
        JTextField lootColor = new JTextField("240,180,0");
        JTextField localX = new JTextField("978");
        JTextField localY = new JTextField("595");
        JTextField localColor = new JTextField("247,95,95");
        JTextField musicX = new JTextField("55");
        JTextField musicY = new JTextField("70");
        JTextField tile1X = new JTextField("1267");
        JTextField tile1Y = new JTextField("1373");
        JTextField tile2X = new JTextField("1267");
        JTextField tile2Y = new JTextField("1424");
        JTextField tile3X = new JTextField("1267");
        JTextField tile3Y = new JTextField("1479");
        JTextField tile4X = new JTextField("1323");
        JTextField tile4Y = new JTextField("1373");
        JTextField tile5X = new JTextField("1323");
        JTextField tile5Y = new JTextField("1426");
        JTextField tile6X = new JTextField("1321");
        JTextField tile6Y = new JTextField("1484");
        JTextField tile7X = new JTextField("1383");
        JTextField tile7Y = new JTextField("1370");
        JTextField tile8X = new JTextField("1383");
        JTextField tile8Y = new JTextField("1425");
        JTextField tile9X = new JTextField("1383");
        JTextField tile9Y = new JTextField("1480");

        JTextField telegramXField = new JTextField(telegramX);
        JTextField telegramYField = new JTextField(telegramY);
        JTextField telegramColorField = new JTextField(telegramColor);
        JTextField telegramTokenField = new JTextField(token);
        JTextField telegramChatIdField = new JTextField(chatId);

        UiUtils.addRow(panel, "FOOD:", food);
        UiUtils.addRow(panel, "TARGET_X:", targetX);
        UiUtils.addRow(panel, "TARGET_Y:", targetY);
        UiUtils.addRow(panel, "TARGET_COLOR (R,G,B):", targetColor);
        UiUtils.addRow(panel, "ROBAK_X:", robakX);
        UiUtils.addRow(panel, "ROBAK_Y:", robakY);
        UiUtils.addRow(panel, "ROBAK_COLOR (R,G,B):", robakColor);
        UiUtils.addRow(panel, "ROPE 1 X:", rope1X);
        UiUtils.addRow(panel, "ROPE 1 Y:", rope1Y);
        UiUtils.addRow(panel, "ROPE 2 X:", rope2X);
        UiUtils.addRow(panel, "ROPE 2 Y:", rope2Y);
        UiUtils.addRow(panel, "DROP 1 X:", drop1X);
        UiUtils.addRow(panel, "DROP 1 Y:", drop1Y);
        UiUtils.addRow(panel, "DROP 2 X:", drop2X);
        UiUtils.addRow(panel, "DROP 2 Y:", drop2Y);
        UiUtils.addRow(panel, "HEAL_X:", healX);
        UiUtils.addRow(panel, "HEAL_Y:", healY);
        UiUtils.addRow(panel, "HEAL_COLOR (R,G,B):", healColor);
        UiUtils.addRow(panel, "Loot message X:", lootX);
        UiUtils.addRow(panel, "Loot message Y:", lootY);
        UiUtils.addRow(panel, "Loot color (R,G,B):", lootColor);
        UiUtils.addRow(panel, "Local X:", localX);
        UiUtils.addRow(panel, "Local Y:", localY);
        UiUtils.addRow(panel, "Local color (R,G,B):", localColor);
        UiUtils.addRow(panel, "Music X:", musicX);
        UiUtils.addRow(panel, "Music Y:", musicY);
        UiUtils.addRow(panel, "Loot tile 1 X:", tile1X);
        UiUtils.addRow(panel, "Loot tile 1 Y:", tile1Y);
        UiUtils.addRow(panel, "Loot tile 2 X:", tile2X);
        UiUtils.addRow(panel, "Loot tile 2 Y:", tile2Y);
        UiUtils.addRow(panel, "Loot tile 3 X:", tile3X);
        UiUtils.addRow(panel, "Loot tile 3 Y:", tile3Y);
        UiUtils.addRow(panel, "Loot tile 4 X:", tile4X);
        UiUtils.addRow(panel, "Loot tile 4 Y:", tile4Y);
        UiUtils.addRow(panel, "Loot tile 5 X:", tile5X);
        UiUtils.addRow(panel, "Loot tile 5 Y:", tile5Y);
        UiUtils.addRow(panel, "Loot tile 6 X:", tile6X);
        UiUtils.addRow(panel, "Loot tile 6 Y:", tile6Y);
        UiUtils.addRow(panel, "Loot tile 7 X:", tile7X);
        UiUtils.addRow(panel, "Loot tile 7 Y:", tile7Y);
        UiUtils.addRow(panel, "Loot tile 8 X:", tile8X);
        UiUtils.addRow(panel, "Loot tile 8 Y:", tile8Y);
        UiUtils.addRow(panel, "Loot tile 9 X:", tile9X);
        UiUtils.addRow(panel, "Loot tile 9 Y:", tile9Y);
        UiUtils.addRow(panel, "Telegram check X:", telegramXField);
        UiUtils.addRow(panel, "Telegram check Y:", telegramYField);
        UiUtils.addRow(panel, "Telegram color (R,G,B):", telegramColorField);
        UiUtils.addRow(panel, "Telegram token:", telegramTokenField);
        UiUtils.addRow(panel, "Telegram chat_id:", telegramChatIdField);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
        JButton startButton = new JButton("START WASP");
        JButton stopButton = new JButton("STOP WASP");
        JButton closeButton = new JButton("CLOSE");
        stopButton.setBackground(Color.RED);

        startButton.addActionListener(e -> {
            try {
                int[][] lootTiles = new int[][] {
                        UiUtils.parsePoint(tile1X, tile1Y),
                        UiUtils.parsePoint(tile2X, tile2Y),
                        UiUtils.parsePoint(tile3X, tile3Y),
                        UiUtils.parsePoint(tile4X, tile4Y),
                        UiUtils.parsePoint(tile5X, tile5Y),
                        UiUtils.parsePoint(tile6X, tile6Y),
                        UiUtils.parseOptionalPoint(tile7X, tile7Y),
                        UiUtils.parseOptionalPoint(tile8X, tile8Y),
                        UiUtils.parseOptionalPoint(tile9X, tile9Y)
                };

                WaspConfig config = WaspConfig.builder()
                        .food(UiUtils.parseInt(food))
                        .target(UiUtils.parseInt(targetX), UiUtils.parseInt(targetY), UiUtils.parseColor(targetColor.getText()))
                        .robak(UiUtils.parseInt(robakX), UiUtils.parseInt(robakY), UiUtils.parseColor(robakColor.getText()))
                        .rope1(UiUtils.parseInt(rope1X), UiUtils.parseInt(rope1Y))
                        .rope2(UiUtils.parseInt(rope2X), UiUtils.parseInt(rope2Y))
                        .drop1(UiUtils.parseInt(drop1X), UiUtils.parseInt(drop1Y))
                        .drop2(UiUtils.parseInt(drop2X), UiUtils.parseInt(drop2Y))
                        .heal(UiUtils.parseInt(healX), UiUtils.parseInt(healY), UiUtils.parseColor(healColor.getText()))
                        .loot(UiUtils.parseInt(lootX), UiUtils.parseInt(lootY), UiUtils.parseColor(lootColor.getText()))
                        .local(UiUtils.parseInt(localX), UiUtils.parseInt(localY), UiUtils.parseColor(localColor.getText()))
                        .music(UiUtils.parseInt(musicX), UiUtils.parseInt(musicY))
                        .lootTiles(lootTiles)
                        .telegram(UiUtils.parseInt(telegramXField), UiUtils.parseInt(telegramYField),
                                UiUtils.parseColor(telegramColorField.getText()),
                                telegramTokenField.getText().trim(), telegramChatIdField.getText().trim())
                        .build();

                controller.start(config);
                startButton.setBackground(Color.GREEN);
                stopButton.setBackground(null);
            } catch (NumberFormatException ex) {
                System.out.println("Please enter valid WASP numbers.");
            }
        });

        stopButton.addActionListener(e -> {
            controller.stop();
            startButton.setBackground(null);
            stopButton.setBackground(Color.RED);
        });
        closeButton.addActionListener(e -> frame.dispose());
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(closeButton);

        frame.add(new JScrollPane(panel), BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                controller.stop();
            }
        });
        frame.setVisible(true);
    }
}
