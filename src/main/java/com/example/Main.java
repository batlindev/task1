package com.example;

import javax.swing.SwingUtilities;

import com.example.ui.MainWindow;

/** Application entry point. */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainWindow::show);
    }
}
