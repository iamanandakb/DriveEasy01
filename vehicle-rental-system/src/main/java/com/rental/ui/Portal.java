package com.rental.ui;

import com.rental.model.UserSession;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ABSTRACT parent of CustomerPortal and AdminPortal: a dark sidebar with navigation
 * on the left and the selected page on the right. Subclasses only decide WHICH pages exist.
 */
public abstract class Portal extends JPanel {

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JPanel menu = new JPanel();
    private final Map<String, Page> pages = new LinkedHashMap<>();
    private final Map<String, NavButton> navButtons = new LinkedHashMap<>();

    protected Portal(UserSession session, String roleLabel, Runnable onLogout) {
        setLayout(new BorderLayout());

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Theme.NAVY);
        sidebar.setPreferredSize(new Dimension(240, 0));

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(BorderFactory.createEmptyBorder(26, 24, 18, 24));
        JLabel logo = new JLabel("DriveEasy");
        logo.setFont(Theme.font(Font.BOLD, 24f));
        logo.setForeground(Color.WHITE);
        JLabel role = new JLabel(roleLabel + " PORTAL");
        role.setFont(Theme.font(Font.BOLD, 11f));
        role.setForeground(Theme.ACCENT);
        brand.add(logo);
        brand.add(Box.createVerticalStrut(4));
        brand.add(role);

        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(BorderFactory.createEmptyBorder(14, 20, 22, 20));
        JLabel signedIn = new JLabel("Signed in as");
        signedIn.setFont(Theme.font(Font.PLAIN, 11f));
        signedIn.setForeground(new Color(0x94A3B8));
        JLabel name = new JLabel(session.getDisplayName());
        name.setFont(Theme.font(Font.BOLD, 14f));
        name.setForeground(Color.WHITE);
        RoundedButton logout = new RoundedButton("Log out", Theme.NAVY_LIGHT);
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        logout.addActionListener(e -> onLogout.run());
        signedIn.setAlignmentX(Component.LEFT_ALIGNMENT);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.add(signedIn);
        footer.add(name);
        footer.add(Box.createVerticalStrut(12));
        footer.add(logout);

        sidebar.add(brand, BorderLayout.NORTH);
        sidebar.add(menu, BorderLayout.CENTER);
        sidebar.add(footer, BorderLayout.SOUTH);

        content.setBackground(Theme.BG);
        add(sidebar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
    }

    /** Adds a page to the sidebar menu. */
    protected void addPage(String title, Page page) {
        pages.put(title, page);
        content.add(page, title);
        NavButton button = new NavButton(title);
        button.addActionListener(e -> showPage(title));
        navButtons.put(title, button);
        menu.add(button);
        menu.add(Box.createVerticalStrut(6));
    }

    /** Shows a page, reloads its data and highlights its menu entry. */
    protected void showPage(String title) {
        cards.show(content, title);
        pages.get(title).refresh();
        for (Map.Entry<String, NavButton> entry : navButtons.entrySet()) {
            entry.getValue().setSelectedItem(entry.getKey().equals(title));
        }
    }

    /** Sidebar button with a highlight for the selected page. */
    private static class NavButton extends JButton {
        private boolean selectedItem = false;
        private boolean hovering = false;

        NavButton(String text) {
            super(text);
            setFont(Theme.font(Font.BOLD, 14f));
            setForeground(new Color(0xCBD5E1));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            setPreferredSize(new Dimension(200, 46));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) { hovering = true; repaint(); }
                @Override public void mouseExited(java.awt.event.MouseEvent e) { hovering = false; repaint(); }
            });
        }

        void setSelectedItem(boolean selected) {
            this.selectedItem = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (selectedItem || hovering) {
                g2.setColor(selectedItem ? Theme.NAVY_LIGHT : new Color(0x172033));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
            if (selectedItem) {
                g2.setColor(Theme.ACCENT);
                g2.fillRoundRect(0, 10, 4, getHeight() - 20, 4, 4);
            }
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(selectedItem ? Color.WHITE : getForeground());
            g2.drawString(getText(), 20, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
