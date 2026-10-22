package com.rental.ui;

import com.rental.exception.RentalException;
import com.rental.model.Customer;
import com.rental.model.Rental;
import com.rental.model.UserSession;
import com.rental.model.Vehicle;
import com.rental.service.RentalService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.time.LocalDate;
import java.util.List;

/**
 * Booking form. Customers open it from a vehicle card (vehicle and customer are fixed).
 * The administrator opens it from "New Rental" and also picks the customer and vehicle.
 */
public class BookingDialog extends JDialog {

    private final UserSession session;
    private final RentalService service;

    private final JComboBox<Customer> customerBox;      // null for customers
    private final JComboBox<Vehicle> vehicleBox;
    private final JSpinner daysSpinner = new JSpinner(new SpinnerNumberModel(1, 1, RentalService.MAX_DAYS, 1));
    private final JTextField promoField = new JTextField(10);
    private final JComboBox<String> paymentBox = new JComboBox<>(RentalService.PAYMENT_METHODS);

    private final ImagePanel picture = new ImagePanel("swift.png", 16);
    private final JLabel nameLabel = new JLabel();
    private final JLabel specsLabel = new JLabel();
    private final JLabel standardLabel = new JLabel();
    private final JLabel volumeLabel = new JLabel();
    private final JLabel promoLabel = new JLabel();
    private final JLabel totalLabel = new JLabel();
    private final JLabel dueLabel = new JLabel();
    private final JLabel messageLabel = new JLabel(" ");

    private String appliedPromo = "";
    private boolean booked = false;

    /**
     * @param vehicles  vehicles to choose from (only available ones are useful)
     * @param selected  vehicle to preselect (may be null)
     * @param customers list of customers for the administrator flow, or null for the customer flow
     */
    public BookingDialog(Window owner, UserSession session, RentalService service,
                         List<Vehicle> vehicles, Vehicle selected, List<Customer> customers) {
        super(owner, "Book a Vehicle", ModalityType.APPLICATION_MODAL);
        this.session = session;
        this.service = service;

        vehicleBox = new JComboBox<Vehicle>(vehicles.toArray(new Vehicle[0]));
        if (selected != null) {
            for (int i = 0; i < vehicleBox.getItemCount(); i++) {
                if (vehicleBox.getItemAt(i).getId() == selected.getId()) vehicleBox.setSelectedIndex(i);
            }
        }
        vehicleBox.setEnabled(customers != null);       // customers booked one specific vehicle already
        customerBox = customers == null ? null : new JComboBox<Customer>(customers.toArray(new Customer[0]));

        JPanel root = new JPanel(new BorderLayout(26, 0));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(24, 26, 22, 26));
        root.add(buildLeft(), BorderLayout.WEST);
        root.add(buildRight(), BorderLayout.CENTER);
        setContentPane(root);

        daysSpinner.addChangeListener(e -> refreshQuote());
        vehicleBox.addActionListener(e -> refreshQuote());
        refreshQuote();

