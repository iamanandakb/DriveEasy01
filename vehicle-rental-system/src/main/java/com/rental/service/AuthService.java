package com.rental.service;

import com.rental.db.CustomerDao;
import com.rental.exception.RentalException;
import com.rental.model.Customer;
import com.rental.model.Role;
import com.rental.model.UserSession;

import java.sql.SQLException;

/** Login and registration rules. */
public class AuthService {

    // DEMO ONLY: hard-coded credentials are NOT suitable for a real system.
    // A real application would store administrators in the database with hashed passwords.
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin123";

    private final CustomerDao customerDao = new CustomerDao();

    public UserSession loginAdmin(String username, String password) throws RentalException {
        requireCredentials(username, password);
        if (ADMIN_USERNAME.equals(username.trim()) && ADMIN_PASSWORD.equals(password)) {
            return new UserSession(Role.ADMIN, 0, "Administrator");
        }
        throw new RentalException("Invalid administrator username or password.");
    }

    public UserSession loginCustomer(String username, String password) throws RentalException {
        requireCredentials(username, password);
        try {
            Customer c = customerDao.login(username.trim().toLowerCase(), password);
            if (c == null) {
                throw new RentalException("Invalid username or password.");
            }
            return new UserSession(Role.CUSTOMER, c.getId(), c.getName());
        } catch (SQLException e) {
            throw RentalException.fromSQL(e);
        }
    }

    /** Registers a new customer after validating every field. */
    public Customer register(String name, String email, String phone, String username, String password)
            throws RentalException {
        name = name == null ? "" : name.trim();
        email = email == null ? "" : email.trim();
        phone = phone == null ? "" : phone.trim();
        username = username == null ? "" : username.trim().toLowerCase();
        password = password == null ? "" : password;

        if (name.length() < 2) throw new RentalException("Please enter the customer's full name.");
        if (!email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) throw new RentalException("Please enter a valid email address.");
        if (!phone.matches("\\d{10}")) throw new RentalException("Phone number must be exactly 10 digits.");
        if (!username.matches("[a-z0-9_]{3,20}")) throw new RentalException("Username must be 3-20 letters, digits or underscores.");
        if (password.length() < 6) throw new RentalException("Password must be at least 6 characters.");

        try {
            if (customerDao.usernameExists(username)) throw new RentalException("That username is already taken.");
            if (customerDao.emailExists(email)) throw new RentalException("That email is already registered.");
            Customer c = new Customer(0, name, email, phone, username);
            int id = customerDao.add(c, password);
            return new Customer(id, name, email, phone, username);
        } catch (SQLException e) {
            throw RentalException.fromSQL(e);
        }
    }

    private void requireCredentials(String username, String password) throws RentalException {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new RentalException("Please enter both username and password.");
        }
    }
}
