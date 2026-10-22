package com.rental.ui;

import com.rental.exception.RentalException;
import com.rental.model.Rental;
import com.rental.model.UserSession;
import com.rental.service.RentalService;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Administrator dashboard: five summary cards and the most recent rentals. */
public class AdminDashboardPage extends Page {

    private final UserSession session;
    private final RentalService service;
    private final StatCard vehiclesCard = new StatCard("Total vehicles", Theme.PRIMARY);
    private final StatCard availableCard = new StatCard("Available vehicles", Theme.SUCCESS);
    private final StatCard activeCard = new StatCard("Active rentals", Theme.ACCENT);
    private final StatCard customersCard = new StatCard("Registered customers", new Color(0x7C3AED));
    private final StatCard revenueCard = new StatCard("Revenue (demo)", new Color(0x0EA5E9));
    private final StyledTable table = new StyledTable("Booking #", "Customer", "Vehicle", "Start", "Days", "Total", "Status");

    public AdminDashboardPage(UserSession session, RentalService service) {
        this.session = session;
        this.service = service;

        JPanel cards = new JPanel(new GridLayout(1, 5, 16, 0));
        cards.setOpaque(false);
        cards.add(vehiclesCard);
        cards.add(availableCard);
        cards.add(activeCard);
        cards.add(customersCard);
        cards.add(revenueCard);

        JPanel top = new JPanel(new BorderLayout(0, 18));
        top.setOpaque(false);
        top.add(header("Dashboard", "A quick overview of the rental business."), BorderLayout.NORTH);
        top.add(cards, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        JPanel recent = new JPanel(new BorderLayout(0, 10));
        recent.setOpaque(false);
        JLabel title = new JLabel("Recent rentals");
        title.setFont(Theme.font(Font.BOLD, 18f));
        recent.add(title, BorderLayout.NORTH);
        recent.add(table.wrap(), BorderLayout.CENTER);
        add(recent, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        try {
            Map<String, Integer> stats = service.getStats(session);
            vehiclesCard.setValue(String.valueOf(stats.get("Total Vehicles")));
            availableCard.setValue(String.valueOf(stats.get("Available Vehicles")));
            activeCard.setValue(String.valueOf(stats.get("Active Rentals")));
            customersCard.setValue(String.valueOf(stats.get("Registered Customers")));
            revenueCard.setValue(com.rental.model.Vehicle.CURRENCY + String.format("%,.0f", service.getTotalRevenue(session)));

            List<Object[]> rows = new ArrayList<>();
            for (Rental r : service.getRentals(session)) {
                if (rows.size() == 10) break;
                rows.add(new Object[]{r.getId(), r.getCustomerName(), r.getVehicleName(), Theme.date(r.getStartDate()),
                        r.getDays(), r.getTotalAmount(), r.getStatus()});
            }
            table.setRows(rows);
        } catch (RentalException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
