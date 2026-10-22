package com.rental.ui;

import com.rental.exception.RentalException;
import com.rental.model.Customer;
import com.rental.model.UserSession;
import com.rental.model.Vehicle;
import com.rental.service.RentalService;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

/** Administrator page: every rental, with "New Rental" for a customer and "Return" for any rental. */
public class RentalManagementPage extends Page {

    private final UserSession session;
    private final RentalService service;
    private final RentalTablePanel rentals;

    public RentalManagementPage(UserSession session, RentalService service) {
        this.session = session;
        this.service = service;
        rentals = new RentalTablePanel(session, service, true);

        RoundedButton newRental = new RoundedButton("+ New Rental", Theme.PRIMARY);
        newRental.addActionListener(e -> createRental());
        rentals.addToolbarComponent(newRental);

        add(header("Rentals", "All bookings. Create a rental for a customer or process a vehicle return."), BorderLayout.NORTH);
        add(rentals, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        rentals.refresh();
    }

    private void createRental() {
        try {
            List<Vehicle> availableVehicles = new ArrayList<>();
            for (Vehicle v : service.getAllVehicles()) {
                if (v.isAvailable()) availableVehicles.add(v);
            }
            List<Customer> customers = service.getCustomers(session);
            if (availableVehicles.isEmpty() || customers.isEmpty()) {
                JOptionPane.showMessageDialog(this, "You need at least one available vehicle and one registered customer.",
                        "Cannot create rental", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            BookingDialog dialog = new BookingDialog(SwingUtilities.getWindowAncestor(this), session, service,
                    availableVehicles, availableVehicles.get(0), customers);
            dialog.setVisible(true);
            if (dialog.isBooked()) refresh();
        } catch (RentalException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
