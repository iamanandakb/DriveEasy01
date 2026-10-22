package com.rental.ui;

import com.rental.model.UserSession;
import com.rental.service.RentalService;

/** INHERITANCE: the customer's version of the portal. It only decides which pages the sidebar contains. */
public class CustomerPortal extends Portal {

    public CustomerPortal(UserSession session, RentalService service, Runnable onLogout) {
        super(session, "CUSTOMER", onLogout);
        addPage("Home", new CustomerHomePage(session, service));
        addPage("Browse Vehicles", new BrowseVehiclesPage(session, service));
        addPage("My Bookings", new MyBookingsPage(session, service));
        addPage("Services & Info", new ServicesPage(session, service));
        showPage("Home");
    }
}
