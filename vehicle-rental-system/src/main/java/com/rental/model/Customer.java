package com.rental.model;

/** A registered customer. The password is NOT stored here (only a hash in the database). */
public class Customer {

    private final int id;
    private final String name;
    private final String email;
    private final String phone;
    private final String username;

    public Customer(int id, String name, String email, String phone, String username) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.username = username;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getUsername() { return username; }

    @Override
    public String toString() { return name + " (@" + username + ")"; }
}
