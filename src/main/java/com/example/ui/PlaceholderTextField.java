package com.example.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JTextField;

/**
 * A {@link JTextField} that paints grey placeholder text while it is empty and
 * unfocused. The hint vanishes as soon as the field gains focus or holds text,
 * so it never gets submitted as a real value.
 */
public final class PlaceholderTextField extends JTextField {

    private final String placeholder;

    public PlaceholderTextField(String placeholder, String text) {
        super(text == null ? "" : text);
        this.placeholder = placeholder;
        // Repaint on focus changes so the hint shows/hides immediately.
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (placeholder == null || placeholder.isEmpty()
                || !getText().isEmpty() || isFocusOwner()) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.GRAY);
        g2.setFont(getFont().deriveFont(Font.ITALIC));
        Insets in = getInsets();
        int y = (getHeight() + g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent()) / 2;
        g2.drawString(placeholder, in.left + 2, y);
        g2.dispose();
    }
}
