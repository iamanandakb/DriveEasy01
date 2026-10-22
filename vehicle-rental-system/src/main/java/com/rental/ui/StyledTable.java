package com.rental.ui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

/**
 * A read-only table with a clean modern style, zebra rows and coloured status words.
 * Double values are shown as money, so only store money amounts as Double.
 */
public class StyledTable extends JTable {

    private final DefaultTableModel model;

    public StyledTable(String... columns) {
        super(new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }

            // Lets the sorter compare numbers as numbers instead of as text.
            @Override public Class<?> getColumnClass(int col) {
                return getRowCount() > 0 && getValueAt(0, col) != null ? getValueAt(0, col).getClass() : Object.class;
            }
        });
        this.model = (DefaultTableModel) getModel();

        setRowHeight(38);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        setFont(Theme.font(Font.PLAIN, 13f));
        setForeground(Theme.TEXT);
        setSelectionBackground(new Color(0xDBEAFE));
        setSelectionForeground(Theme.TEXT);
        setFillsViewportHeight(true);
        setAutoCreateRowSorter(true);
        setDefaultRenderer(Object.class, new CellRenderer());
        setDefaultRenderer(String.class, new CellRenderer());
        setDefaultRenderer(Integer.class, new CellRenderer());
        setDefaultRenderer(Double.class, new CellRenderer());

        JTableHeader header = getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(100, 40));
        header.setDefaultRenderer(new HeaderRenderer());
    }

    public void setRows(List<Object[]> rows) {
        model.setRowCount(0);
        for (Object[] row : rows) {
            model.addRow(row);
        }
        fitColumns();
    }

    /** Gives each column a preferred width based on its longest text, so words are not cut off. */
    private void fitColumns() {
        for (int col = 0; col < getColumnCount(); col++) {
            int width = getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(this, getColumnName(col), false, false, -1, col).getPreferredSize().width;
            for (int row = 0; row < getRowCount(); row++) {
                Component cell = prepareRenderer(getCellRenderer(row, col), row, col);
                width = Math.max(width, cell.getPreferredSize().width);
            }
            getColumnModel().getColumn(col).setPreferredWidth(Math.min(width + 8, 460));
        }
    }

    /** Index of the selected row in the data list (the sorter may reorder what is displayed). */
    public int getSelectedDataRow() {
        int viewRow = getSelectedRow();
        return viewRow < 0 ? -1 : convertRowIndexToModel(viewRow);
    }

    /** Wraps the table in a scroll pane with a light border. */
    public JScrollPane wrap() {
        JScrollPane scroll = new JScrollPane(this);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(t, v, false, false, r, c);
            label.setBackground(new Color(0xF8FAFC));
            label.setForeground(Theme.MUTED);
            label.setFont(Theme.font(Font.BOLD, 12f));
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
            label.setOpaque(true);
            return label;
        }
    }

    private static class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            Object shown = v instanceof Double ? Theme.money((Double) v) : v;
            JLabel label = (JLabel) super.getTableCellRendererComponent(t, shown, sel, false, r, c);
            label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setFont(Theme.font(Font.PLAIN, 13f));
            if (!sel) {
                label.setBackground(r % 2 == 0 ? Color.WHITE : new Color(0xF8FAFC));
                label.setForeground(Theme.TEXT);
            }
            String text = String.valueOf(v);
            if (text.equals("Available") || text.equals("RETURNED")) {
                label.setForeground(Theme.SUCCESS);
                label.setFont(Theme.font(Font.BOLD, 13f));
            } else if (text.equals("ACTIVE") || text.equals("Rented")) {
                label.setForeground(Theme.WARNING);
                label.setFont(Theme.font(Font.BOLD, 13f));
            } else if (text.equals("Unavailable")) {
                label.setForeground(Theme.DANGER);
                label.setFont(Theme.font(Font.BOLD, 13f));
            }
            return label;
        }
    }
}
