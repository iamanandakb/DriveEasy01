package com.rental.ui;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/** A simple two-column "label : field" form layout used by all dialogs. */
public class FormPanel extends JPanel {

    private int row = 0;

    public FormPanel() {
        super(new GridBagLayout());
        setOpaque(false);
    }

    public void addRow(String labelText, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.anchor = GridBagConstraints.WEST;

        JLabel label = new JLabel(labelText);
        label.setFont(Theme.font(Font.BOLD, 12f));
        label.setForeground(Theme.MUTED);
        c.gridx = 0;
        c.insets = new Insets(7, 0, 7, 14);
        add(label, c);

        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(7, 0, 7, 0);
        add(field, c);
        row++;
    }
}
