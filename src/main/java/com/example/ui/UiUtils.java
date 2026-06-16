package com.example.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
            } else {
                r.add(new JLabel(String.valueOf(part)));
            }
        }
        return r;
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
