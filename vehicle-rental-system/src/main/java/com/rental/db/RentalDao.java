package com.rental.db;

import com.rental.exception.RentalException;
import com.rental.model.Rental;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** DAO for rentals and payments. Booking and returning use TRANSACTIONS so data never ends up half-saved. */
public class RentalDao {

    // Shared SELECT: joins the customer name, vehicle name and payment method onto each rental row.
    private static final String SELECT_BASE =
            "SELECT r.id, r.customer_id, r.vehicle_id, r.start_date, r.days, r.end_date, r.return_date, "
            + "r.discount, r.late_fee, r.total_amount, r.status, "
            + "c.name AS customer_name, v.name AS vehicle_name, p.method AS payment_method "
            + "FROM rentals r JOIN customers c ON c.id = r.customer_id "
            + "JOIN vehicles v ON v.id = r.vehicle_id "
            + "LEFT JOIN payments p ON p.rental_id = r.id ";

    /**
     * Saves the rental, marks the vehicle unavailable and records the (demo) payment.
     * All three steps succeed together or none of them happen (commit / rollback).
     */
    public int createRentalWithPayment(Rental r, String paymentMethod) throws SQLException, RentalException {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int rentalId = insertRental(conn, r);

                String lock = "UPDATE vehicles SET available = 0 WHERE id = ? AND available = 1";
                try (PreparedStatement ps = conn.prepareStatement(lock)) {
                    ps.setInt(1, r.getVehicleId());
                    if (ps.executeUpdate() == 0) {
                        throw new RentalException("This vehicle is no longer available.");
                    }
                }

                String pay = "INSERT INTO payments (rental_id, method, amount, paid_at, status) VALUES (?,?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(pay)) {
                    ps.setInt(1, rentalId);
                    ps.setString(2, paymentMethod);
                    ps.setDouble(3, r.getTotalAmount());
                    ps.setString(4, LocalDateTime.now().toString());
                    ps.setString(5, "SIMULATED");
                    ps.executeUpdate();
                }
                conn.commit();
                return rentalId;
            } catch (SQLException | RentalException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private int insertRental(Connection conn, Rental r) throws SQLException {
        String sql = "INSERT INTO rentals (customer_id, vehicle_id, start_date, days, end_date, discount, "
                + "late_fee, total_amount, status) VALUES (?,?,?,?,?,?,0,?,'ACTIVE')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getCustomerId());
            ps.setInt(2, r.getVehicleId());
            ps.setString(3, r.getStartDate().toString());
            ps.setInt(4, r.getDays());
            ps.setString(5, r.getEndDate().toString());
            ps.setDouble(6, r.getDiscount());
            ps.setDouble(7, r.getTotalAmount());
            ps.executeUpdate();
            return Database.lastInsertId(conn);
        }
    }

    /** Marks the rental RETURNED, adds any late fee to the total and makes the vehicle available again. */
    public void completeReturn(int rentalId, LocalDate returnDate, double lateFee) throws SQLException, RentalException {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String close = "UPDATE rentals SET status = 'RETURNED', return_date = ?, late_fee = ?, "
                        + "total_amount = total_amount + ? WHERE id = ? AND status = 'ACTIVE'";
                try (PreparedStatement ps = conn.prepareStatement(close)) {
                    ps.setString(1, returnDate.toString());
                    ps.setDouble(2, lateFee);
                    ps.setDouble(3, lateFee);
                    ps.setInt(4, rentalId);
                    if (ps.executeUpdate() == 0) {
                        throw new RentalException("This rental has already been returned.");
                    }
                }
                String free = "UPDATE vehicles SET available = 1 WHERE id = (SELECT vehicle_id FROM rentals WHERE id = ?)";
                try (PreparedStatement ps = conn.prepareStatement(free)) {
                    ps.setInt(1, rentalId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException | RentalException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public Rental findById(int id) throws SQLException {
        List<Rental> list = query(SELECT_BASE + "WHERE r.id = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Rental> findAll() throws SQLException {
        return query(SELECT_BASE + "ORDER BY r.id DESC", null);
    }

    public List<Rental> findByCustomer(int customerId) throws SQLException {
        return query(SELECT_BASE + "WHERE r.customer_id = ? ORDER BY r.id DESC", customerId);
    }

    public boolean hasRentals(int vehicleId) throws SQLException {
        return count("SELECT COUNT(*) FROM rentals WHERE vehicle_id = ?", vehicleId) > 0;
    }

    public boolean hasActiveRental(int vehicleId) throws SQLException {
        return count("SELECT COUNT(*) FROM rentals WHERE vehicle_id = ? AND status = 'ACTIVE'", vehicleId) > 0;
    }

    public int countActive() throws SQLException {
        return count("SELECT COUNT(*) FROM rentals WHERE status = 'ACTIVE'", null);
    }

    public double totalRevenue() throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(SUM(total_amount), 0) FROM rentals");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    /** Runs a COUNT query that has zero or one "?" parameter. */
    private int count(String sql, Integer param) throws SQLException {
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) {
                ps.setInt(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private List<Rental> query(String sql, Integer param) throws SQLException {
        List<Rental> list = new ArrayList<>();
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) {
                ps.setInt(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    private Rental map(ResultSet rs) throws SQLException {
        Rental r = new Rental();
        r.setId(rs.getInt("id"));
        r.setCustomerId(rs.getInt("customer_id"));
        r.setVehicleId(rs.getInt("vehicle_id"));
        r.setCustomerName(rs.getString("customer_name"));
        r.setVehicleName(rs.getString("vehicle_name"));
        r.setPaymentMethod(rs.getString("payment_method"));
        r.setStartDate(LocalDate.parse(rs.getString("start_date")));
        r.setDays(rs.getInt("days"));
        r.setEndDate(LocalDate.parse(rs.getString("end_date")));
        String returned = rs.getString("return_date");
        r.setReturnDate(returned == null ? null : LocalDate.parse(returned));
        r.setDiscount(rs.getDouble("discount"));
        r.setLateFee(rs.getDouble("late_fee"));
        r.setTotalAmount(rs.getDouble("total_amount"));
        r.setStatus(rs.getString("status"));
        return r;
    }
}
