package com.rental.model;

/** INTERFACE: anything that can be charged for a number of rental days. */
public interface Billable {
    double calculateRent(int days);
}
