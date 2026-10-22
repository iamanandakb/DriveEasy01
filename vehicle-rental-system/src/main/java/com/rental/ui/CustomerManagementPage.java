package com.rental.ui;

import com.rental.exception.RentalException;
import com.rental.model.Customer;
import com.rental.model.Rental;
import com.rental.model.UserSession;
import com.rental.service.AuthService;
import com.rental.service.RentalService;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Administrator page: registered customers and a "Register Customer" button. */
public class CustomerManagementPage extends Page {

    private final UserSession session;
    private final RentalService service;
    private final AuthService auth;
    private final StyledTable table = new StyledTable("ID", "Name", "Email", "Phone", "Username", "Total Bookings", "Active Rentals");

    public CustomerManagementPage(UserSession session, RentalService service, AuthService auth) {
        this.session = session;
        this.service = service;
        this.auth = auth;

        RoundedButton register = new RoundedButton("+ Register Customer", Theme.PRIMARY);
        register.addActionListener(e -> {
            new RegisterDialog(SwingUtilities.getWindowAncestor(this), auth, true).setVisible(true);
            refresh();
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(register);

        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.add(header("Customers", "Everyone who has registered with the rental service."), BorderLayout.NORTH);
        top.add(buttons, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        add(table.wrap(), BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        try {
            Map<Integer, Integer> total = new HashMap<>();
            Map<Integer, Integer> active = new HashMap<>();
            for (Rental r : service.getRentals(session)) {
                total.merge(r.getCustomerId(), 1, Integer::sum);
                if (r.isActive()) active.merge(r.getCustomerId(), 1, Integer::sum);
            }
            List<Object[]> rows = new ArrayList<>();
            for (Customer c : service.getCustomers(session)) {
                rows.add(new Object[]{c.getId(), c.getName(), c.getEmail(), c.getPhone(), c.getUsername(),
                        total.getOrDefault(c.getId(), 0), active.getOrDefault(c.getId(), 0)});
            }
            table.setRows(rows);
        } catch (RentalException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
