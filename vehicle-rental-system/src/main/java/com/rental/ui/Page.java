package com.rental.ui;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Font;

/** ABSTRACT base class for every screen inside a portal (shared padding, background and title block). */
public abstract class Page extends JPanel implements Refreshable {

    protected Page() {
        setLayout(new BorderLayout(0, 18));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
    }

    /** Builds the big title + grey subtitle shown at the top of a page. */
    protected JPanel header(String title, String subtitle) {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(Theme.font(Font.BOLD, 26f));
        t.setForeground(Theme.TEXT);
        JLabel s = new JLabel(subtitle);
        s.setFont(Theme.font(Font.PLAIN, 14f));
        s.setForeground(Theme.MUTED);
        box.add(t);
        box.add(javax.swing.Box.createVerticalStrut(4));
        box.add(s);
        return box;
    }

    @Override
    public void refresh() {
        // pages that show database data override this
    }
}
