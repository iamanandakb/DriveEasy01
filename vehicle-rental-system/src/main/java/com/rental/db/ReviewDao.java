package com.rental.db;

import com.rental.model.Review;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** DAO for customer reviews (a LIVE feature: reviews are saved in the database). */
public class ReviewDao {

    public void add(int customerId, int rating, String comment) throws SQLException {
        String sql = "INSERT INTO reviews (customer_id, rating, comment, created_at) VALUES (?,?,?,?)";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, rating);
            ps.setString(3, comment);
            ps.setString(4, LocalDateTime.now().toString());
            ps.executeUpdate();
        }
    }

    public List<Review> findAll() throws SQLException {
        String sql = "SELECT r.id, c.name, r.rating, r.comment, r.created_at FROM reviews r "
                + "JOIN customers c ON c.id = r.customer_id ORDER BY r.id DESC";
        List<Review> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Review(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getString(4), rs.getString(5)));
            }
        }
        return list;
    }
}
