package com.rental.ui;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/** A dashboard summary card: a big number with a small title and a coloured accent strip. */
public class StatCard extends RoundedPanel {

    private final JLabel valueLabel = new JLabel("-");
    private final Color accent;

    public StatCard(String title, Color accent) {
        super(16, Color.WHITE);
        this.accent = accent;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 18));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.font(Font.PLAIN, 13f));
        titleLabel.setForeground(Theme.MUTED);
        valueLabel.setFont(Theme.font(Font.BOLD, 26f));
        valueLabel.setForeground(Theme.TEXT);

        add(titleLabel);
        add(javax.swing.Box.createVerticalStrut(6));
        add(valueLabel);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(accent);
        g.fillRoundRect(0, 14, 5, getHeight() - 28, 5, 5);
    }
}
