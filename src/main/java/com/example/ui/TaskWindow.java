package com.example.ui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Robot;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.example.bot.task.TaskController;
import com.example.config.AppSettings;
import com.example.config.DefaultSettings;
import com.example.config.TaskConfig;

/**
 * The "Task Control" window: minimap rectangle + the three uniquely-colored
 * patrol marks, plus START/STOP/CLOSE.
 *
 * Fields bind into the shared {@link AppSettings} so the single preset bar in
 * Bot Control persists them. Telegram token / chat_id live only in Bot Control;
 * this window reads them from the shared registry.
 *
 * Defaults come from analyzing minimap.png (panel inner area in screen coords;
 * player cross anchored at 1806,1177). The patrol order is 1 -> 2 -> 3 -> 2 -> ...
 */
public final class TaskWindow {

    private TaskWindow() {
    }

    public static void open(AppSettings settings) {
        open(settings, false);
    }

    /**
     * @param withGenerator when true this is the TASK2 panel: shows the
     *        action-generator (loot/heal/telegram checkboxes + live machine
     *        preview) so the user composes the machine. When false it is the
     *        plain TASK panel with the fixed loot+heal behavior.
     */
    public static void open(AppSettings settings, boolean withGenerator) {
        TaskController controller = new TaskController();

        JFrame frame = new JFrame(withGenerator ? "Task 2 - Generator" : "Task Control");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(520, 720);
        frame.setLayout(new BorderLayout());

        JTextField mapX = UiUtils.num(DefaultSettings.get("mapX"));
        JTextField mapY = UiUtils.num(DefaultSettings.get("mapY"));
        JTextField mapW = UiUtils.num(DefaultSettings.get("mapW"));
        JTextField mapH = UiUtils.num(DefaultSettings.get("mapH"));
        JTextField tolerance = UiUtils.num(DefaultSettings.get("tolerance"));
        JTextField arrive = UiUtils.num(DefaultSettings.get("arrive"));
        JTextField point1Color = UiUtils.rgb(DefaultSettings.get("point1Color"));    // niebieski (prawo)
        JTextField point2Color = UiUtils.rgb(DefaultSettings.get("point2Color"));    // czerwony (lewo od krzyzyka)
        JTextField point3Color = UiUtils.rgb(DefaultSettings.get("point3Color"));    // zielony (dol)
        JTextField targetX = UiUtils.num(DefaultSettings.get("targetX"));
        JTextField targetY = UiUtils.num(DefaultSettings.get("targetY"));
        JTextField targetColor = UiUtils.rgb(DefaultSettings.get("targetColor"));
        JTextField robakX = UiUtils.num(DefaultSettings.get("robakX"));
        JTextField robakY = UiUtils.num(DefaultSettings.get("robakY"));
        JTextField robakColor = UiUtils.rgb(DefaultSettings.get("robakColor"));
        JTextField lootX = UiUtils.num(DefaultSettings.get("lootX"));
        JTextField lootY = UiUtils.num(DefaultSettings.get("lootY"));
        JTextField lootColor = UiUtils.rgb(DefaultSettings.get("lootColor"));
        JTextField tile1X = UiUtils.num(DefaultSettings.get("tile1X"));
        JTextField tile1Y = UiUtils.num(DefaultSettings.get("tile1Y"));
        JTextField tile2X = UiUtils.num(DefaultSettings.get("tile2X"));
        JTextField tile2Y = UiUtils.num(DefaultSettings.get("tile2Y"));
        JTextField tile3X = UiUtils.num(DefaultSettings.get("tile3X"));
        JTextField tile3Y = UiUtils.num(DefaultSettings.get("tile3Y"));
        JTextField tile4X = UiUtils.num(DefaultSettings.get("tile4X"));
        JTextField tile4Y = UiUtils.num(DefaultSettings.get("tile4Y"));
        JTextField tile5X = UiUtils.num(DefaultSettings.get("tile5X"));
        JTextField tile5Y = UiUtils.num(DefaultSettings.get("tile5Y"));
        JTextField tile6X = UiUtils.num(DefaultSettings.get("tile6X"));
        JTextField tile6Y = UiUtils.num(DefaultSettings.get("tile6Y"));
        JTextField tile7X = UiUtils.num(DefaultSettings.get("tile7X"));
        JTextField tile7Y = UiUtils.num(DefaultSettings.get("tile7Y"));
        JTextField tile8X = UiUtils.num(DefaultSettings.get("tile8X"));
        JTextField tile8Y = UiUtils.num(DefaultSettings.get("tile8Y"));
        JTextField tile9X = UiUtils.num(DefaultSettings.get("tile9X"));
        JTextField tile9Y = UiUtils.num(DefaultSettings.get("tile9Y"));
        JTextField healX = UiUtils.num(DefaultSettings.get("healX"));
        JTextField healY = UiUtils.num(DefaultSettings.get("healY"));
        JTextField healColor = UiUtils.rgb(DefaultSettings.get("healColor"));
        JTextField ebeX = UiUtils.num(DefaultSettings.get("ebeX"));
        JTextField ebeY = UiUtils.num(DefaultSettings.get("ebeY"));
        JTextField ebeColor = UiUtils.rgb(DefaultSettings.get("ebeColor"));

        // Stable key -> field registry; bind each into the shared AppSettings.
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
        fields.put("ebeX", ebeX);
        fields.put("ebeY", ebeY);
        fields.put("ebeColor", ebeColor);

        for (Map.Entry<String, JTextField> e : fields.entrySet()) {
            settings.bind(e.getKey(), e.getValue());
        }
        List<String> taskKeys = new ArrayList<>(fields.keySet());

        // Grouped, category-titled layout: narrow numeric coord fields and wider
        // R,G,B color fields share rows so each category reads at a glance.
        JPanel panel = UiUtils.verticalBox();

        JPanel minimap = UiUtils.category("1-4.");
        minimap.add(UiUtils.row("Pozycja X/Y (lewy-gorny):", mapX, mapY));
        minimap.add(UiUtils.row("Rozmiar szer/wys:", mapW, mapH));
        minimap.add(UiUtils.row("+/-", tolerance,
                "(px):", arrive));
        panel.add(minimap);

        JPanel points = UiUtils.category("5-7.");
        points.add(UiUtils.row("1:", point1Color,
                "2:", point2Color, "3:", point3Color));
        panel.add(points);

        JPanel target = UiUtils.category("8");
        target.add(UiUtils.row("X/Y:", targetX, targetY, "color:", targetColor));
        panel.add(target);

        JPanel robak = UiUtils.category("9");
        robak.add(UiUtils.row("X/Y:", robakX, robakY, "color:", robakColor));
        panel.add(robak);

        JPanel loot = UiUtils.category("10. Loot");
        loot.add(UiUtils.row("X/Y:", lootX, lootY, "color:", lootColor));
        panel.add(loot);

        JPanel tiles = UiUtils.category("11. Loot 1-9 (X/Y)");
        tiles.add(UiUtils.row("1:", tile1X, tile1Y, "2:", tile2X, tile2Y, "3:", tile3X, tile3Y));
        tiles.add(UiUtils.row("4:", tile4X, tile4Y, "5:", tile5X, tile5Y, "6:", tile6X, tile6Y));
        tiles.add(UiUtils.row("7:", tile7X, tile7Y, "8:", tile8X, tile8Y, "9:", tile9X, tile9Y));
        panel.add(tiles);

        JPanel heal = UiUtils.category("12. heal");
        heal.add(UiUtils.row("X/Y:", healX, healY, "color:", healColor));
        panel.add(heal);

        JPanel ebe = UiUtils.category("13.");
        ebe.add(UiUtils.row("X/Y:", ebeX, ebeY, "color:", ebeColor));
        panel.add(ebe);

        // Generator panel: toggle the optional machine actions and watch the
        // live preview redraw so the user sees exactly what machine is built.
        JCheckBox lootCheck = new JCheckBox("Loot (zbieraj 1-9 po zabiciu)", true);
        JCheckBox healCheck = new JCheckBox("Heal (osobny watek ~4.5s)", true);
        JCheckBox telegramAttackCheck = new JCheckBox("Telegram przy ataku (kazda SPACJA)", false);
        JTextArea preview = new JTextArea(machinePreview(true, true, false));
        preview.setEditable(false);
        preview.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
        preview.setBackground(new Color(245, 245, 245));

        Runnable refreshPreview = () -> preview.setText(machinePreview(
                lootCheck.isSelected(), healCheck.isSelected(), telegramAttackCheck.isSelected()));
        lootCheck.addActionListener(e -> refreshPreview.run());
        healCheck.addActionListener(e -> refreshPreview.run());
        telegramAttackCheck.addActionListener(e -> refreshPreview.run());

        // Only the TASK2 panel exposes the generator; plain TASK keeps the fixed
        // loot+heal behavior (the checkbox defaults above already encode it).
        if (withGenerator) {
            JPanel generator = UiUtils.category("14. Akcje maszyny (generator)");
            generator.add(lootCheck);
            generator.add(healCheck);
            generator.add(telegramAttackCheck);
            generator.add(preview);
            panel.add(generator);
        }

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
        JButton startButton = new JButton("START TASK");
        JButton stopButton = new JButton("STOP TASK");
        JButton closeButton = new JButton("CLOSE");
        stopButton.setBackground(Color.RED);

        final Thread[] watcherRef = new Thread[1];

        // Shared STOP action: used by the STOP button and by the color watcher,
        // so detecting the ebe color does the same thing as pressing STOP TASK.
        Runnable stopAction = () -> {
            controller.stop();
            if (watcherRef[0] != null) watcherRef[0].interrupt();
            startButton.setBackground(null);
            stopButton.setBackground(Color.RED);
        };

        startButton.addActionListener(e -> {
            TaskConfig config;
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

                String token = settings.get("telegramToken");
                String chatId = settings.get("telegramChatId");
                config = TaskConfig.builder()
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
                        .telegram(token == null ? "" : token.trim(),
                                chatId == null ? "" : chatId.trim())
                        .lootEnabled(lootCheck.isSelected())
                        .healEnabled(healCheck.isSelected())
                        .telegramOnAttack(telegramAttackCheck.isSelected())
                        .build();
            } catch (NumberFormatException ex) {
                System.out.println("Please enter valid TASK numbers.");
                return;
            }

            controller.start(config);
            watcherRef[0] = startColorWatcher(ebeX, ebeY, ebeColor, 5000L, stopAction);
            startButton.setBackground(Color.GREEN);
            stopButton.setBackground(null);
        });

