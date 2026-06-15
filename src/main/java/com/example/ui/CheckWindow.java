package com.example.ui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.example.util.RobotActions;

/**
 * A helper window for finding screen coordinates/colors: press "L" and it
 * clicks at the current mouse position and prints the pixel color there.
 */
public final class CheckWindow {

    private CheckWindow() {
    }

    public static void open() {
        JFrame frame = new JFrame("Mouse Click Coordinates");
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setFocusable(true);
        panel.requestFocusInWindow();

        JButton closeButton = new JButton("CLOSE");
        closeButton.addActionListener(e -> frame.dispose());

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
                    Color pixelColor = robot.getPixelColor(x, y);
                    System.out.println("Clicked at: x = " + x + ", y = " + y);
                    System.out.println("Color at (" + x + ", " + y + "): " + pixelColor);
                } catch (AWTException ex) {
                    ex.printStackTrace();
                }
            }
        });

        frame.add(new JScrollPane(panel), BorderLayout.CENTER);
        frame.add(closeButton, BorderLayout.SOUTH);
        frame.setUndecorated(true);
        frame.setVisible(true);
    }
}
