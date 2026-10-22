package com.rental.ui;

import com.rental.exception.RentalException;
import com.rental.model.Rental;
import com.rental.model.UserSession;
import com.rental.model.Vehicle;
import com.rental.service.RentalService;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/** Customer dashboard: welcome banner, summary cards and the customer's active rentals. */
public class CustomerHomePage extends Page {

    private final UserSession session;
    private final RentalService service;
    private final StatCard activeCard = new StatCard("Active rentals", Theme.ACCENT);
    private final StatCard totalCard = new StatCard("Total bookings", Theme.PRIMARY);
    private final StatCard spentCard = new StatCard("Total spent", Theme.SUCCESS);
    private final StatCard availableCard = new StatCard("Vehicles available now", new Color(0x7C3AED));
    private final StyledTable table = new StyledTable("Booking #", "Vehicle", "Start", "Due back", "Total", "Status");

    public CustomerHomePage(UserSession session, RentalService service) {
        this.session = session;
        this.service = service;

        JPanel banner = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Theme.PRIMARY, getWidth(), 0, new Color(0x7C3AED)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));
        JLabel hello = new JLabel("Welcome back, " + session.getDisplayName() + "!");
        hello.setFont(Theme.font(Font.BOLD, 24f));
        hello.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Browse vehicles, manage your bookings and explore our extra services.");
        sub.setFont(Theme.font(Font.PLAIN, 14f));
        sub.setForeground(new Color(0xDBEAFE));
        JPanel texts = new JPanel(new GridLayout(2, 1, 0, 4));
        texts.setOpaque(false);
        texts.add(hello);
        texts.add(sub);
        banner.add(texts, BorderLayout.CENTER);

        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);
        cards.add(activeCard);
        cards.add(totalCard);
        cards.add(spentCard);
        cards.add(availableCard);

        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.add(banner, BorderLayout.NORTH);
        top.add(cards, BorderLayout.CENTER);

        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setOpaque(false);
        JLabel tableTitle = new JLabel("Your active rentals");
        tableTitle.setFont(Theme.font(Font.BOLD, 18f));
        tablePanel.add(tableTitle, BorderLayout.NORTH);
        tablePanel.add(table.wrap(), BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        try {
            List<Rental> rentals = service.getRentals(session);
            List<Object[]> rows = new ArrayList<>();
            int active = 0;
            double spent = 0;
            for (Rental r : rentals) {
                spent += r.getTotalAmount();
                if (r.isActive()) {
                    active++;
                    rows.add(new Object[]{r.getId(), r.getVehicleName(), Theme.date(r.getStartDate()),
                            Theme.date(r.getEndDate()), r.getTotalAmount(), r.getStatus()});
                }
            }
            int available = 0;
            for (Vehicle v : service.getAllVehicles()) {
                if (v.isAvailable()) available++;
            }
            activeCard.setValue(String.valueOf(active));
            totalCard.setValue(String.valueOf(rentals.size()));
            spentCard.setValue(Vehicle.CURRENCY + String.format("%,.0f", spent));
            availableCard.setValue(String.valueOf(available));
            table.setRows(rows);
        } catch (RentalException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
