package com.rental.ui;

import com.rental.model.Vehicle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.function.Consumer;

/** One vehicle in the browse grid: picture, name, category, price, availability and a Book button. */
public class VehicleCard extends RoundedPanel {

    public VehicleCard(Vehicle v, Consumer<Vehicle> onBook) {
        super(18, Color.WHITE, new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 14, 10));
        setPreferredSize(new Dimension(280, 360));

        ImagePanel picture = new ImagePanel(v.getImageFile(), 14);
        picture.setPreferredSize(new Dimension(260, 160));
        add(picture, BorderLayout.NORTH);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(BorderFactory.createEmptyBorder(12, 6, 0, 6));

        JLabel category = new JLabel(v.getCategory().toUpperCase());
        category.setFont(Theme.font(Font.BOLD, 11f));
        category.setForeground(Theme.PRIMARY);
        JLabel name = new JLabel(v.getName());
        name.setFont(Theme.font(Font.BOLD, 17f));
        name.setForeground(Theme.TEXT);
        JLabel specs = new JLabel(v.getSpecs());
        specs.setFont(Theme.font(Font.PLAIN, 13f));
        specs.setForeground(Theme.MUTED);
        JLabel price = new JLabel(Vehicle.CURRENCY + String.format("%,.0f", v.getDailyRate()) + " / day");
        price.setFont(Theme.font(Font.BOLD, 18f));
        price.setForeground(Theme.TEXT);
        JLabel status = new JLabel(v.isAvailable() ? "\u25CF Available" : "\u25CF Rented out / unavailable");
        status.setFont(Theme.font(Font.BOLD, 12f));
        status.setForeground(v.isAvailable() ? Theme.SUCCESS : Theme.DANGER);

        info.add(category);
        info.add(Box.createVerticalStrut(2));
        info.add(name);
        info.add(Box.createVerticalStrut(2));
        info.add(specs);
        info.add(Box.createVerticalStrut(10));
        info.add(price);
        info.add(Box.createVerticalStrut(4));
        info.add(status);
        add(info, BorderLayout.CENTER);

        RoundedButton book = new RoundedButton(v.isAvailable() ? "Book Now" : "Unavailable", Theme.ACCENT);
        book.setEnabled(v.isAvailable());
        book.addActionListener(e -> onBook.accept(v));
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.setBorder(BorderFactory.createEmptyBorder(12, 6, 0, 6));
        south.add(book, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        Box.createVerticalGlue();
    }
}