        stopButton.addActionListener(e -> stopAction.run());
        closeButton.addActionListener(e -> {
            controller.stop();
            if (watcherRef[0] != null) watcherRef[0].interrupt();
            frame.dispose();
        });
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(closeButton);

        // On close, hand the fields' last values back to the shared cache so the
        // Bot Control preset still sees them after this window is gone.
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                settings.unbind(taskKeys);
            }
        });

        // NORTH wrapper keeps categories at their natural height (no vertical stretch).
        JPanel northHold = new JPanel(new BorderLayout());
        northHold.add(panel, BorderLayout.NORTH);
        frame.add(new JScrollPane(northHold), BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    /** Renders the assembled state machine as text for the generator preview. */
    private static String machinePreview(boolean loot, boolean heal, boolean telegram) {
        StringBuilder sb = new StringBuilder();
        sb.append("WALK   szukaj punktu -> klik -> auto-chodzenie\n");
        sb.append("   |  dotarl (dist <= arrive)\n");
        sb.append("   v\n");
        sb.append("ATTACK bialy? -> SPACJA");
        if (telegram) {
            sb.append(" + Telegram");
        }
        sb.append("\n");
        if (loot) {
            sb.append("       brak potwora? -> zbierz LOOT 1-9 -> dalej\n");
        } else {
            sb.append("       brak potwora? -> (loot OFF) -> dalej\n");
        }
        sb.append("   |\n");
        sb.append("   v\n");
        sb.append("WALK   nastepny punkt (ping-pong 1,2,3,2)\n");
        sb.append("\n");
        sb.append("Rownolegle: ").append(heal ? "HEAL co ~4.5s" : "(heal OFF)");
        return sb.toString();
    }

    private static Thread startColorWatcher(JTextField ebeX, JTextField ebeY, JTextField ebeColor, long intervalMs,
            Runnable onDetect) {
        Thread watcher = new Thread(() -> {
            try {
                Robot robot = new Robot();
                while (true) {
                    final String[] xs = new String[1];
                    final String[] ys = new String[1];
                    final String[] cs = new String[1];
                    try {
                        SwingUtilities.invokeAndWait(() -> {
                            xs[0] = ebeX.getText().trim();
                            ys[0] = ebeY.getText().trim();
                            cs[0] = ebeColor.getText().trim();
                        });
                    } catch (InvocationTargetException | InterruptedException e) {
                        xs[0] = DefaultSettings.get("ebeX");
                        ys[0] = DefaultSettings.get("ebeY");
                        cs[0] = DefaultSettings.get("ebeColor");
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
                        System.out.println("[ColorWatcher] Detected target color at (" + x + "," + y + "), stopping task.");
                        SwingUtilities.invokeLater(onDetect);
                        return;
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
