package com.rental.ui;

import com.rental.exception.RentalException;
import com.rental.model.UserSession;
import com.rental.model.Vehicle;
import com.rental.service.RentalService;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/** Customer page: vehicle cards in a responsive grid with a category filter. */
public class BrowseVehiclesPage extends Page {

    private final UserSession session;
    private final RentalService service;
    private final ResponsiveGrid grid = new ResponsiveGrid(270);
    private final JLabel countLabel = new JLabel();
    private final JCheckBox availableOnly = new JCheckBox("Available only");
    private final PillBar categoryBar;
    private String category = "All";
    private List<Vehicle> vehicles = new ArrayList<>();

    public BrowseVehiclesPage(UserSession session, RentalService service) {
        this.session = session;
        this.service = service;

        categoryBar = new PillBar(new String[]{"All", "Hatchback", "Sedan", "SUV", "Motorcycle", "Scooter"}, false, label -> {
            category = label;
            showCards();
        });
        categoryBar.select("All", false);
        availableOnly.setOpaque(false);
        availableOnly.addActionListener(e -> showCards());
        countLabel.setForeground(Theme.MUTED);
        countLabel.setFont(Theme.font(Font.PLAIN, 13f));

        JPanel filters = new JPanel(new BorderLayout(16, 0));
        filters.setOpaque(false);
        filters.add(categoryBar, BorderLayout.WEST);
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.add(countLabel);
        right.add(availableOnly);
        filters.add(right, BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.add(header("Browse Vehicles", "Pick a vehicle, choose how many days you need and book it instantly."), BorderLayout.NORTH);
        top.add(filters, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        add(scroll, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        try {
            vehicles = service.getAllVehicles();
            showCards();
        } catch (RentalException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showCards() {
        grid.removeAll();
        int shown = 0;
        for (Vehicle v : vehicles) {
            boolean matchesCategory = category.equals("All") || v.getCategory().equals(category);
            if (matchesCategory && (!availableOnly.isSelected() || v.isAvailable())) {
                grid.add(new VehicleCard(v, this::openBooking));
                shown++;
            }
        }
        countLabel.setText(shown + " vehicle" + (shown == 1 ? "" : "s"));
        grid.revalidate();
        grid.repaint();
    }

    private void openBooking(Vehicle v) {
        BookingDialog dialog = new BookingDialog(SwingUtilities.getWindowAncestor(this), session, service, vehicles, v, null);
        dialog.setVisible(true);
        if (dialog.isBooked()) refresh();
    }
}
