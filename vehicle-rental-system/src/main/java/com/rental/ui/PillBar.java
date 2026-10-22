package com.rental.ui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** A row of "pill" buttons where exactly one is selected (used for category filters and service tabs). */
public class PillBar extends JPanel {

    private final List<RoundedButton> buttons = new ArrayList<>();
    private final Consumer<String> onSelect;

    public PillBar(String[] labels, boolean stretch, Consumer<String> onSelect) {
        this.onSelect = onSelect;
        setOpaque(false);
        setLayout(stretch ? new GridLayout(1, labels.length, 8, 0) : new FlowLayout(FlowLayout.LEFT, 8, 0));
        for (String label : labels) {
            RoundedButton button = new RoundedButton(label, Theme.BORDER);
            button.addActionListener(e -> select(label, true));
            buttons.add(button);
            add(button);
        }
    }

    /** Highlights the pill with this label and (optionally) tells the listener. */
    public void select(String label, boolean notify) {
        for (RoundedButton button : buttons) {
            boolean selected = button.getText().equals(label);
            button.setBaseColor(selected ? Theme.PRIMARY : new Color(0xE2E8F0));
            button.setForeground(selected ? Color.WHITE : Theme.TEXT);
        }
        if (notify) {
            onSelect.accept(label);
        }
    }
}
