package com.rental.service;

import com.rental.db.CustomerDao;
import com.rental.db.RentalDao;
import com.rental.db.ReviewDao;
import com.rental.db.VehicleDao;
import com.rental.exception.RentalException;
import com.rental.model.Bike;
import com.rental.model.Car;
import com.rental.model.Customer;
import com.rental.model.Rental;
import com.rental.model.Review;
import com.rental.model.UserSession;
import com.rental.model.Vehicle;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Business logic layer. The GUI calls this class; this class applies the rules
 * (validation, permissions, price calculation) and then calls the DAO classes.
 */
public class RentalService {

    public static final int MAX_DAYS = 30;
    public static final String[] PAYMENT_METHODS =
            {"UPI (Demo)", "Credit/Debit Card (Demo)", "Net Banking (Demo)", "Cash at Pickup"};

    /** Promo codes -> percentage off. (COLLECTIONS: a Map keeps insertion order with LinkedHashMap.) */
    public static final Map<String, Double> PROMO_CODES = new LinkedHashMap<>();
    static {
        PROMO_CODES.put("WELCOME5", 5.0);
        PROMO_CODES.put("FESTIVE10", 10.0);
        PROMO_CODES.put("DIWALI15", 15.0);
    }

    private final VehicleDao vehicleDao = new VehicleDao();
    private final CustomerDao customerDao = new CustomerDao();
    private final RentalDao rentalDao = new RentalDao();
    private final ReviewDao reviewDao = new ReviewDao();

    // ---------------------------------------------------------------- vehicles

    public List<Vehicle> getAllVehicles() throws RentalException {
        try {
            return vehicleDao.findAll();
        } catch (SQLException e) {
            throw RentalException.fromSQL(e);
        }
    }

    public int addVehicle(UserSession session, Vehicle v) throws RentalException {
        requireAdmin(session);
        validateVehicle(v);
        try {
            return vehicleDao.add(v);
        } catch (SQLException e) {
            throw RentalException.fromSQL(e);
        }
    }

    public void updateVehicle(UserSession session, Vehicle v) throws RentalException {
        requireAdmin(session);
        validateVehicle(v);
        try {
            if (rentalDao.hasActiveRental(v.getId())) {
                v.setAvailable(false);          // a vehicle that is out on rent can never be marked available
            }
            vehicleDao.update(v);
        } catch (SQLException e) {
            throw RentalException.fromSQL(e);
        }
    }

    public void deleteVehicle(UserSession session, int vehicleId) throws RentalException {
        requireAdmin(session);
        try {
            if (rentalDao.hasRentals(vehicleId)) {
                throw new RentalException("This vehicle has rental history and cannot be deleted.\n"
                        + "Edit it and untick \"Available\" instead.");
            }
            vehicleDao.delete(vehicleId);
        } catch (SQLException e) {
            throw RentalException.fromSQL(e);
        }
    }

    private void validateVehicle(Vehicle v) throws RentalException {
        if (v == null) throw new RentalException("No vehicle data supplied.");
        List<String> allowed = Arrays.asList(v instanceof Car ? Vehicle.CAR_CATEGORIES : Vehicle.BIKE_CATEGORIES);
        if (!allowed.contains(v.getCategory())) throw new RentalException("Invalid category for this vehicle type.");
        if (v.getImageFile() == null || v.getImageFile().isEmpty()) throw new RentalException("Please choose a vehicle image.");
        if (v instanceof Car && ((Car) v).getFuel() == null) throw new RentalException("Please choose a fuel type.");
    }

    // ---------------------------------------------------------------- pricing

    /**
     * Calculates the price. Returns {standard price, long-rental discount, promo discount, total}.
     * vehicle.calculateRent(days) is POLYMORPHIC: Car and Bike apply different discounts.
     */
    public double[] quote(Vehicle v, int days, String promoCode) throws RentalException {
        if (days < 1 || days > MAX_DAYS) {
            throw new RentalException("Rental duration must be between 1 and " + MAX_DAYS + " days.");
        }
        double standard = Math.round(v.getDailyRate() * days * 100.0) / 100.0;
        double afterVolume = v.calculateRent(days);
        double percent = promoPercent(promoCode);
        double promoDiscount = Math.round(afterVolume * percent) / 100.0;
        double total = Math.round((afterVolume - promoDiscount) * 100.0) / 100.0;
        double volumeDiscount = Math.round((standard - afterVolume) * 100.0) / 100.0;
        return new double[]{standard, volumeDiscount, promoDiscount, total};
    }

    private double promoPercent(String code) throws RentalException {
        if (code == null || code.trim().isEmpty()) return 0;
        Double percent = PROMO_CODES.get(code.trim().toUpperCase());
        if (percent == null) throw new RentalException("\"" + code.trim() + "\" is not a valid promo code.");
        return percent;
    }

    // ---------------------------------------------------------------- rentals

