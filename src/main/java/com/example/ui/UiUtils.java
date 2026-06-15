package com.example.ui;

import java.awt.Color;

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
