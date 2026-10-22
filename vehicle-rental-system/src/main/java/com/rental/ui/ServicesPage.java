package com.rental.ui;

import com.rental.exception.RentalException;
import com.rental.model.Review;
import com.rental.model.UserSession;
import com.rental.service.RentalService;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Additional services. Every tab carries a badge:
 *   LIVE      = really works and is stored in the database (festive promo codes, customer reviews)
 *   INFO ONLY = demonstration content, not connected to any booking or external system
 */
public class ServicesPage extends Page {

    private static final String[] TABS = {"Tour Packages", "Drivers & Guides", "Festive Deals", "Reviews", "Terms", "Outlets", "Payments"};

    private final UserSession session;
    private final RentalService service;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final StyledTable reviewTable = new StyledTable("Customer", "Rating", "Comment", "Date");
    private final JLabel averageLabel = new JLabel(" ");
    private final JComboBox<String> ratingBox = new JComboBox<>(new String[]{"5 stars", "4 stars", "3 stars", "2 stars", "1 star"});
    private final JTextField commentField = new JTextField();

    public ServicesPage(UserSession session, RentalService service) {
        this.session = session;
        this.service = service;

        PillBar tabs = new PillBar(TABS, true, label -> cards.show(content, label));
        tabs.select(TABS[0], false);

        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.add(header("Services & Information", "Extras that go with your rental. Look for the LIVE and INFO ONLY badges."), BorderLayout.NORTH);
        top.add(tabs, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        content.setOpaque(false);
        content.add(tablePage(false, "Packages are shown for demonstration only. They cannot be booked in this application.",
                new String[]{"Package", "Duration", "Suggested vehicle", "Highlights", "Indicative price"},
                new Object[][]{
                    {"Konkan Coastal Escape", "3 days", "Hyundai Creta", "Beaches, Ganpatipule temple, seafood", money(14500)},
                    {"Pune - Lonavala Weekend", "2 days", "Honda City", "Hill views, Bhushi Dam, Karla caves", money(7800)},
                    {"Mahabaleshwar Hill Getaway", "2 days", "Mahindra Thar", "Strawberry farms, Venna Lake, viewpoints", money(9500)},
                    {"Ajanta - Ellora Heritage Circuit", "4 days", "Toyota Fortuner", "UNESCO cave temples, Daulatabad Fort", money(27500)},
                    {"Goa Beach Road Trip", "5 days", "Royal Enfield Classic 350", "Coastal highways, beach hopping", money(11000)}}), TABS[0]);
        content.add(tablePage(false, "Sample guides for demonstration only. Hiring a driver or guide is not part of the booking flow.",
                new String[]{"Name", "Languages", "Experience", "Speciality", "Rate per day"},
                new Object[][]{
                    {"Ramesh Kulkarni", "Marathi, Hindi, English", "12 years", "Highway driving, family trips", money(1200)},
                    {"Sneha Patil", "Marathi, Hindi, English", "6 years", "Heritage and fort tours", money(1500)},
                    {"Imran Shaikh", "Hindi, Urdu, English", "9 years", "Hill roads and off-road routes", money(1400)},
                    {"Joseph D'Souza", "Konkani, English, Hindi", "8 years", "Goa and coastal routes", money(1350)}}), TABS[1]);
        content.add(festivePage(), TABS[2]);
        content.add(reviewsPage(), TABS[3]);
        content.add(termsPage(), TABS[4]);
        content.add(tablePage(false, "Sample outlets for demonstration only. There is no GPS or map search in this application.",
                new String[]{"Outlet", "City", "Address (sample)", "Phone (sample)", "Opening hours"},
                new Object[][]{
                    {"DriveEasy Central", "Solapur", "12 Station Road", "0217-555-0101", "8 AM - 9 PM"},
                    {"DriveEasy Pune Airport", "Pune", "Arrivals Level, Lohegaon", "020-5550-0102", "24 hours"},
                    {"DriveEasy Mumbai Andheri", "Mumbai", "45 Link Road, Andheri West", "022-5550-0103", "7 AM - 10 PM"},
                    {"DriveEasy Kolhapur", "Kolhapur", "8 Rajarampuri Main Road", "0231-555-0104", "8 AM - 8 PM"}}), TABS[5]);
        content.add(tablePage(false, "Payments are SIMULATED. No money is charged and no payment gateway is connected. Your choice is only recorded.",
                new String[]{"Method", "Used for", "In this application"},
                new Object[][]{
                    {"UPI (Demo)", "Instant payment", "Simulated - choice stored with the booking"},
                    {"Credit/Debit Card (Demo)", "Card payment", "Simulated - no card details are asked or stored"},
                    {"Net Banking (Demo)", "Bank transfer", "Simulated - choice stored with the booking"},
                    {"Cash at Pickup", "Pay at the outlet", "Simulated - recorded as the chosen method"}}), TABS[6]);
        add(content, BorderLayout.CENTER);
    }

    private static String money(double amount) {
        return Theme.money(amount);
    }

    /** Banner on top of a tab: green LIVE or amber INFO ONLY badge + one sentence. */
    private JPanel banner(boolean live, String text) {
        RoundedPanel panel = new RoundedPanel(12, live ? new Color(0xDCFCE7) : new Color(0xFEF3C7), new BorderLayout(12, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel badge = new JLabel(live ? "LIVE" : "INFO ONLY");
        badge.setFont(Theme.font(Font.BOLD, 12f));
        badge.setForeground(live ? Theme.SUCCESS : Theme.WARNING);
        JLabel label = new JLabel(text);
        label.setFont(Theme.font(Font.PLAIN, 13f));
        label.setForeground(Theme.TEXT);
        panel.add(badge, BorderLayout.WEST);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JPanel tablePage(boolean live, String bannerText, String[] columns, Object[][] data) {
        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setOpaque(false);
        StyledTable table = new StyledTable(columns);
        List<Object[]> rows = new ArrayList<>();
        for (Object[] row : data) rows.add(row);
        table.setRows(rows);
        page.add(banner(live, bannerText), BorderLayout.NORTH);
        page.add(table.wrap(), BorderLayout.CENTER);
        return page;
    }

    /** Festive deals: the promo codes shown here really work in the booking dialog. */
    private JPanel festivePage() {
        String[] occasions = {"New customers", "Festive season", "Diwali special"};
        Object[][] data = new Object[RentalService.PROMO_CODES.size()][];
        int i = 0;
        for (Map.Entry<String, Double> entry : RentalService.PROMO_CODES.entrySet()) {
            data[i] = new Object[]{entry.getKey(), entry.getValue().intValue() + "% off", occasions[i % occasions.length],
                    "Type the code in the \"Promo code\" box when booking and press Apply"};
            i++;
        }
        return tablePage(true, "These promo codes are real: they are applied to the total when you book.",
                new String[]{"Code", "Discount", "Occasion", "How to use"}, data);
    }

    private JPanel termsPage() {
        JTextArea text = new JTextArea(
                "1. Eligibility: The renter must hold a valid driving licence for the vehicle category and be at least 18 years old.\n\n"
              + "2. Rental period: Rentals run from 1 to 30 days. The vehicle is due back at the end of the booked period.\n\n"
              + "3. Pricing: Cars get 10% off for 7 days or more, bikes get 5% off for 5 days or more. Promo codes are applied on top.\n\n"
              + "4. Late returns: Every day after the due date is charged one extra daily rate and added to the final total.\n\n"
              + "5. Early returns: Returning early does not reduce the amount charged.\n\n"
              + "6. Condition: The vehicle must be returned in the condition it was delivered. Damage costs are the renter's responsibility.\n\n"
              + "7. Fuel and fines: Fuel, tolls and traffic fines during the rental are paid by the renter.\n\n"
              + "8. Payments: Payments in this academic demo are simulated.\n\n"
              + "(Sample terms written for a college project - not a legal document.)");
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setFont(Theme.font(Font.PLAIN, 14f));
        text.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        JScrollPane scroll = new JScrollPane(text);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setOpaque(false);
        page.add(banner(false, "Sample rental terms that match the rules this application applies."), BorderLayout.NORTH);
        page.add(scroll, BorderLayout.CENTER);
        return page;
    }

    private JPanel reviewsPage() {
        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setOpaque(false);

        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setOpaque(false);
        north.add(banner(true, "Reviews are saved in the database and visible to everyone."), BorderLayout.NORTH);
        averageLabel.setFont(Theme.font(Font.BOLD, 15f));
        north.add(averageLabel, BorderLayout.SOUTH);
        page.add(north, BorderLayout.NORTH);
        page.add(reviewTable.wrap(), BorderLayout.CENTER);

        if (!session.isAdmin()) {                      // only customers can write reviews
            Theme.styleField(commentField);
            RoundedButton submit = new RoundedButton("Post Review", Theme.PRIMARY);
            submit.addActionListener(e -> postReview());
            JPanel form = new JPanel(new BorderLayout(10, 0));
            form.setOpaque(false);
            form.add(ratingBox, BorderLayout.WEST);
            form.add(commentField, BorderLayout.CENTER);
            form.add(submit, BorderLayout.EAST);
            page.add(form, BorderLayout.SOUTH);
        }
        return page;
    }

    private void postReview() {
        try {
            int rating = 5 - ratingBox.getSelectedIndex();
            service.addReview(session, rating, commentField.getText());
            commentField.setText("");
            loadReviews();
            JOptionPane.showMessageDialog(this, "Thank you for your review!", "Review posted", JOptionPane.INFORMATION_MESSAGE);
        } catch (RentalException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot post review", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void loadReviews() {
        try {
            List<Review> reviews = service.getReviews();
            List<Object[]> rows = new ArrayList<>();
            int sum = 0;
            for (Review r : reviews) {
                sum += r.getRating();
                String date = r.getCreatedAt().length() >= 10 ? r.getCreatedAt().substring(0, 10) : r.getCreatedAt();
                rows.add(new Object[]{r.getCustomerName(), "\u2605".repeat(r.getRating()) + "\u2606".repeat(5 - r.getRating()),
                        r.getComment(), date});
            }
            reviewTable.setRows(rows);
            averageLabel.setText(reviews.isEmpty() ? "No reviews yet"
                    : String.format("Average rating: %.1f / 5  (%d review%s)", (double) sum / reviews.size(),
                        reviews.size(), reviews.size() == 1 ? "" : "s"));
        } catch (RentalException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void refresh() {
        loadReviews();
    }
}
