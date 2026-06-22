package com.example.ui;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Robot;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.example.bot.task.TaskController;
import com.example.config.AppSettings;
import com.example.config.DefaultSettings;
import com.example.config.PatrolStep;
import com.example.config.TaskConfig;

/**
 * The Task control panel, built inline into {@link MainWindow}: minimap
 * rectangle, the attack/loot/heal pixels, the loop generator, and START/STOP.
 *
 * Fields bind into the shared {@link AppSettings} so the single preset bar
 * persists them. Telegram token / chat_id live in the main page; this panel
 * reads them from the shared registry. Because the panel lives for the whole
 * app session, fields stay bound (no unbind on close).
 */
public final class TaskPanel {

    private TaskPanel() {
    }

    public static JComponent build(AppSettings settings) {
        TaskController controller = new TaskController();

        // Settings/presets live under a "t2." key prefix (kept for backward
        // compatibility with presets saved by the old TASK2 panel).
        String prefix = "t2.";
        java.util.function.Function<String, String> def = k -> DefaultSettings.get(prefix + k);

        JTextField mapX = UiUtils.num(def.apply("mapX"));
        JTextField mapY = UiUtils.num(def.apply("mapY"));
        JTextField mapW = UiUtils.num(def.apply("mapW"));
        JTextField mapH = UiUtils.num(def.apply("mapH"));
        JTextField tolerance = UiUtils.num(def.apply("tolerance"));
        JTextField arrive = UiUtils.num(def.apply("arrive"));
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
        // Point 14: optional LADDER_UP dialog-confirm coords (LPM). Empty = unset.
        // The Ctrl+LPM use-click reuses loot tile 5.
        JTextField ladderUpX = UiUtils.num(def.apply("ladderUpX"));
        JTextField ladderUpY = UiUtils.num(def.apply("ladderUpY"));

        // Stable key -> field registry; bind each into the shared AppSettings.
        Map<String, JTextField> fields = new LinkedHashMap<>();
        fields.put("mapX", mapX);
        fields.put("mapY", mapY);
        fields.put("mapW", mapW);
        fields.put("mapH", mapH);
        fields.put("tolerance", tolerance);
        fields.put("arrive", arrive);
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
        fields.put("ladderUpX", ladderUpX);
        fields.put("ladderUpY", ladderUpY);

        for (Map.Entry<String, JTextField> e : fields.entrySet()) {
            settings.bind(prefix + e.getKey(), e.getValue());
        }

        // Grouped, category-titled layout: narrow numeric coord fields and wider
        // R,G,B color fields share rows so each category reads at a glance.
        // Categories 1-13 live in one collapsible box (default closed).
        JPanel panel = UiUtils.verticalBox();
        JPanel coords = UiUtils.verticalBox();

        JPanel minimap = UiUtils.category("1-4.");
        minimap.add(UiUtils.row("Pozycja X/Y (lewy-gorny):", mapX, mapY));
        minimap.add(UiUtils.row("Rozmiar szer/wys:", mapW, mapH));
        minimap.add(UiUtils.row("+/-", tolerance,
                "(px):", arrive));
        coords.add(minimap);

        // Telegram-on-attack: fires a message on every SPACE swing. Lives here
        // next to the attack pixel (panel 8).
        JCheckBox telegramAttackCheck = new JCheckBox("telegram notification", false);

        JPanel target = UiUtils.category("8");
        target.add(UiUtils.row("X/Y:", targetX, targetY, "color:", targetColor, telegramAttackCheck));
        coords.add(target);

        JPanel robak = UiUtils.category("9");
        robak.add(UiUtils.row("X/Y:", robakX, robakY, "color:", robakColor));
        coords.add(robak);

        // Loot checkbox: toggles ONLY the Telegram ping fired when loot is
        // grabbed. Loot collection itself runs regardless.
        JCheckBox lootCheck = new JCheckBox("telegram notification", true);

        JPanel loot = UiUtils.category("10. Loot");
        loot.add(UiUtils.row("X/Y:", lootX, lootY, "color:", lootColor, lootCheck));
        coords.add(loot);

        JPanel tiles = UiUtils.category("11. Loot 1-9 (X/Y)");
        tiles.add(UiUtils.row("1:", tile1X, tile1Y, "2:", tile2X, tile2Y, "3:", tile3X, tile3Y));
        tiles.add(UiUtils.row("4:", tile4X, tile4Y, "5:", tile5X, tile5Y, "6:", tile6X, tile6Y));
        tiles.add(UiUtils.row("7:", tile7X, tile7Y, "8:", tile8X, tile8Y, "9:", tile9X, tile9Y));
        coords.add(tiles);

        JPanel heal = UiUtils.category("12. heal");
        heal.add(UiUtils.row("X/Y:", healX, healY, "color:", healColor));
        coords.add(heal);

        JPanel ebe = UiUtils.category("13.");
        ebe.add(UiUtils.row("X/Y:", ebeX, ebeY, "color:", ebeColor));
        coords.add(ebe);

        // Point 14: optional LADDER_UP confirm coords (LPM). Only used by the
        // LADDER_UP step; the Ctrl+LPM use-click reuses loot tile 5. Empty = skip.
        JPanel ladderUp = UiUtils.category("14. Ladder up potw. (LPM, opcjonalne)");
        ladderUp.add(UiUtils.row("X/Y:", ladderUpX, ladderUpY));
        coords.add(ladderUp);

        // Loop generator: the user builds an ordered list of steps, each a color
        // + type (RUN ATTACK = walk then attack, RUN = walk only). The list cycles
        // linearly 1..N..1, so a ping-pong is just colors added in ping-pong order.
        List<PatrolStep> loopSteps = new ArrayList<>();
        // Parallel to loopSteps: a user-typed group label per step ("" = ungrouped).
        // Purely visual — consecutive steps sharing a label render inside one titled
        // box so the route reads as named stages. The bot ignores groups entirely.
        List<String> groupNames = new ArrayList<>();
        // Card the loop starts the first lap at (click a card's body to set it).
        // Reorder/delete keep it pinned to the same action. Default 0 = front.
        final int[] startIdx = { 0 };
        // Live cursor written by the running task; -1 = stopped (no yellow card).
        final AtomicInteger activeStep = new AtomicInteger(-1);

        {
            JTextField loopColor = UiUtils.rgb("");
            JPanel loopList = UiUtils.verticalBox();
            // Holder so the per-card buttons can call refresh from inside the very
            // lambda that defines it (a plain local self-reference is illegal).
            final Runnable[] refresh = new Runnable[1];

            // Append a step (always ungrouped); reorder/group it afterwards.
            java.util.function.Consumer<PatrolStep> addStep = s -> {
                loopSteps.add(s);
                groupNames.add("");
                refresh[0].run();
            };

            refresh[0] = () -> {
                loopList.removeAll();
                // Keep labels aligned with steps (older presets may carry none).
                while (groupNames.size() < loopSteps.size()) groupNames.add("");
                while (groupNames.size() > loopSteps.size()) groupNames.remove(groupNames.size() - 1);
                // Keep the start card in range as steps come and go.
                if (loopSteps.isEmpty()) startIdx[0] = 0;
                else if (startIdx[0] >= loopSteps.size()) startIdx[0] = loopSteps.size() - 1;
                else if (startIdx[0] < 0) startIdx[0] = 0;
                int i = 0;
                while (i < loopSteps.size()) {
                    String g = groupNames.get(i);
                    if (g != null && !g.isBlank()) {
                        // Maximal run of consecutive steps with the same label = one box.
                        JPanel box = UiUtils.category(g);
                        int j = i;
                        while (j < loopSteps.size() && g.equals(groupNames.get(j))) {
                            box.add(stepCard(j, loopSteps, groupNames, refresh, startIdx, activeStep));
                            j++;
                        }
                        loopList.add(box);
                        i = j;
                    } else {
                        loopList.add(stepCard(i, loopSteps, groupNames, refresh, startIdx, activeStep));
                        i++;
                    }
                }
                loopList.revalidate();
                loopList.repaint();
            };

            // Poll the live cursor; rebuild (to move the yellow highlight) only
            // when the active step actually changes — fires every few seconds, not
            // every tick. Runs on the EDT, so refresh is safe here.
            final int[] lastShown = { -1 };
            Timer cursorTimer = new Timer(150, e -> {
                int now = activeStep.get();
                if (now != lastShown[0]) {
                    lastShown[0] = now;
                    refresh[0].run();
                }
            });
            cursorTimer.start();

            // Steps and their group labels serialize together (one preset string),
            // so they can never drift out of alignment. Default (no preset) = empty.
            settings.bindState(prefix + "loopSteps",
                    () -> encodeLoop(loopSteps, groupNames),
                    text -> {
                        decodeLoop(text, loopSteps, groupNames);
                        refresh[0].run();
                    });
            // Remember which card the loop starts at across runs/presets.
            settings.bindState(prefix + "loopStart",
                    () -> String.valueOf(startIdx[0]),
                    text -> {
                        try {
                            startIdx[0] = Integer.parseInt(text.trim());
                        } catch (NumberFormatException ex) {
                            startIdx[0] = 0;
                        }
                        refresh[0].run();
                    });

            JButton addAtk = new JButton("+ RUN ATTACK");
            JButton addAtkOnly = new JButton("+ ATTACK IN PLACE");
            JButton addRun = new JButton("+ RUN");
            JButton addRopeDown = new JButton("+ ROPE DOWN");
            JButton addLadderUp = new JButton("+ LADDER UP");
            JButton addRopeUp = new JButton("+ ROPE UP");
            JButton addStairs = new JButton("+ STAIRS");
            JButton delLast = new JButton("DELETE LAST ONE");
            JButton clearAll = new JButton("CLEAR");

            // Color steps read the R,G,B field; waypoint steps use the fixed
            // yellow marker, so they ignore the field.
            addAtk.addActionListener(e -> {
                try {
                    addStep.accept(PatrolStep.attack(UiUtils.parseColor(loopColor.getText())));
                } catch (NumberFormatException ex) {
                    System.out.println("Bad color - enter R,G,B");
                }
            });
            addRun.addActionListener(e -> {
                try {
                    addStep.accept(PatrolStep.run(UiUtils.parseColor(loopColor.getText())));
                } catch (NumberFormatException ex) {
                    System.out.println("Bad color - enter R,G,B");
                }
            });
            // Attack-in-place: no color needed, just a swing where we stand.
            addAtkOnly.addActionListener(e -> addStep.accept(PatrolStep.attackOnly()));
            addRopeDown.addActionListener(e -> addStep.accept(PatrolStep.ropeDown()));
            addLadderUp.addActionListener(e -> addStep.accept(PatrolStep.ladderUp()));
            addRopeUp.addActionListener(e -> addStep.accept(PatrolStep.ropeUp()));
            addStairs.addActionListener(e -> addStep.accept(PatrolStep.stairs()));
            delLast.addActionListener(e -> {
                if (!loopSteps.isEmpty()) {
                    loopSteps.remove(loopSteps.size() - 1);
                    groupNames.remove(groupNames.size() - 1);
                    refresh[0].run();
                }
            });
            clearAll.addActionListener(e -> {
                loopSteps.clear();
                groupNames.clear();
                refresh[0].run();
            });

            // Quick-pick palette: a clickable swatch per preset; clicking fills the
            // color field. The R,G,B value lives in the tooltip, not on the face.
            String[] palette = { "1,1,240", "227,0,15", "68,206,87", "247,148,28", "192,192,192", "88,35,4" };
            List<Object> palParts = new ArrayList<>();
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
                sw.setPreferredSize(new Dimension(22, 16));
                sw.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                sw.setToolTipText(rgb);
                sw.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                sw.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        loopColor.setText(rgb);
                    }
                });
                palParts.add(sw);
            }

            JPanel generator = UiUtils.verticalBox();
            generator.add(UiUtils.row(loopColor, addAtk, addRun));
            generator.add(UiUtils.row(palParts.toArray()));
            generator.add(UiUtils.row(addAtkOnly));
            generator.add(UiUtils.row(addRopeDown, addLadderUp, addRopeUp, addStairs));
            generator.add(UiUtils.row(delLast, clearAll));
            generator.add(loopList);
            // Collapsible like the others, but open by default.
            panel.add(UiUtils.collapsible("Route generator", generator, true));
            refresh[0].run();
        }

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton startButton = new JButton("START TASK");
        JButton stopButton = new JButton("STOP TASK");
        stopButton.setBackground(Color.RED);

        final Thread[] watcherRef = new Thread[1];

        // Shared STOP action: used by the STOP button and by the color watcher,
        // so detecting the ebe color does the same thing as pressing STOP TASK.
        Runnable stopAction = () -> {
            controller.stop();
            // Clear the live cursor; the polling timer drops the yellow highlight.
            activeStep.set(-1);
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
                        .ladderPoint(UiUtils.parseOptionalPoint(ladderUpX, ladderUpY))
                        .heal(UiUtils.parseInt(healX), UiUtils.parseInt(healY),
                                UiUtils.parseColor(healColor.getText()))
                        .telegram(token == null ? "" : token.trim(),
                                chatId == null ? "" : chatId.trim())
                        .lootEnabled(true)
                        .telegramOnLoot(lootCheck.isSelected())
                        .healEnabled(true)
                        .telegramOnAttack(telegramAttackCheck.isSelected());

                // Drive the user-built loop. Empty list = nothing to do.
                if (loopSteps.isEmpty()) {
                    System.out.println("Empty loop - add steps in the generator (route generator).");
                    return;
                }
                b.steps(new ArrayList<>(loopSteps));
                b.startIndex(startIdx[0]);
                b.progress(activeStep);
                config = b.build();
            } catch (NumberFormatException ex) {
                System.out.println("Please enter valid TASK numbers.");
                return;
            }

            // Show the cursor on the start card right away (covers the countdown).
            activeStep.set(startIdx[0]);
            controller.start(config);
            watcherRef[0] = startColorWatcher(ebeX, ebeY, ebeColor, 5000L, stopAction);
            startButton.setBackground(Color.GREEN);
            stopButton.setBackground(null);
        };

        startButton.addActionListener(e -> startAction.run());
        stopButton.addActionListener(e -> stopAction.run());

        // Global STOP: middle mouse button from any window (the game has focus, not the bot).
        com.example.util.GlobalStopHotkey.install(stopAction);
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        panel.add(buttonPanel);
        // Settings (1-14) sit below START/STOP, collapsed by default.
        panel.add(UiUtils.collapsible("Ustawienia 1-14", coords, false));
        return panel;
    }

    /**
     * One step rendered as a graphical card: index, color swatch, type, a group
     * name field, and ↑/↓/✕ controls. Reorder and group edits mutate {@code steps}
     * and {@code groups} in lock-step, then call {@code refresh[0]} to rebuild.
     */
    private static JPanel stepCard(int idx, List<PatrolStep> steps, List<String> groups, Runnable[] refresh,
                                   int[] startIdx, AtomicInteger activeStep) {
        PatrolStep s = steps.get(idx);

        JLabel swatch = new JLabel();
        swatch.setOpaque(true);
        swatch.setBackground(s.color());
        swatch.setPreferredSize(new Dimension(18, 14));
        swatch.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        String typ = switch (s.type()) {
            case RUN -> "RUN";
            case RUN_ATTACK -> "RUN ATTACK";
            case ATTACK_ONLY -> "ATTACK IN PLACE";
            case ROPE_DOWN -> "ROPE DOWN";
            case LADDER_UP -> "LADDER UP";
            case ROPE_UP -> "ROPE UP";
            case STAIRS -> "STAIRS";
        };
        String rgb = s.color().getRed() + "," + s.color().getGreen() + "," + s.color().getBlue();
        String info = s.type() == PatrolStep.Type.ATTACK_ONLY ? "(in place)"
                : s.isWaypoint() ? "(yellow)" : "(" + rgb + ")";

        JTextField groupField = new JTextField(idx < groups.size() ? groups.get(idx) : "", 8);
        groupField.setToolTipText("Nazwa grupy (puste = brak). Kolejne kroki z tą samą nazwą tworzą jedną wizualną grupę. Nie wpływa na działanie bota.");
        Runnable commit = () -> {
            if (idx >= groups.size()) {
                return;
            }
            String v = sanitizeGroup(groupField.getText());
            if (!v.equals(groups.get(idx))) {
                groups.set(idx, v);
                refresh[0].run();
            }
        };
        groupField.addActionListener(e -> commit.run());
        groupField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                commit.run();
            }
        });

        JButton up = miniBtn("↑");
        up.setEnabled(idx > 0);
        up.addActionListener(e -> {
            Collections.swap(steps, idx, idx - 1);
            Collections.swap(groups, idx, idx - 1);
            // Keep the start highlight on the same action as it moves.
            if (startIdx[0] == idx) startIdx[0] = idx - 1;
            else if (startIdx[0] == idx - 1) startIdx[0] = idx;
            refresh[0].run();
        });
        JButton down = miniBtn("↓");
        down.setEnabled(idx < steps.size() - 1);
        down.addActionListener(e -> {
            Collections.swap(steps, idx, idx + 1);
            Collections.swap(groups, idx, idx + 1);
            if (startIdx[0] == idx) startIdx[0] = idx + 1;
            else if (startIdx[0] == idx + 1) startIdx[0] = idx;
            refresh[0].run();
        });
        JButton del = miniBtn("✕");
        del.addActionListener(e -> {
            steps.remove(idx);
            groups.remove(idx);
            // Removing a card above the start shifts the start down one.
            if (startIdx[0] > idx) startIdx[0]--;
            refresh[0].run();
        });

        JPanel card = UiUtils.row((idx + 1) + ".", swatch, typ + "  " + info, "grupa:", groupField, up, down, del);
        card.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
        // Clicking a card's body picks it as the loop's start; the chosen card
        // paints a soft, see-through green. Buttons/fields keep their own clicks,
        // so only empty card space triggers the pick. The currently-running step
        // wins with a bright yellow over the green start mark.
        if (idx == activeStep.get()) {
            card.setOpaque(true);
            card.setBackground(new Color(255, 246, 143));
        } else if (idx == startIdx[0]) {
            card.setOpaque(true);
            card.setBackground(new Color(198, 239, 206));
        }
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                startIdx[0] = idx;
                refresh[0].run();
            }
        });
        return card;
    }

    private static JButton miniBtn(String text) {
        JButton b = new JButton(text);
        b.setMargin(new Insets(1, 5, 1, 5));
        b.setFocusable(false);
        return b;
    }

    /** Strip the two reserved separators ({@code ;} and {@code @}) from a group
     *  name so it survives serialization without escaping. */
    private static String sanitizeGroup(String raw) {
        return raw == null ? "" : raw.replace(";", "").replace("@", "").trim();
    }

    /** Encode steps + group labels as {@code STEP@@group;STEP;STEP@@group...}.
     *  A step with no group has no {@code @@} suffix (so old presets round-trip). */
    private static String encodeLoop(List<PatrolStep> steps, List<String> groups) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < steps.size(); i++) {
            if (sb.length() > 0) {
                sb.append(';');
            }
            sb.append(steps.get(i).encode());
            String g = i < groups.size() ? groups.get(i) : "";
            if (g != null && !g.isEmpty()) {
                sb.append("@@").append(g);
            }
        }
        return sb.toString();
    }

    /** Parse {@link #encodeLoop} back into the two lists. Tokens without {@code @@}
     *  (legacy presets) decode to an empty group label. Bad steps are skipped. */
    private static void decodeLoop(String text, List<PatrolStep> steps, List<String> groups) {
        steps.clear();
        groups.clear();
        if (text == null) {
            return;
        }
        for (String part : text.split(";")) {
            String p = part.trim();
            if (p.isEmpty()) {
                continue;
            }
            String g = "";
            int at = p.indexOf("@@");
            if (at >= 0) {
                g = p.substring(at + 2);
                p = p.substring(0, at);
            }
            try {
                steps.add(PatrolStep.decode(p));
                groups.add(g);
            } catch (RuntimeException ex) {
                System.out.println("Skipping bad loop step: " + part + " (" + ex.getMessage() + ")");
            }
        }
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
                    boolean match = c.equals(target);
                    System.out.printf(
                            "[ColorWatcher] ",
                            x, y, target.getRed(), target.getGreen(), target.getBlue(),
                            c.getRed(), c.getGreen(), c.getBlue(), match ? "TAK" : "nie");
                    if (match) {
                        System.out.printf("[ColorWatcher] Kolor docelowy wykryty w (%d,%d) → STOP bota.%n", x, y);
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
