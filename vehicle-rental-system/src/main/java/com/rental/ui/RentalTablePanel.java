package com.rental.ui;

import com.rental.exception.RentalException;
import com.rental.model.Rental;
import com.rental.model.UserSession;
import com.rental.service.RentalService;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/**
 * Rental table + details + "Return" button. Shared by the customer's "My Bookings" page
 * and the administrator's "Rentals" page (the service decides what each user may see or return).
 */
public class RentalTablePanel extends JPanel implements Refreshable {

    private final UserSession session;
    private final RentalService service;
    private final StyledTable table;
    private final JTextArea details = new JTextArea();
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All", "Active", "Returned"});
    private final JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    private final boolean showCustomer;
    private List<Rental> shown = new ArrayList<>();

    public RentalTablePanel(UserSession session, RentalService service, boolean showCustomer) {
        this.session = session;
        this.service = service;
        this.showCustomer = showCustomer;
        setOpaque(false);
        setLayout(new BorderLayout(0, 12));

        table = showCustomer
                ? new StyledTable("ID", "Customer", "Vehicle", "Start", "Days", "Due", "Returned", "Late Fee", "Total", "Status")
                : new StyledTable("ID", "Vehicle", "Start", "Days", "Due", "Returned", "Late Fee", "Total", "Status");

        toolbar.setOpaque(false);
        JLabel filterLabel = new JLabel("Show:");
        filterLabel.setForeground(Theme.MUTED);
        RoundedButton returnButton = new RoundedButton("Return Selected Vehicle", Theme.SUCCESS);
        returnButton.addActionListener(e -> returnSelected());
        toolbar.add(returnButton);
        toolbar.add(filterLabel);
        toolbar.add(statusFilter);
        statusFilter.addActionListener(e -> refresh());
        add(toolbar, BorderLayout.NORTH);

        details.setEditable(false);
        details.setFont(Theme.font(Font.PLAIN, 13f));
        details.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        details.setText("Select a booking above to see its details.");
        JScrollPane detailScroll = new JScrollPane(details);
        detailScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, table.wrap(), detailScroll);
        split.setResizeWeight(0.72);
        split.setBorder(null);
        split.setOpaque(false);
        add(split, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showDetails();
        });
    }

    /** Lets the administrator page add its own buttons (for example "New Rental"). */
    public void addToolbarComponent(java.awt.Component c) {
        toolbar.add(c, 0);
    }

    @Override
    public void refresh() {
        try {
            shown = new ArrayList<>();
            List<Object[]> rows = new ArrayList<>();
            String filter = (String) statusFilter.getSelectedItem();
            for (Rental r : service.getRentals(session)) {
                if ("Active".equals(filter) && !r.isActive()) continue;
                if ("Returned".equals(filter) && r.isActive()) continue;
                shown.add(r);
                rows.add(toRow(r));
            }
            table.setRows(rows);
            details.setText("Select a booking above to see its details.");
        } catch (RentalException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Object[] toRow(Rental r) {
        if (showCustomer) {
            return new Object[]{r.getId(), r.getCustomerName(), r.getVehicleName(), Theme.date(r.getStartDate()), r.getDays(),
                    Theme.date(r.getEndDate()), Theme.date(r.getReturnDate()), r.getLateFee(),
                    r.getTotalAmount(), r.getStatus()};
        }
        return new Object[]{r.getId(), r.getVehicleName(), Theme.date(r.getStartDate()), r.getDays(),
                Theme.date(r.getEndDate()), Theme.date(r.getReturnDate()), r.getLateFee(),
                r.getTotalAmount(), r.getStatus()};
    }

    private Rental selectedRental() {
        int row = table.getSelectedDataRow();
        return row < 0 || row >= shown.size() ? null : shown.get(row);
    }

    private void showDetails() {
        Rental r = selectedRental();
        if (r == null) return;
        details.setText("Booking #" + r.getId() + "   (" + r.getStatus() + ")\n"
                + "Customer: " + r.getCustomerName() + "      Vehicle: " + r.getVehicleName() + "\n"
                + "Rental period: " + Theme.date(r.getStartDate()) + "  to  " + Theme.date(r.getEndDate())
                + "   (" + r.getDays() + " day" + (r.getDays() == 1 ? "" : "s") + ")\n"
                + "Returned on: " + Theme.date(r.getReturnDate()) + "\n"
                + "Discounts: " + Theme.money(r.getDiscount()) + "      Late fee: " + Theme.money(r.getLateFee())
                + "      TOTAL CHARGED: " + Theme.money(r.getTotalAmount()) + "\n"
                + "Payment method: " + r.getPaymentMethod() + " (simulated)");
        details.setCaretPosition(0);
    }

    private void returnSelected() {
        Rental r = selectedRental();
        if (r == null) {
            JOptionPane.showMessageDialog(this, "Please select a booking first.", "Nothing selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            if (!r.isActive()) {
                JOptionPane.showMessageDialog(this, "This vehicle has already been returned.", "Already returned", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            double lateFee = service.calculateLateFee(r);
            String question = "Return " + r.getVehicleName() + " (booking #" + r.getId() + ")?"
                    + (lateFee > 0 ? "\nIt is overdue. A late fee of " + Theme.money(lateFee) + " will be added." : "");
            if (JOptionPane.showConfirmDialog(this, question, "Confirm return", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            Rental done = service.returnVehicle(session, r.getId());     // the service re-checks ownership
            JOptionPane.showMessageDialog(this,
                    done.getVehicleName() + " returned successfully.\nFinal total: " + Theme.money(done.getTotalAmount()),
                    "Vehicle returned", JOptionPane.INFORMATION_MESSAGE);
            refresh();
        } catch (RentalException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot return", JOptionPane.WARNING_MESSAGE);
        }
    }
}
