package com.rental.ui;

import com.rental.exception.RentalException;
import com.rental.model.Rental;
import com.rental.model.UserSession;
import com.rental.model.Vehicle;
import com.rental.service.RentalService;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Administrator page: vehicle inventory with Add / Edit / Delete. */
public class VehicleManagementPage extends Page {

    private final UserSession session;
    private final RentalService service;
    private final StyledTable table = new StyledTable("ID", "Type", "Name", "Brand", "Category", "Daily Rate", "Specs", "Status");
    private List<Vehicle> vehicles = new ArrayList<>();

    public VehicleManagementPage(UserSession session, RentalService service) {
        this.session = session;
        this.service = service;

        RoundedButton add = new RoundedButton("+ Add Vehicle", Theme.PRIMARY);
        RoundedButton edit = new RoundedButton("Edit Selected", Theme.ACCENT);
        RoundedButton delete = new RoundedButton("Delete Selected", Theme.DANGER);
        add.addActionListener(e -> addVehicle());
        edit.addActionListener(e -> editVehicle());
        delete.addActionListener(e -> deleteVehicle());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(add);
        buttons.add(edit);
        buttons.add(delete);

        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.add(header("Vehicle Inventory", "Add vehicles, update their details and see which ones are currently rented."), BorderLayout.NORTH);
        top.add(buttons, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        add(table.wrap(), BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        try {
            vehicles = service.getAllVehicles();
            Set<Integer> rentedIds = new HashSet<>();
            for (Rental r : service.getRentals(session)) {
                if (r.isActive()) rentedIds.add(r.getVehicleId());
            }
            List<Object[]> rows = new ArrayList<>();
            for (Vehicle v : vehicles) {
                String status = v.isAvailable() ? "Available" : (rentedIds.contains(v.getId()) ? "Rented" : "Unavailable");
                rows.add(new Object[]{v.getId(), v.getType(), v.getName(), v.getBrand(), v.getCategory(),
                        v.getDailyRate(), v.getSpecs(), status});
            }
            table.setRows(rows);
        } catch (RentalException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Vehicle selectedVehicle() {
        int row = table.getSelectedDataRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a vehicle in the table first.", "Nothing selected", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return vehicles.get(row);
    }

    private void addVehicle() {
        VehicleFormDialog dialog = new VehicleFormDialog(SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.getResult() == null) return;
        try {
            service.addVehicle(session, dialog.getResult());
            refresh();
            JOptionPane.showMessageDialog(this, dialog.getResult().getName() + " was added to the inventory.", "Vehicle added", JOptionPane.INFORMATION_MESSAGE);
        } catch (RentalException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot add vehicle", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void editVehicle() {
        Vehicle selected = selectedVehicle();
        if (selected == null) return;
        VehicleFormDialog dialog = new VehicleFormDialog(SwingUtilities.getWindowAncestor(this), selected);
        dialog.setVisible(true);
        if (dialog.getResult() == null) return;
        try {
            service.updateVehicle(session, dialog.getResult());
            refresh();
        } catch (RentalException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot update vehicle", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteVehicle() {
        Vehicle selected = selectedVehicle();
        if (selected == null) return;
        if (JOptionPane.showConfirmDialog(this, "Delete " + selected.getName() + " permanently?", "Confirm delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        try {
            service.deleteVehicle(session, selected.getId());
            refresh();
        } catch (RentalException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot delete vehicle", JOptionPane.WARNING_MESSAGE);
        }
    }
}
