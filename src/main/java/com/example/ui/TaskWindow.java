package com.example.ui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.example.bot.task.TaskController;
import com.example.config.AppSettings;
import com.example.config.DefaultSettings;
import com.example.config.PatrolStep;
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

        // TASK2 keeps its own settings/presets under a "t2." key prefix, so it no
        // longer overwrites plain TASK. Field defaults still read the same keys,
        // so TASK2 starts pre-filled with the same values as TASK.
        String prefix = withGenerator ? "t2." : "";
        java.util.function.Function<String, String> def = k -> DefaultSettings.get(prefix + k);

        JTextField mapX = UiUtils.num(def.apply("mapX"));
        JTextField mapY = UiUtils.num(def.apply("mapY"));
        JTextField mapW = UiUtils.num(def.apply("mapW"));
        JTextField mapH = UiUtils.num(def.apply("mapH"));
        JTextField tolerance = UiUtils.num(def.apply("tolerance"));
        JTextField arrive = UiUtils.num(def.apply("arrive"));
        JTextField point1Color = UiUtils.rgb(def.apply("point1Color"));    // niebieski (prawo)
        JTextField point2Color = UiUtils.rgb(def.apply("point2Color"));    // czerwony (lewo od krzyzyka)
        JTextField point3Color = UiUtils.rgb(def.apply("point3Color"));    // zielony (dol)
        JTextField targetX = UiUtils.num(def.apply("targetX"));
        JTextField targetY = UiUtils.num(def.apply("targetY"));
        JTextField targetColor = UiUtils.rgb(def.apply("targetColor"));
        JTextField robakX = UiUtils.num(def.apply("robakX"));
        JTextField robakY = UiUtils.num(def.apply("robakY"));
        JTextField robakColor = UiUtils.rgb(def.apply("robakColor"));
        JTextField lootX = UiUtils.num(def.apply("lootX"));
        JTextField lootY = UiUtils.num(def.apply("lootY"));
        JTextField lootColor = UiUtils.rgb(def.apply("lootColor"));
        JTextField tile1X = UiUtils.num(def.apply("tile1X"));
        JTextField tile1Y = UiUtils.num(def.apply("tile1Y"));
        JTextField tile2X = UiUtils.num(def.apply("tile2X"));
        JTextField tile2Y = UiUtils.num(def.apply("tile2Y"));
        JTextField tile3X = UiUtils.num(def.apply("tile3X"));
        JTextField tile3Y = UiUtils.num(def.apply("tile3Y"));
        JTextField tile4X = UiUtils.num(def.apply("tile4X"));
        JTextField tile4Y = UiUtils.num(def.apply("tile4Y"));
        JTextField tile5X = UiUtils.num(def.apply("tile5X"));
        JTextField tile5Y = UiUtils.num(def.apply("tile5Y"));
        JTextField tile6X = UiUtils.num(def.apply("tile6X"));
        JTextField tile6Y = UiUtils.num(def.apply("tile6Y"));
        JTextField tile7X = UiUtils.num(def.apply("tile7X"));
        JTextField tile7Y = UiUtils.num(def.apply("tile7Y"));
        JTextField tile8X = UiUtils.num(def.apply("tile8X"));
        JTextField tile8Y = UiUtils.num(def.apply("tile8Y"));
        JTextField tile9X = UiUtils.num(def.apply("tile9X"));
        JTextField tile9Y = UiUtils.num(def.apply("tile9Y"));
        JTextField healX = UiUtils.num(def.apply("healX"));
        JTextField healY = UiUtils.num(def.apply("healY"));
        JTextField healColor = UiUtils.rgb(def.apply("healColor"));
        JTextField ebeX = UiUtils.num(def.apply("ebeX"));
        JTextField ebeY = UiUtils.num(def.apply("ebeY"));
        JTextField ebeColor = UiUtils.rgb(def.apply("ebeColor"));

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
            settings.bind(prefix + e.getKey(), e.getValue());
        }
        List<String> taskKeys = new ArrayList<>();
        for (String k : fields.keySet()) {
            taskKeys.add(prefix + k);
        }

        // Grouped, category-titled layout: narrow numeric coord fields and wider
        // R,G,B color fields share rows so each category reads at a glance.
        JPanel panel = UiUtils.verticalBox();

        JPanel minimap = UiUtils.category("1-4.");
        minimap.add(UiUtils.row("Pozycja X/Y (lewy-gorny):", mapX, mapY));
        minimap.add(UiUtils.row("Rozmiar szer/wys:", mapW, mapH));
        minimap.add(UiUtils.row("+/-", tolerance,
                "(px):", arrive));
        panel.add(minimap);

        // Plain TASK uses the three fixed marks (ping-pong). TASK2 hides them —
        // its loop is built in the generator (panel 14) instead.
        if (!withGenerator) {
            JPanel points = UiUtils.category("5-7.");
            points.add(UiUtils.row("1:", point1Color,
                    "2:", point2Color, "3:", point3Color));
            panel.add(points);
        }

        // Telegram-on-attack: fires a message on every SPACE swing. Lives here
        // next to the attack pixel (panel 8), available in both TASK and TASK2.
        JCheckBox telegramAttackCheck = new JCheckBox("telegram msg", false);

        JPanel target = UiUtils.category("8");
        target.add(UiUtils.row("X/Y:", targetX, targetY, "color:", targetColor, telegramAttackCheck));
        panel.add(target);

        JPanel robak = UiUtils.category("9");
        robak.add(UiUtils.row("X/Y:", robakX, robakY, "color:", robakColor));
        panel.add(robak);

        // Loot checkbox: toggles ONLY the Telegram ping fired when loot is
        // grabbed. Loot collection itself runs regardless.
        JCheckBox lootCheck = new JCheckBox("telegram msg", true);

        JPanel loot = UiUtils.category("10. Loot");
        loot.add(UiUtils.row("X/Y:", lootX, lootY, "color:", lootColor, lootCheck));
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

        // Loop generator (TASK2 only): the user builds an ordered list of steps,
        // each a color + type (BIEG+ATAK = walk then attack, BIEG = walk only).
        // The list cycles linearly 1..N..1, so a ping-pong is just colors added
        // in ping-pong order. Forward ref so the "URUCHOM" button can start.
        final Runnable[] startRef = new Runnable[1];
        List<PatrolStep> loopSteps = new ArrayList<>();

        if (withGenerator) {
            JTextField loopColor = UiUtils.rgb("");
            JPanel loopList = UiUtils.verticalBox();

            Runnable refreshLoop = () -> {
                loopList.removeAll();
                if (loopSteps.isEmpty()) {
                    loopList.add(UiUtils.row("(pusto - dodaj kroki)"));
                } else {
                    for (int i = 0; i < loopSteps.size(); i++) {
                        PatrolStep s = loopSteps.get(i);
                        JLabel swatch = new JLabel();
                        swatch.setOpaque(true);
                        swatch.setBackground(s.color());
                        swatch.setPreferredSize(new Dimension(18, 14));
                        String typ = switch (s.type()) {
                            case RUN -> "BIEG";
                            case RUN_ATTACK -> "BIEG+ATAK";
                            case ROPE_DOWN -> "ROPE DOWN";
                            case LADDER_UP -> "LADDER UP";
                            case ROPE_UP -> "ROPE UP";
                        };
                        String rgb = s.color().getRed() + "," + s.color().getGreen() + "," + s.color().getBlue();
                        String info = s.isWaypoint() ? "(zolty marker)" : "(" + rgb + ")";
                        loopList.add(UiUtils.row((i + 1) + ".", swatch, typ + "  " + info));
                    }
                }
                loopList.revalidate();
                loopList.repaint();
            };

            JButton addAtk = new JButton("+ BIEG+ATAK");
            JButton addRun = new JButton("+ BIEG");
            JButton addRopeDown = new JButton("+ ROPE DOWN");
            JButton addLadderUp = new JButton("+ LADDER UP");
            JButton addRopeUp = new JButton("+ ROPE UP");
            JButton delLast = new JButton("Usun ostatni");
            JButton clearAll = new JButton("Wyczysc");
            JButton runLoop = new JButton("URUCHOM PETLE");
            runLoop.setBackground(Color.GREEN);

            // Color steps read the R,G,B field; waypoint steps use the fixed
            // yellow marker, so they ignore the field.
            addAtk.addActionListener(e -> {
                try {
                    loopSteps.add(PatrolStep.attack(UiUtils.parseColor(loopColor.getText())));
                    refreshLoop.run();
                } catch (NumberFormatException ex) {
                    System.out.println("Zly kolor - podaj R,G,B");
                }
            });
            addRun.addActionListener(e -> {
                try {
                    loopSteps.add(PatrolStep.run(UiUtils.parseColor(loopColor.getText())));
                    refreshLoop.run();
                } catch (NumberFormatException ex) {
                    System.out.println("Zly kolor - podaj R,G,B");
                }
            });
            addRopeDown.addActionListener(e -> {
                loopSteps.add(PatrolStep.ropeDown());
                refreshLoop.run();
            });
            addLadderUp.addActionListener(e -> {
                loopSteps.add(PatrolStep.ladderUp());
                refreshLoop.run();
            });
            addRopeUp.addActionListener(e -> {
                loopSteps.add(PatrolStep.ropeUp());
                refreshLoop.run();
            });
            delLast.addActionListener(e -> {
                if (!loopSteps.isEmpty()) {
                    loopSteps.remove(loopSteps.size() - 1);
                    refreshLoop.run();
                }
            });
            clearAll.addActionListener(e -> {
                loopSteps.clear();
                refreshLoop.run();
            });
            runLoop.addActionListener(e -> startRef[0].run());

            // Quick-pick palette: swatch + R,G,B; clicking fills the color field.
            String[] palette = { "1,1,240", "227,0,15", "68,206,87", "247,148,28" };
            List<Object> palParts = new ArrayList<>();
            palParts.add("szablony:");
            for (String rgb : palette) {
                Color c;
                try {
                    c = UiUtils.parseColor(rgb);
                } catch (NumberFormatException ex) {
                    continue;
                }
                JLabel sw = new JLabel();
                sw.setOpaque(true);
                sw.setBackground(c);
                sw.setPreferredSize(new Dimension(16, 14));
                JButton pick = new JButton(rgb);
                pick.setMargin(new java.awt.Insets(1, 4, 1, 4));
                pick.addActionListener(e -> loopColor.setText(rgb));
                palParts.add(sw);
                palParts.add(pick);
            }

            JPanel generator = UiUtils.category("14. Generator petli");
            generator.add(UiUtils.row("kolor R,G,B:", loopColor, addAtk, addRun));
            generator.add(UiUtils.row(palParts.toArray()));
            generator.add(UiUtils.row("zolty marker:", addRopeDown, addLadderUp, addRopeUp));
            generator.add(UiUtils.row(delLast, clearAll, runLoop));
            generator.add(loopList);
            panel.add(generator);
            refreshLoop.run();
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

        Runnable startAction = () -> {
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
                TaskConfig.Builder b = TaskConfig.builder()
                        .minimap(UiUtils.parseInt(mapX), UiUtils.parseInt(mapY),
                                UiUtils.parseInt(mapW), UiUtils.parseInt(mapH))
                        .colorTolerance(UiUtils.parseInt(tolerance))
                        .arriveThreshold(UiUtils.parseInt(arrive))
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
                        .lootEnabled(true)
                        .telegramOnLoot(lootCheck.isSelected())
                        .healEnabled(true)
                        .telegramOnAttack(telegramAttackCheck.isSelected());

                if (withGenerator) {
                    // TASK2: drive the user-built loop. Empty list = nothing to do.
                    if (loopSteps.isEmpty()) {
                        System.out.println("Pusta petla - dodaj kroki w generatorze (panel 14).");
                        return;
                    }
                    b.steps(new ArrayList<>(loopSteps));
                } else {
                    // Plain TASK: classic ping-pong derived from the three marks.
                    b.points(UiUtils.parseColor(point1Color.getText()),
                            UiUtils.parseColor(point2Color.getText()),
                            UiUtils.parseColor(point3Color.getText()));
                }
                config = b.build();
            } catch (NumberFormatException ex) {
                System.out.println("Please enter valid TASK numbers.");
                return;
            }

            controller.start(config);
            watcherRef[0] = startColorWatcher(ebeX, ebeY, ebeColor, 5000L, stopAction);
            startButton.setBackground(Color.GREEN);
            stopButton.setBackground(null);
        };
        startRef[0] = startAction;

        startButton.addActionListener(e -> startAction.run());
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
