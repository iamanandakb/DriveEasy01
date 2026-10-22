package com.rental.model;

/**
 * Who is logged in right now. Every service method that needs permission checks
 * receives a UserSession, so customers can never run admin operations.
 */
public class UserSession {

    private final Role role;
    private final int customerId;   // 0 for the administrator
    private final String displayName;

    public UserSession(Role role, int customerId, String displayName) {
        this.role = role;
        this.customerId = customerId;
        this.displayName = displayName;
    }

    public Role getRole() { return role; }
    public int getCustomerId() { return customerId; }
    public String getDisplayName() { return displayName; }
    public boolean isAdmin() { return role == Role.ADMIN; }
}
