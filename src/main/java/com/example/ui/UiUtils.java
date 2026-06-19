package com.example.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/** Small Swing helpers for building forms and parsing field values. */
final class UiUtils {

    private UiUtils() {
    }

    static void addRow(JPanel panel, String label, JTextField field) {
        panel.add(new JLabel(label));
        panel.add(field);
    }

    static void addRow(JPanel panel, int number, String label, JTextField field) {
        panel.add(new JLabel(number + ". " + label));
        panel.add(field);
    }

    /** Narrow text field for coordinates / small numbers (≤4 digits). */
    static JTextField num(String value) {
        return new JTextField(value == null ? "" : value, 4);
    }

    /** Wider text field for "R,G,B" color triplets. */
    static JTextField rgb(String value) {
        return new JTextField(value == null ? "" : value, 8);
    }

    /** Vertical container that stacks category panels top-to-bottom. */
    static JPanel verticalBox() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    /** A titled category panel; add rows built with {@link #row}. */
    static JPanel category(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    /**
     * One left-aligned row. Each {@code String} part becomes a label, each
     * {@code JTextField} is added at its preferred width (so narrow coord fields
     * stay narrow and wide RGB fields stay wide).
     */
    static JPanel row(Object... parts) {
        JPanel r = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        r.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (Object part : parts) {
            if (part instanceof JTextField field) {
                r.add(field);
            } else if (part instanceof Component c) {
                r.add(c);
            } else {
                r.add(new JLabel(String.valueOf(part)));
            }
        }
        return r;
    }

    /**
     * A collapsible section: a full-width toggle header that shows/hides
     * {@code content}. The header shows an up-arrow when open, a down-arrow when
     * closed. Starts collapsed unless {@code expanded} is true. Add the returned
     * wrapper into a vertical box.
     */
    static JPanel collapsible(String title, JComponent content, boolean expanded) {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Flat, chrome-less header: blends with the background so it reads as a
        // section title, not a button. The arrow is the only affordance.
        JButton header = new JButton();
        header.setHorizontalAlignment(SwingConstants.LEFT);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setBorderPainted(false);
        header.setContentAreaFilled(false);
        header.setFocusPainted(false);
        header.setOpaque(false);
        header.setMargin(new Insets(2, 2, 2, 2));
        header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        header.setFont(header.getFont().deriveFont(Font.BOLD, header.getFont().getSize2D() + 4f));
        content.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.setVisible(expanded);

        // Arrow trails the title: down-triangle = open (click to fold),
        // right-triangle = closed.
        Runnable relabel = () -> header.setText(title + "  " + (content.isVisible() ? "▾" : "▸"));
        relabel.run();
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, header.getPreferredSize().height));
        header.addActionListener(e -> {
            content.setVisible(!content.isVisible());
            relabel.run();
            wrap.revalidate();
            wrap.repaint();
        });

        wrap.add(header);
        wrap.add(content);
        return wrap;
    }

    static int parseInt(JTextField field) {
        return Integer.parseInt(field.getText().trim());
    }

    static int[] parsePoint(JTextField xField, JTextField yField) {
        return new int[] { parseInt(xField), parseInt(yField) };
    }

    static int[] parseOptionalPoint(JTextField xField, JTextField yField) {
        String xText = xField.getText().trim();
        String yText = yField.getText().trim();
        if (xText.isEmpty() && yText.isEmpty()) {
            return null;
        }
        return new int[] { Integer.parseInt(xText), Integer.parseInt(yText) };
    }

    static Color parseColor(String text) {
        String[] values = text.split(",");
        if (values.length != 3) {
            throw new NumberFormatException("Color must have R,G,B");
        }
        return new Color(
                Integer.parseInt(values[0].trim()),
                Integer.parseInt(values[1].trim()),
                Integer.parseInt(values[2].trim()));
    }
}
