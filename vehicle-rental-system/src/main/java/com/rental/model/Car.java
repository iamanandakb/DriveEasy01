package com.rental.model;

/** INHERITANCE: a Car is a Vehicle with seats and a fuel type. */
public class Car extends Vehicle {

    private int seats;
    private String fuel;

    public Car(int id, String name, String brand, String category, double dailyRate,
               String imageFile, boolean available, int seats, String fuel) {
        super(id, name, brand, category, dailyRate, imageFile, available);
        setSeats(seats);
        this.fuel = fuel;
    }

    public int getSeats() { return seats; }
    public String getFuel() { return fuel; }

    public void setSeats(int seats) {
        if (seats < 2 || seats > 9) throw new IllegalArgumentException("Seats must be between 2 and 9.");
        this.seats = seats;
    }

    @Override
    public String getType() { return "CAR"; }

    @Override
    public String getSpecs() { return seats + " seats \u2022 " + fuel; }

    /** METHOD OVERRIDING (POLYMORPHISM): cars get 10% off for rentals of 7 days or more. */
    @Override
    public double calculateRent(int days) {
        double total = super.calculateRent(days);
        return days >= 7 ? round2(total * 0.90) : total;
    }
}
