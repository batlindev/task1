package com.example.ui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Robot;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.example.bot.bear.BearController;
import com.example.config.BearConfig;
import com.example.config.BearPresetStore;

/**
 * The "Bear Control" window: minimap rectangle + the three uniquely-colored
 * patrol marks, plus START/STOP/CLOSE.
 *
 * Defaults come from analyzing minimap.png (panel inner area in screen coords;
 * player cross anchored at 1806,1177). The patrol order is 1 -> 2 -> 3 -> 2 -> ...
 */
public final class BearWindow {

    private BearWindow() {
    }

    public static void open(String token, String chatId) {
        BearController controller = new BearController();

        JFrame frame = new JFrame("Bear Control");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(520, 760);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(0, 2));
        JTextField mapX = new JTextField("1753");
        JTextField mapY = new JTextField("1122");
        JTextField mapW = new JTextField("107");
        JTextField mapH = new JTextField("110");
        JTextField tolerance = new JTextField("10");
        JTextField arrive = new JTextField("5");
        JTextField point1Color = new JTextField("1,1,240");      // niebieski (prawo)
        JTextField point2Color = new JTextField("227,0,15");     // czerwony (lewo od krzyzyka)
        JTextField point3Color = new JTextField("68,206,87");    // zielony (dol)
        JTextField targetX = new JTextField("1767");
        JTextField targetY = new JTextField("1492");
        JTextField targetColor = new JTextField("255,0,0");
        JTextField robakX = new JTextField("1759");
        JTextField robakY = new JTextField("1493");
        JTextField robakColor = new JTextField("70,70,70");
        JTextField lootX = new JTextField("923");
        JTextField lootY = new JTextField("2061");
        JTextField lootColor = new JTextField("240,180,0");
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
        JTextField healX = new JTextField("1822");
        JTextField healY = new JTextField("1242");
        JTextField healColor = new JTextField("241,97,97");
        JTextField butyX = new JTextField("1810");
        JTextField butyY = new JTextField("1391");
        JTextField butyColor = new JTextField("58,61,63");
        JTextField telegramTokenField = new JTextField(token);
        JTextField telegramChatIdField = new JTextField(chatId);

        // Stable key -> field registry, used to save/load presets generically.
        Map<String, JTextField> fields = new LinkedHashMap<>();
        fields.put("mapX", mapX);
        fields.put("mapY", mapY);
        fields.put("mapW", mapW);
        fields.put("mapH", mapH);
        fields.put("tolerance", tolerance);
        fields.put("arrive", arrive);
        fields.put("point1Color", point1Color);
        fields.put("point2Color", point2Color);
        fields.put("point3Color", point3Color);
        fields.put("targetX", targetX);
        fields.put("targetY", targetY);
        fields.put("targetColor", targetColor);
        fields.put("robakX", robakX);
        fields.put("robakY", robakY);
        fields.put("robakColor", robakColor);
        fields.put("lootX", lootX);
        fields.put("lootY", lootY);
        fields.put("lootColor", lootColor);
        fields.put("tile1X", tile1X);
        fields.put("tile1Y", tile1Y);
        fields.put("tile2X", tile2X);
        fields.put("tile2Y", tile2Y);
        fields.put("tile3X", tile3X);
        fields.put("tile3Y", tile3Y);
        fields.put("tile4X", tile4X);
        fields.put("tile4Y", tile4Y);
        fields.put("tile5X", tile5X);
        fields.put("tile5Y", tile5Y);
        fields.put("tile6X", tile6X);
        fields.put("tile6Y", tile6Y);
        fields.put("tile7X", tile7X);
        fields.put("tile7Y", tile7Y);
        fields.put("tile8X", tile8X);
        fields.put("tile8Y", tile8Y);
        fields.put("tile9X", tile9X);
        fields.put("tile9Y", tile9Y);
        fields.put("healX", healX);
        fields.put("healY", healY);
        fields.put("healColor", healColor);
        fields.put("butyX", butyX);
        fields.put("butyY", butyY);
        fields.put("butyColor", butyColor);
        fields.put("telegramToken", telegramTokenField);
        fields.put("telegramChatId", telegramChatIdField);