    /** Creates a rental. Customers may only rent for themselves; the administrator may rent for anyone. */
    public Rental rentVehicle(UserSession session, int customerId, int vehicleId, int days,
                              String paymentMethod, String promoCode) throws RentalException {
        requireLogin(session);
        if (!session.isAdmin() && session.getCustomerId() != customerId) {
            throw new RentalException("Access denied: you can only book vehicles for your own account.");
        }
        if (!Arrays.asList(PAYMENT_METHODS).contains(paymentMethod)) {
            throw new RentalException("Please select a payment method.");
        }
        try {
            Vehicle v = vehicleDao.findById(vehicleId);
            if (v == null) throw new RentalException("Vehicle not found.");
            if (!v.isAvailable()) throw new RentalException(v.getName() + " is not available for rent right now.");
            if (customerDao.findById(customerId) == null) throw new RentalException("Customer not found.");

            double[] q = quote(v, days, promoCode);      // also validates the number of days

            Rental r = new Rental();
            r.setCustomerId(customerId);
            r.setVehicleId(vehicleId);
            r.setStartDate(LocalDate.now());
            r.setDays(days);
            r.setEndDate(LocalDate.now().plusDays(days));
            r.setDiscount(q[1] + q[2]);
            r.setTotalAmount(q[3]);

            int id = rentalDao.createRentalWithPayment(r, paymentMethod);
            return rentalDao.findById(id);
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                throw new RentalException("This vehicle already has an active rental.");
            }
            throw RentalException.fromSQL(e);
        }
    }

    /** Returns a vehicle. A customer can only return their OWN rentals; the administrator can return any. */
    public Rental returnVehicle(UserSession session, int rentalId) throws RentalException {
        requireLogin(session);
        try {
            Rental r = rentalDao.findById(rentalId);
            if (r == null) throw new RentalException("Rental not found.");
            if (!session.isAdmin() && r.getCustomerId() != session.getCustomerId()) {
                throw new RentalException("Access denied: you can only return your own rentals.");
            }
            if (!r.isActive()) throw new RentalException("This rental has already been returned.");

            rentalDao.completeReturn(rentalId, LocalDate.now(), calculateLateFee(r));
            return rentalDao.findById(rentalId);
        } catch (SQLException e) {
            throw RentalException.fromSQL(e);
        }
    }

    /** Late fee = number of days after the due date x the vehicle's daily rate. */
    public double calculateLateFee(Rental r) throws RentalException {
        try {
            long lateDays = ChronoUnit.DAYS.between(r.getEndDate(), LocalDate.now());
            if (lateDays <= 0) return 0;
            Vehicle v = vehicleDao.findById(r.getVehicleId());
            return v == null ? 0 : Math.round(lateDays * v.getDailyRate() * 100.0) / 100.0;
        } catch (SQLException e) {
            throw RentalException.fromSQL(e);
        }
    }

    /** Administrators see every rental; customers see only their own. */
    public List<Rental> getRentals(UserSession session) throws RentalException {
        requireLogin(session);
        try {
            return session.isAdmin() ? rentalDao.findAll() : rentalDao.findByCustomer(session.getCustomerId());
        } catch (SQLException e) {
            throw RentalException.fromSQL(e);
        }
    }

    // ---------------------------------------------------------------- admin data

    public List<Customer> getCustomers(UserSession session) throws RentalException {
        requireAdmin(session);
        try {
            return customerDao.findAll();
        } catch (SQLException e) {
            throw RentalException.fromSQL(e);
        }
    }

    /** Dashboard numbers for the administrator. */
    public Map<String, Integer> getStats(UserSession session) throws RentalException {
        requireAdmin(session);
        try {
            Map<String, Integer> stats = new LinkedHashMap<>();
            stats.put("Total Vehicles", vehicleDao.count());
            stats.put("Available Vehicles", vehicleDao.countAvailable());
            stats.put("Active Rentals", rentalDao.countActive());
            stats.put("Registered Customers", customerDao.count());
            return stats;
        } catch (SQLException e) {
            throw RentalException.fromSQL(e);
        }
    }

    public double getTotalRevenue(UserSession session) throws RentalException {
        requireAdmin(session);
        try {
            return rentalDao.totalRevenue();
        } catch (SQLException e) {
            throw RentalException.fromSQL(e);
        }
    }

    // ---------------------------------------------------------------- reviews

    public List<Review> getReviews() throws RentalException {
        try {
            return reviewDao.findAll();
        } catch (SQLException e) {
            throw RentalException.fromSQL(e);
        }
    }

    public void addReview(UserSession session, int rating, String comment) throws RentalException {
        requireLogin(session);
        if (session.isAdmin()) throw new RentalException("Only customers can write reviews.");
        if (rating < 1 || rating > 5) throw new RentalException("Rating must be between 1 and 5 stars.");
        if (comment == null || comment.trim().length() < 5) throw new RentalException("Please write at least 5 characters.");
        if (comment.trim().length() > 300) throw new RentalException("Reviews can be at most 300 characters.");
        try {
            reviewDao.add(session.getCustomerId(), rating, comment.trim());
        } catch (SQLException e) {
            throw RentalException.fromSQL(e);
        }
    }

    // ---------------------------------------------------------------- permission checks

    private void requireLogin(UserSession session) throws RentalException {
        if (session == null) throw new RentalException("You must be logged in.");
    }

    private void requireAdmin(UserSession session) throws RentalException {
        if (session == null || !session.isAdmin()) {
            throw new RentalException("Access denied: administrator permission required.");
        }
    }
}
