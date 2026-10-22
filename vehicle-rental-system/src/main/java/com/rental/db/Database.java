package com.rental.db;

import com.rental.model.Bike;
import com.rental.model.Car;
import com.rental.model.Customer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

/**
 * Creates the SQLite database, the tables and the sample data, and hands out JDBC connections.
 * The database file lives in the user's home folder so it works no matter where the app is started from.
 * Override the location with:  -Drental.db=C:/path/to/file.db
 */
public class Database {

    private static final String DB_FILE = System.getProperty("rental.db",
            Paths.get(System.getProperty("user.home"), ".vehicle-rental", "rental.db").toString());

    private static final String[] TABLES = {
        "CREATE TABLE IF NOT EXISTS vehicles ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "type TEXT NOT NULL CHECK (type IN ('CAR','BIKE')),"
            + "name TEXT NOT NULL, brand TEXT NOT NULL, category TEXT NOT NULL,"
            + "daily_rate REAL NOT NULL CHECK (daily_rate > 0),"
            + "seats INTEGER, fuel TEXT, engine_cc INTEGER, has_gear INTEGER,"
            + "image TEXT, available INTEGER NOT NULL DEFAULT 1)",

        "CREATE TABLE IF NOT EXISTS customers ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "name TEXT NOT NULL, email TEXT NOT NULL UNIQUE, phone TEXT NOT NULL,"
            + "username TEXT NOT NULL UNIQUE, password_hash TEXT NOT NULL)",

        "CREATE TABLE IF NOT EXISTS rentals ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "customer_id INTEGER NOT NULL REFERENCES customers(id),"
            + "vehicle_id INTEGER NOT NULL REFERENCES vehicles(id),"
            + "start_date TEXT NOT NULL,"
            + "days INTEGER NOT NULL CHECK (days BETWEEN 1 AND 30),"
            + "end_date TEXT NOT NULL, return_date TEXT,"
            + "discount REAL NOT NULL DEFAULT 0, late_fee REAL NOT NULL DEFAULT 0,"
            + "total_amount REAL NOT NULL,"
            + "status TEXT NOT NULL CHECK (status IN ('ACTIVE','RETURNED')))",

        "CREATE TABLE IF NOT EXISTS payments ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "rental_id INTEGER NOT NULL REFERENCES rentals(id),"
            + "method TEXT NOT NULL, amount REAL NOT NULL,"
            + "paid_at TEXT NOT NULL, status TEXT NOT NULL)",

        "CREATE TABLE IF NOT EXISTS reviews ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "customer_id INTEGER NOT NULL REFERENCES customers(id),"
            + "rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),"
            + "comment TEXT NOT NULL, created_at TEXT NOT NULL)",

        // The database itself refuses a second ACTIVE rental for the same vehicle.
        "CREATE UNIQUE INDEX IF NOT EXISTS idx_one_active_rental_per_vehicle "
            + "ON rentals(vehicle_id) WHERE status = 'ACTIVE'"
    };

    private Database() { }

    public static String getDbPath() {
        return DB_FILE;
    }

    /** Opens a new connection. Always use it inside try-with-resources so it gets closed. */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");   // SQLite ignores foreign keys unless asked
        }
        return conn;
    }

    /** Id of the row that was just inserted on this connection (SQLite's last_insert_rowid()). */
    public static int lastInsertId(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    /** Creates the folder, tables and (on the very first launch only) the sample data. */
    public static void initialize() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            Files.createDirectories(Paths.get(DB_FILE).toAbsolutePath().getParent());
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found. Check the Maven dependency.", e);
        } catch (IOException e) {
            throw new SQLException("Cannot create the database folder: " + e.getMessage(), e);
        }

        int version;
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            for (String sql : TABLES) {
                st.execute(sql);
            }
            try (ResultSet rs = st.executeQuery("PRAGMA user_version")) {
                version = rs.getInt(1);
            }
        }

        if (version == 0) {                       // brand-new database
            insertSampleData();
            try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
                st.execute("PRAGMA user_version = 1");   // remember that sample data was inserted
            }
        }
    }

    private static void insertSampleData() throws SQLException {
        VehicleDao vehicles = new VehicleDao();
        if (vehicles.count() == 0) {
            vehicles.add(new Car(0, "Maruti Swift", "Maruti Suzuki", "Hatchback", 1500, "swift.png", true, 5, "Petrol"));
            vehicles.add(new Car(0, "Hyundai i20", "Hyundai", "Hatchback", 1700, "i20.png", true, 5, "Petrol"));
            vehicles.add(new Car(0, "Honda City", "Honda", "Sedan", 2500, "city.png", true, 5, "Petrol"));
            vehicles.add(new Car(0, "Toyota Camry", "Toyota", "Sedan", 4500, "camry.png", true, 5, "Hybrid"));
            vehicles.add(new Car(0, "Hyundai Creta", "Hyundai", "SUV", 3200, "creta.png", true, 5, "Diesel"));
            vehicles.add(new Car(0, "Mahindra Thar", "Mahindra", "SUV", 3800, "thar.png", true, 4, "Diesel"));
            vehicles.add(new Car(0, "Toyota Fortuner", "Toyota", "SUV", 5500, "fortuner.png", true, 7, "Diesel"));
            vehicles.add(new Bike(0, "Royal Enfield Classic 350", "Royal Enfield", "Motorcycle", 1200, "classic350.png", true, 350, true));
            vehicles.add(new Bike(0, "Bajaj Pulsar 150", "Bajaj", "Motorcycle", 800, "pulsar.png", true, 150, true));
            vehicles.add(new Bike(0, "KTM Duke 200", "KTM", "Motorcycle", 1000, "duke.png", true, 200, true));
            vehicles.add(new Bike(0, "Honda Activa", "Honda", "Scooter", 500, "activa.png", true, 110, false));
            vehicles.add(new Bike(0, "TVS Jupiter", "TVS", "Scooter", 450, "jupiter.png", true, 110, false));
        }

        CustomerDao customers = new CustomerDao();
        if (customers.count() == 0) {
            customers.add(new Customer(0, "Demo Customer", "demo@example.com", "9876543210", "demo"), "demo123");
            int riya = customers.add(new Customer(0, "Riya Sharma", "riya@example.com", "9123456780", "riya"), "riya123");

            String sql = "INSERT INTO reviews (customer_id, rating, comment, created_at) VALUES (?, ?, ?, ?)";
            try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, riya);
                ps.setInt(2, 5);
                ps.setString(3, "Smooth booking and a very clean car. Will rent again!");
                ps.setString(4, LocalDateTime.now().toString());
                ps.executeUpdate();
            }
        }
    }
}
