package com.rental.exception;

import java.sql.SQLException;

/**
 * Custom CHECKED exception for business-rule problems (invalid input, vehicle not
 * available, access denied...). The message is always safe to show to the user.
 */
public class RentalException extends Exception {

    public RentalException(String message) {
        super(message);
    }

    public RentalException(String message, Throwable cause) {
        super(message, cause);
    }

    /** Converts a low-level SQLException into a friendly message (details go to the console). */
    public static RentalException fromSQL(SQLException e) {
        System.err.println("Database error: " + e.getMessage());
        return new RentalException("A database error occurred. Please try again.\n(" + e.getMessage() + ")", e);
    }
}