        UiUtils.addRow(panel,  1, "Minimap X (lewy-gorny):", mapX);
        UiUtils.addRow(panel,  1, "Minimap Y (lewy-gorny):", mapY);
        UiUtils.addRow(panel,  2, "Minimap szerokosc:", mapW);
        UiUtils.addRow(panel,  2, "Minimap wysokosc:", mapH);
        UiUtils.addRow(panel,  3, "Tolerancja koloru (+/-):", tolerance);
        UiUtils.addRow(panel,  4, "Prog dotarcia (px):", arrive);
        UiUtils.addRow(panel,  5, "Punkt 1 kolor (R,G,B):", point1Color);
        UiUtils.addRow(panel,  6, "Punkt 2 kolor (R,G,B):", point2Color);
        UiUtils.addRow(panel,  7, "Punkt 3 kolor (R,G,B):", point3Color);
        UiUtils.addRow(panel,  8, "TARGET_X (atak):", targetX);
        UiUtils.addRow(panel,  8, "TARGET_Y (atak):", targetY);
        UiUtils.addRow(panel,  8, "TARGET_COLOR (R,G,B):", targetColor);
        UiUtils.addRow(panel,  9, "ROBAK_X:", robakX);
        UiUtils.addRow(panel,  9, "ROBAK_Y:", robakY);
        UiUtils.addRow(panel,  9, "ROBAK_COLOR (R,G,B):", robakColor);
        UiUtils.addRow(panel, 10, "Loot message X:", lootX);
        UiUtils.addRow(panel, 10, "Loot message Y:", lootY);
        UiUtils.addRow(panel, 10, "Loot color (R,G,B):", lootColor);
        UiUtils.addRow(panel, 11, "Loot tile 1 X:", tile1X);
        UiUtils.addRow(panel, 11, "Loot tile 1 Y:", tile1Y);
        UiUtils.addRow(panel, 11, "Loot tile 2 X:", tile2X);
        UiUtils.addRow(panel, 11, "Loot tile 2 Y:", tile2Y);
        UiUtils.addRow(panel, 11, "Loot tile 3 X:", tile3X);
        UiUtils.addRow(panel, 11, "Loot tile 3 Y:", tile3Y);
        UiUtils.addRow(panel, 11, "Loot tile 4 X:", tile4X);
        UiUtils.addRow(panel, 11, "Loot tile 4 Y:", tile4Y);
        UiUtils.addRow(panel, 11, "Loot tile 5 X:", tile5X);
        UiUtils.addRow(panel, 11, "Loot tile 5 Y:", tile5Y);
        UiUtils.addRow(panel, 11, "Loot tile 6 X:", tile6X);
        UiUtils.addRow(panel, 11, "Loot tile 6 Y:", tile6Y);
        UiUtils.addRow(panel, 11, "Loot tile 7 X:", tile7X);
        UiUtils.addRow(panel, 11, "Loot tile 7 Y:", tile7Y);
        UiUtils.addRow(panel, 11, "Loot tile 8 X:", tile8X);
        UiUtils.addRow(panel, 11, "Loot tile 8 Y:", tile8Y);
        UiUtils.addRow(panel, 11, "Loot tile 9 X:", tile9X);
        UiUtils.addRow(panel, 11, "Loot tile 9 Y:", tile9Y);
        UiUtils.addRow(panel, 12, "HEAL_X:", healX);
        UiUtils.addRow(panel, 12, "HEAL_Y:", healY);
        UiUtils.addRow(panel, 12, "HEAL_COLOR (R,G,B):", healColor);
        UiUtils.addRow(panel, 13, "BUTY X:", butyX);
        UiUtils.addRow(panel, 13, "BUTY Y:", butyY);
        UiUtils.addRow(panel, 13, "BUTY COLOR (R,G,B):", butyColor);
        UiUtils.addRow(panel, 14, "Telegram token:", telegramTokenField);
        UiUtils.addRow(panel, 15, "Telegram chat_id:", telegramChatIdField);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
        JButton startButton = new JButton("START BEAR");
        JButton stopButton = new JButton("STOP BEAR");
        JButton closeButton = new JButton("CLOSE");
        stopButton.setBackground(Color.RED);

        final Thread[] watcherRef = new Thread[1];

