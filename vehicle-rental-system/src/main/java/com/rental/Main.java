package com.rental;

import com.rental.db.Database;
import com.rental.ui.MainFrame;
import com.rental.ui.Theme;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.sql.SQLException;

/** Program entry point: prepare the database, then open the window. */
public class Main {

    public static void main(String[] args) {
        try {
            Database.initialize();      // creates the database file, tables and sample data if needed
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "The database could not be opened:\n" + e.getMessage() + "\n\nLocation: " + Database.getDbPath(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Swing must be created on the "event dispatch thread"
        SwingUtilities.invokeLater(() -> {
            Theme.install();
            new MainFrame().setVisible(true);
        });
    }
}
