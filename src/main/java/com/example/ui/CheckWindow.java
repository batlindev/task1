package com.example.ui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.example.util.RobotActions;

/**
 * A helper window for finding screen coordinates/colors: press "L" and it
 * clicks at the current mouse position and prints the pixel color there.
 *
 * The panel draws a short instruction list plus two reference dots (black and
 * green) so the user has obvious targets to hover/sample.
 */
public final class CheckWindow {

    private static final String[] INSTRUCTIONS = {
            "- Click LLM on the black dot below",
            "- move the mouse to the desired location",
            "- click the L_button.",
            "- (results in the terminal. )",
            "- Repeat the process.",
    };

    private CheckWindow() {
    }

    public static void open() {
        JFrame frame = new JFrame("Mouse Click Coordinates");
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(Color.BLACK);
                g2.setFont(getFont().deriveFont(Font.PLAIN, 14f));
                int y = 34;
                for (String line : INSTRUCTIONS) {
                    g2.drawString(line, 20, y);
                    y += 24;
                }

                // Two big, well-visible reference dots, centered: black | green.
                int diameter = 48;
                int cx = getWidth() / 2;
                int cy = getHeight() / 2 + 30;
                int blackX = cx - diameter - 16;
                int greenX = cx + 16;
                int top = cy - diameter / 2;

                g2.setColor(Color.BLACK);
                g2.fillOval(blackX, top, diameter, diameter);
                g2.setColor(Color.GREEN);
                g2.fillOval(greenX, top, diameter, diameter);

                g2.setColor(Color.DARK_GRAY);
                g2.drawOval(blackX, top, diameter, diameter);
                g2.drawOval(greenX, top, diameter, diameter);
            }
        };
        panel.setBackground(Color.WHITE);
        panel.setFocusable(true);
        panel.requestFocusInWindow();

        JButton closeButton = new JButton("CLOSE");
        closeButton.addActionListener(e -> frame.dispose());

        final int[] clickCount = {0};
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_L) {
                    return;
                }
                try {
                    Robot robot = new Robot();
                    int x = (int) MouseInfo.getPointerInfo().getLocation().getX();
                    int y = (int) MouseInfo.getPointerInfo().getLocation().getY();

                    RobotActions.clickMouse(robot, x, y);
                    Color c = robot.getPixelColor(x, y);
                    int n = ++clickCount[0];
                    System.out.println(n + " clicked at:");
                    System.out.println(n+ "X = " + x);
                    System.out.println(n+"Y = " + y);
                    System.out.println("COLOR: [r=" + c.getRed() + ",g=" + c.getGreen()
                            + ",b=" + c.getBlue() + "]");
                    System.out.println("=============");
                } catch (AWTException ex) {
                    ex.printStackTrace();
                }
            }
        });

        frame.add(panel, BorderLayout.CENTER);
        frame.add(closeButton, BorderLayout.SOUTH);
        frame.setUndecorated(true);
        frame.setVisible(true);
    }
}