        startButton.addActionListener(e -> {
            BearConfig config;
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

                config = BearConfig.builder()
                        .minimap(UiUtils.parseInt(mapX), UiUtils.parseInt(mapY),
                                UiUtils.parseInt(mapW), UiUtils.parseInt(mapH))
                        .colorTolerance(UiUtils.parseInt(tolerance))
                        .arriveThreshold(UiUtils.parseInt(arrive))
                        .points(UiUtils.parseColor(point1Color.getText()),
                                UiUtils.parseColor(point2Color.getText()),
                                UiUtils.parseColor(point3Color.getText()))
                        .target(UiUtils.parseInt(targetX), UiUtils.parseInt(targetY),
                                UiUtils.parseColor(targetColor.getText()))
                        .robak(UiUtils.parseInt(robakX), UiUtils.parseInt(robakY),
                                UiUtils.parseColor(robakColor.getText()))
                        .loot(UiUtils.parseInt(lootX), UiUtils.parseInt(lootY),
                                UiUtils.parseColor(lootColor.getText()))
                        .lootTiles(lootTiles)
                        .heal(UiUtils.parseInt(healX), UiUtils.parseInt(healY),
                                UiUtils.parseColor(healColor.getText()))
                        .telegram(telegramTokenField.getText().trim(), telegramChatIdField.getText().trim())
                        .build();
            } catch (NumberFormatException ex) {
                System.out.println("Please enter valid BEAR numbers.");
                return;
            }

            controller.start(config);
            watcherRef[0] = startColorWatcher(butyX, butyY, butyColor, 5000L);
            startButton.setBackground(Color.GREEN);
            stopButton.setBackground(null);
        });

        stopButton.addActionListener(e -> {
            controller.stop();
            if (watcherRef[0] != null) watcherRef[0].interrupt();
            startButton.setBackground(null);
            stopButton.setBackground(Color.RED);
        });
        closeButton.addActionListener(e -> {
            controller.stop();
            if (watcherRef[0] != null) watcherRef[0].interrupt();
            frame.dispose();
        });
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(closeButton);

        // Preset bar: dropdown of saved presets + save/delete.
        BearPresetStore store = new BearPresetStore();
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
            Map<String, String> values = store.load(sel.toString());
            for (Map.Entry<String, String> entry : values.entrySet()) {
                JTextField field = fields.get(entry.getKey());
                if (field != null) field.setText(entry.getValue());
            }
        });

        saveButton.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(frame, "Nazwa ustawień:", "Zapisz preset",
                    JOptionPane.PLAIN_MESSAGE);
            if (name == null || name.trim().isEmpty()) return;
            name = name.trim();
            Map<String, String> values = new LinkedHashMap<>();
            for (Map.Entry<String, JTextField> entry : fields.entrySet()) {
                values.put(entry.getKey(), entry.getValue().getText());
            }
            try {
                store.save(name, values);
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
        frame.add(new JScrollPane(panel), BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private static Thread startColorWatcher(JTextField butyX, JTextField butyY, JTextField butyColor, long intervalMs) {
        Thread watcher = new Thread(() -> {
            try {
                Robot robot = new Robot();
                while (true) {
                    final String[] xs = new String[1];
                    final String[] ys = new String[1];
                    final String[] cs = new String[1];
                    try {
                        SwingUtilities.invokeAndWait(() -> {
                            xs[0] = butyX.getText().trim();
                            ys[0] = butyY.getText().trim();
                            cs[0] = butyColor.getText().trim();
                        });
                    } catch (InvocationTargetException | InterruptedException e) {
                        xs[0] = "1810";
                        ys[0] = "1391";
                        cs[0] = "58,61,63";
                    }
                    int x;
                    int y;
                    Color target;
                    try {
                        x = Integer.parseInt(xs[0]);
                        y = Integer.parseInt(ys[0]);
                        target = UiUtils.parseColor(cs[0]);
                    } catch (NumberFormatException nfe) {
                        x = 1810;
                        y = 1391;
                        target = new Color(58, 61, 63);
                    }
                    Color c = robot.getPixelColor(x, y);
                    System.out.println("[ColorWatcher] Checking (" + x + "," + y + ") -> " + c);
                    if (c.equals(target)) {
                        System.out.println("[ColorWatcher] Detected target color at (" + x + "," + y + "), exiting.");
                        System.exit(0);
                    }
                    Thread.sleep(intervalMs);
                }
            } catch (AWTException ex) {
                ex.printStackTrace();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }, "ColorWatcher");
        watcher.setDaemon(true);
        watcher.start();
        return watcher;
    }
}
