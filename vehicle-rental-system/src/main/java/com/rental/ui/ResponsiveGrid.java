package com.rental.ui;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;

/**
 * A grid that changes its number of columns when the window is resized,
 * so vehicle cards always fill the width nicely (1 column on narrow windows, 4+ on wide ones).
 */
public class ResponsiveGrid extends JPanel implements Scrollable {

    private static final int GAP = 18;
    private final int minCardWidth;

    public ResponsiveGrid(int minCardWidth) {
        this.minCardWidth = minCardWidth;
        setOpaque(false);
        setLayout(new GridLayout(0, 1, GAP, GAP));
    }

    @Override
    public void doLayout() {
        int columns = Math.max(1, (getWidth() + GAP) / (minCardWidth + GAP));
        GridLayout grid = (GridLayout) getLayout();
        if (grid.getColumns() != columns) {
            grid.setColumns(columns);
            revalidate();       // the height changes with the number of columns
        }
        super.doLayout();
    }

    // ---- Scrollable: follow the width of the scroll pane, scroll vertically
    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) { return 24; }
    @Override public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) { return 160; }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}