        pack();
        setLocationRelativeTo(owner);
    }

    public boolean isBooked() {
        return booked;
    }

    private JPanel buildLeft() {
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setPreferredSize(new Dimension(330, 300));
        picture.setPreferredSize(new Dimension(330, 200));
        picture.setMaximumSize(new Dimension(330, 200));
        picture.setAlignmentX(0f);
        nameLabel.setFont(Theme.font(Font.BOLD, 22f));
        specsLabel.setFont(Theme.font(Font.PLAIN, 14f));
        specsLabel.setForeground(Theme.MUTED);
        JLabel rules = new JLabel("<html>Long-rental discounts: cars 10% off from 7 days,<br>bikes 5% off from 5 days. Late returns are charged<br>one extra daily rate per day.</html>");
        rules.setFont(Theme.font(Font.PLAIN, 12f));
        rules.setForeground(Theme.MUTED);
        left.add(picture);
        left.add(Box.createVerticalStrut(14));
        left.add(nameLabel);
        left.add(Box.createVerticalStrut(4));
        left.add(specsLabel);
        left.add(Box.createVerticalStrut(14));
        left.add(rules);
        return left;
    }

    private JPanel buildRight() {
        JPanel right = new JPanel(new BorderLayout(0, 12));
        right.setOpaque(false);

        FormPanel form = new FormPanel();
        if (customerBox != null) form.addRow("Customer", customerBox);
        form.addRow("Vehicle", vehicleBox);
        form.addRow("Rental days (1-" + RentalService.MAX_DAYS + ")", daysSpinner);

        Theme.styleField(promoField);
        RoundedButton apply = new RoundedButton("Apply", Theme.PRIMARY);
        apply.addActionListener(e -> {
            appliedPromo = promoField.getText().trim();
            refreshQuote();
        });
        JPanel promoRow = new JPanel(new BorderLayout(8, 0));
        promoRow.setOpaque(false);
        promoRow.add(promoField, BorderLayout.CENTER);
        promoRow.add(apply, BorderLayout.EAST);
        form.addRow("Promo code", promoRow);
        form.addRow("Payment (simulated)", paymentBox);
        right.add(form, BorderLayout.NORTH);

        RoundedPanel summary = new RoundedPanel(14, new Color(0xF8FAFC), new java.awt.GridLayout(0, 2, 10, 6));
        summary.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        addSummaryRow(summary, "Standard price", standardLabel, false);
        addSummaryRow(summary, "Long-rental discount", volumeLabel, false);
        addSummaryRow(summary, "Promo discount", promoLabel, false);
        addSummaryRow(summary, "Return by", dueLabel, false);
        addSummaryRow(summary, "TOTAL", totalLabel, true);

        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setOpaque(false);
        messageLabel.setForeground(Theme.DANGER);
        messageLabel.setFont(Theme.font(Font.PLAIN, 12f));
        JLabel note = new JLabel("Payment is simulated - no money is charged.");
        note.setFont(Theme.font(Font.ITALIC, 12f));
        note.setForeground(Theme.MUTED);

        RoundedButton cancel = new RoundedButton("Cancel", new Color(0x94A3B8));
        RoundedButton confirm = new RoundedButton("Confirm Booking", Theme.ACCENT);
        cancel.addActionListener(e -> dispose());
        confirm.addActionListener(e -> confirmBooking());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(cancel);
        buttons.add(confirm);

        JPanel messages = new JPanel(new BorderLayout());
        messages.setOpaque(false);
        messages.add(note, BorderLayout.NORTH);
        messages.add(messageLabel, BorderLayout.SOUTH);
        south.add(messages, BorderLayout.NORTH);
        south.add(buttons, BorderLayout.SOUTH);

        right.add(summary, BorderLayout.CENTER);
        right.add(south, BorderLayout.SOUTH);
        right.setPreferredSize(new Dimension(400, 440));
        return right;
    }

    private void addSummaryRow(JPanel panel, String label, JLabel value, boolean bold) {
        JLabel l = new JLabel(label);
        l.setFont(Theme.font(bold ? Font.BOLD : Font.PLAIN, bold ? 16f : 13f));
        l.setForeground(bold ? Theme.TEXT : Theme.MUTED);
        value.setFont(Theme.font(Font.BOLD, bold ? 20f : 13f));
        value.setForeground(bold ? Theme.PRIMARY : Theme.TEXT);
        value.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        panel.add(l);
        panel.add(value);
    }

    /** Recalculates the price whenever the vehicle, number of days or promo code changes. */
    private void refreshQuote() {
        Vehicle v = (Vehicle) vehicleBox.getSelectedItem();
        if (v == null) return;
        picture.setImage(v.getImageFile());
        nameLabel.setText(v.getName());
        specsLabel.setText(v.getCategory() + "  \u2022  " + v.getSpecs());
        int days = (Integer) daysSpinner.getValue();
        try {
            double[] q = service.quote(v, days, appliedPromo);
            standardLabel.setText(Theme.money(q[0]));
            volumeLabel.setText("- " + Theme.money(q[1]));
            promoLabel.setText("- " + Theme.money(q[2]));
            totalLabel.setText(Theme.money(q[3]));
            dueLabel.setText(Theme.date(LocalDate.now().plusDays(days)));
            messageLabel.setForeground(Theme.SUCCESS);
            messageLabel.setText(appliedPromo.isEmpty() ? " " : "Promo code applied.");
        } catch (RentalException ex) {
            appliedPromo = "";
            messageLabel.setForeground(Theme.DANGER);
            messageLabel.setText(ex.getMessage());
            refreshWithoutPromo(v, days);
        }
    }

    private void refreshWithoutPromo(Vehicle v, int days) {
        try {
            double[] q = service.quote(v, days, "");
            standardLabel.setText(Theme.money(q[0]));
            volumeLabel.setText("- " + Theme.money(q[1]));
            promoLabel.setText("- " + Theme.money(0));
            totalLabel.setText(Theme.money(q[3]));
            dueLabel.setText(Theme.date(LocalDate.now().plusDays(days)));
        } catch (RentalException ignored) {
            totalLabel.setText("-");
        }
    }

    private void confirmBooking() {
        Vehicle v = (Vehicle) vehicleBox.getSelectedItem();
        if (v == null) {
            JOptionPane.showMessageDialog(this, "There is no vehicle to book.", "Nothing to book", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int customerId = session.getCustomerId();
        if (customerBox != null) {
            Customer c = (Customer) customerBox.getSelectedItem();
            if (c == null) {
                JOptionPane.showMessageDialog(this, "Please register a customer first.", "No customer", JOptionPane.WARNING_MESSAGE);
                return;
            }
            customerId = c.getId();
        }
        try {
            Rental r = service.rentVehicle(session, customerId, v.getId(), (Integer) daysSpinner.getValue(),
                    (String) paymentBox.getSelectedItem(), appliedPromo);
            booked = true;
            JOptionPane.showMessageDialog(this,
                    "Booking confirmed!\n\nBooking #" + r.getId() + "\nVehicle: " + r.getVehicleName()
                            + "\nCustomer: " + r.getCustomerName()
                            + "\nDuration: " + r.getDays() + " day(s)\nReturn by: " + Theme.date(r.getEndDate())
                            + "\nTotal: " + Theme.money(r.getTotalAmount()) + "\nPayment: " + r.getPaymentMethod() + " (simulated)",
                    "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (RentalException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Booking failed", JOptionPane.WARNING_MESSAGE);
        }
    }
}
