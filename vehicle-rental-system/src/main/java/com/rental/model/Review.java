package com.rental.model;

/** A customer review of the rental service. */
public class Review {

    private final int id;
    private final String customerName;
    private final int rating;
    private final String comment;
    private final String createdAt;

    public Review(int id, String customerName, int rating, String comment, String createdAt) {
        this.id = id;
        this.customerName = customerName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getCustomerName() { return customerName; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public String getCreatedAt() { return createdAt; }
}
