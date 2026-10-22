package com.rental.ui;

import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** A flat, rounded, coloured button that looks the same on Windows, macOS and Linux. */
public class RoundedButton extends JButton {

    private Color baseColor;
    private boolean hovering = false;

    public RoundedButton(String text, Color baseColor) {
        super(text);
        this.baseColor = baseColor;
        setForeground(Color.WHITE);
        setFont(Theme.font(Font.BOLD, 13f));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
        });
    }

    @Override
    public void updateUI() {
        setUI(new BasicButtonUI());     // plain UI so that our own painting is used on every look & feel
    }

    public void setBaseColor(Color color) {
        this.baseColor = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color fill = !isEnabled() ? new Color(0xCBD5E1) : (hovering ? baseColor.darker() : baseColor);
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(isEnabled() ? getForeground() : Color.WHITE);
        g2.drawString(getText(), x, y);
        g2.dispose();
    }
}
