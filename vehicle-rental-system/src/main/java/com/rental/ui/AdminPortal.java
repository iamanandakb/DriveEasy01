package com.rental.ui;

import com.rental.model.UserSession;
import com.rental.service.AuthService;
import com.rental.service.RentalService;

/** INHERITANCE: the administrator's version of the portal. */
public class AdminPortal extends Portal {

    public AdminPortal(UserSession session, RentalService service, AuthService auth, Runnable onLogout) {
        super(session, "ADMIN", onLogout);
        addPage("Dashboard", new AdminDashboardPage(session, service));
        addPage("Vehicles", new VehicleManagementPage(session, service));
        addPage("Customers", new CustomerManagementPage(session, service, auth));
        addPage("Rentals", new RentalManagementPage(session, service));
        addPage("Services & Info", new ServicesPage(session, service));
        showPage("Dashboard");
    }
}
