package com.rental.db;

import com.rental.model.Bike;
import com.rental.model.Car;
import com.rental.model.Vehicle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** DAO (Data Access Object): all SQL for the "vehicles" table lives here. */
public class VehicleDao {

    public List<Vehicle> findAll() throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM vehicles ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Vehicle findById(int id) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM vehicles WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public int add(Vehicle v) throws SQLException {
        String sql = "INSERT INTO vehicles (type, name, brand, category, daily_rate, seats, fuel, "
                + "engine_cc, has_gear, image, available) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, v);
            ps.executeUpdate();
            return Database.lastInsertId(conn);
        }
    }

    public void update(Vehicle v) throws SQLException {
        String sql = "UPDATE vehicles SET type=?, name=?, brand=?, category=?, daily_rate=?, seats=?, fuel=?, "
                + "engine_cc=?, has_gear=?, image=?, available=? WHERE id=?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, v);
            ps.setInt(12, v.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM vehicles WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        return scalar("SELECT COUNT(*) FROM vehicles");
    }

    public int countAvailable() throws SQLException {
        return scalar("SELECT COUNT(*) FROM vehicles WHERE available = 1");
    }

    private int scalar(String sql) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Fills the 11 shared "?" placeholders of the INSERT and UPDATE statements. */
    private void bind(PreparedStatement ps, Vehicle v) throws SQLException {
        ps.setString(1, v.getType());
        ps.setString(2, v.getName());
        ps.setString(3, v.getBrand());
        ps.setString(4, v.getCategory());
        ps.setDouble(5, v.getDailyRate());
        if (v instanceof Car) {
            Car car = (Car) v;                       // casting: treat the Vehicle as a Car
            ps.setInt(6, car.getSeats());
            ps.setString(7, car.getFuel());
            ps.setNull(8, Types.INTEGER);
            ps.setNull(9, Types.INTEGER);
        } else {
            Bike bike = (Bike) v;
            ps.setNull(6, Types.INTEGER);
            ps.setNull(7, Types.VARCHAR);
            ps.setInt(8, bike.getEngineCc());
            ps.setInt(9, bike.isGeared() ? 1 : 0);
        }
        ps.setString(10, v.getImageFile());
        ps.setInt(11, v.isAvailable() ? 1 : 0);
    }

    /** Turns one database row into a Car or a Bike object (POLYMORPHISM: returned as Vehicle). */
    private Vehicle map(ResultSet rs) throws SQLException {
        boolean available = rs.getInt("available") == 1;
        if ("CAR".equals(rs.getString("type"))) {
            return new Car(rs.getInt("id"), rs.getString("name"), rs.getString("brand"), rs.getString("category"),
                    rs.getDouble("daily_rate"), rs.getString("image"), available,
                    rs.getInt("seats"), rs.getString("fuel"));
        }
        return new Bike(rs.getInt("id"), rs.getString("name"), rs.getString("brand"), rs.getString("category"),
                rs.getDouble("daily_rate"), rs.getString("image"), available,
                rs.getInt("engine_cc"), rs.getInt("has_gear") == 1);
    }
}
