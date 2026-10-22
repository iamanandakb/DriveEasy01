package com.rental.ui;

import com.rental.model.UserSession;
import com.rental.service.RentalService;

import java.awt.BorderLayout;

/** Customer page: current and previous bookings of the logged-in customer, with a Return button. */
public class MyBookingsPage extends Page {

    private final RentalTablePanel rentals;

    public MyBookingsPage(UserSession session, RentalService service) {
        rentals = new RentalTablePanel(session, service, false);
        add(header("My Bookings", "Your current and previous rentals. Select a booking to see its details or return the vehicle."), BorderLayout.NORTH);
        add(rentals, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        rentals.refresh();
    }
}
