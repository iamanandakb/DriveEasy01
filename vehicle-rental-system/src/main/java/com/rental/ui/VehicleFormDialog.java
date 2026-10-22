package com.rental.ui;

import com.rental.model.Bike;
import com.rental.model.Car;
import com.rental.model.Vehicle;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;

/** Add / edit vehicle form (administrator only). Builds a Car or a Bike object from the fields. */
public class VehicleFormDialog extends JDialog {

    private final Vehicle existing;      // null when adding
    private Vehicle result = null;

    private final JComboBox<String> typeBox = new JComboBox<>(new String[]{"Car", "Bike"});
    private final JComboBox<String> categoryBox = new JComboBox<>();
    private final JTextField nameField = new JTextField(18);
    private final JTextField brandField = new JTextField(18);
    private final JTextField rateField = new JTextField(18);
    private final JSpinner seatsSpinner = new JSpinner(new SpinnerNumberModel(5, 2, 9, 1));
    private final JComboBox<String> fuelBox = new JComboBox<>(new String[]{"Petrol", "Diesel", "Hybrid", "Electric", "CNG"});
    private final JSpinner ccSpinner = new JSpinner(new SpinnerNumberModel(150, 50, 2000, 10));
    private final JCheckBox gearBox = new JCheckBox("Geared", true);
    private final JComboBox<String> imageBox = new JComboBox<>(ImageLoader.ALL_IMAGES);
    private final JCheckBox availableBox = new JCheckBox("Available for rent", true);
    private final ImagePanel preview = new ImagePanel("swift.png", 12);
    private final CardLayout detailCards = new CardLayout();
    private final JPanel detailPanel = new JPanel(detailCards);

    public VehicleFormDialog(Window owner, Vehicle existing) {
        super(owner, existing == null ? "Add Vehicle" : "Edit Vehicle", ModalityType.APPLICATION_MODAL);
        this.existing = existing;

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(22, 26, 20, 26));
        JLabel title = new JLabel(existing == null ? "Add a new vehicle" : "Edit vehicle details");
        title.setFont(Theme.font(Font.BOLD, 20f));
        root.add(title, BorderLayout.NORTH);

        Theme.styleField(nameField);
        Theme.styleField(brandField);
        Theme.styleField(rateField);

        FormPanel carForm = new FormPanel();
        carForm.addRow("Seats", seatsSpinner);
        carForm.addRow("Fuel", fuelBox);
        FormPanel bikeForm = new FormPanel();
        bikeForm.addRow("Engine (cc)", ccSpinner);
        bikeForm.addRow("Gearbox", gearBox);
        detailPanel.setOpaque(false);
        detailPanel.add(carForm, "Car");
        detailPanel.add(bikeForm, "Bike");

        FormPanel form = new FormPanel();
        form.addRow("Type", typeBox);
        form.addRow("Category", categoryBox);
        form.addRow("Name", nameField);
        form.addRow("Brand", brandField);
        form.addRow("Daily rate (" + Vehicle.CURRENCY + ")", rateField);
        form.addRow("Image", imageBox);
        form.addRow("Status", availableBox);

        JPanel center = new JPanel(new BorderLayout(0, 4));
        center.setOpaque(false);
        center.add(form, BorderLayout.NORTH);
        center.add(detailPanel, BorderLayout.CENTER);
        preview.setPreferredSize(new Dimension(300, 120));
        center.add(preview, BorderLayout.SOUTH);
        root.add(center, BorderLayout.CENTER);

        RoundedButton cancel = new RoundedButton("Cancel", new Color(0x94A3B8));
        RoundedButton save = new RoundedButton("Save Vehicle", Theme.PRIMARY);
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> save());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(cancel);
        buttons.add(save);
        root.add(buttons, BorderLayout.SOUTH);

        typeBox.addActionListener(e -> updateTypeFields());
        imageBox.addActionListener(e -> preview.setImage((String) imageBox.getSelectedItem()));
        updateTypeFields();
        if (existing != null) fillFromExisting();
        availableBox.setVisible(existing != null);

        setContentPane(root);
        setMinimumSize(new Dimension(440, 0));
        pack();
        setLocationRelativeTo(owner);
    }

    /** The vehicle built from the form, or null if the dialog was cancelled. */
    public Vehicle getResult() {
        return result;
    }

    private void updateTypeFields() {
        boolean car = "Car".equals(typeBox.getSelectedItem());
        categoryBox.setModel(new DefaultComboBoxModel<>(car ? Vehicle.CAR_CATEGORIES : Vehicle.BIKE_CATEGORIES));
        detailCards.show(detailPanel, car ? "Car" : "Bike");
    }

    private void fillFromExisting() {
        boolean car = existing instanceof Car;
        typeBox.setSelectedItem(car ? "Car" : "Bike");
        typeBox.setEnabled(false);                       // the type of an existing vehicle cannot change
        categoryBox.setSelectedItem(existing.getCategory());
        nameField.setText(existing.getName());
        brandField.setText(existing.getBrand());
        rateField.setText(String.valueOf((long) existing.getDailyRate()));
        imageBox.setSelectedItem(existing.getImageFile());
        preview.setImage(existing.getImageFile());
        availableBox.setSelected(existing.isAvailable());
        if (car) {
            seatsSpinner.setValue(((Car) existing).getSeats());
            fuelBox.setSelectedItem(((Car) existing).getFuel());
        } else {
            ccSpinner.setValue(((Bike) existing).getEngineCc());
            gearBox.setSelected(((Bike) existing).isGeared());
        }
    }

    private void save() {
        double rate;
        try {
            rate = Double.parseDouble(rateField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Daily rate must be a number, for example 1500.", "Check your input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = existing == null ? 0 : existing.getId();
        String category = (String) categoryBox.getSelectedItem();
        String image = (String) imageBox.getSelectedItem();
        boolean available = existing == null || availableBox.isSelected();
        try {
            // The Car / Bike constructors validate their values and throw IllegalArgumentException if invalid.
            if ("Car".equals(typeBox.getSelectedItem())) {
                result = new Car(id, nameField.getText(), brandField.getText(), category, rate, image, available,
                        (Integer) seatsSpinner.getValue(), (String) fuelBox.getSelectedItem());
            } else {
                result = new Bike(id, nameField.getText(), brandField.getText(), category, rate, image, available,
                        (Integer) ccSpinner.getValue(), gearBox.isSelected());
            }
            dispose();
        } catch (IllegalArgumentException ex) {
            result = null;
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Check your input", JOptionPane.WARNING_MESSAGE);
        }
    }
}
