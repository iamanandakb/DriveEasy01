package com.rental.model;

/**
 * ABSTRACT base class for every rentable vehicle.
 * Fields are private (ENCAPSULATION) and reached through getters/setters,
 * and the setters validate their input.
 */
public abstract class Vehicle implements Billable {

    public static final String CURRENCY = "\u20B9"; // rupee sign
    public static final String[] CAR_CATEGORIES = {"Hatchback", "Sedan", "SUV"};
    public static final String[] BIKE_CATEGORIES = {"Motorcycle", "Scooter"};

    private int id;
    private String name;
    private String brand;
    private String category;
    private double dailyRate;
    private String imageFile;
    private boolean available;

    // CONSTRUCTOR: used by Car and Bike through super(...)
    protected Vehicle(int id, String name, String brand, String category,
                      double dailyRate, String imageFile, boolean available) {
        this.id = id;
        setName(name);
        setBrand(brand);
        this.category = category;
        setDailyRate(dailyRate);
        this.imageFile = imageFile;
        this.available = available;
    }

    /** ABSTRACT methods: every subclass must provide its own version. */
    public abstract String getType();   // "CAR" or "BIKE"
    public abstract String getSpecs();  // short description shown on cards

    /** Default price = daily rate x days. Subclasses OVERRIDE this to add discounts. */
    @Override
    public double calculateRent(int days) {
        return round2(dailyRate * days);
    }

    protected static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getCategory() { return category; }
    public double getDailyRate() { return dailyRate; }
    public String getImageFile() { return imageFile; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Vehicle name cannot be empty.");
        this.name = name.trim();
    }

    public void setBrand(String brand) {
        if (brand == null || brand.trim().isEmpty()) throw new IllegalArgumentException("Brand cannot be empty.");
        this.brand = brand.trim();
    }

    public void setDailyRate(double dailyRate) {
        if (dailyRate <= 0 || dailyRate > 100000) throw new IllegalArgumentException("Daily rate must be between 1 and 100000.");
        this.dailyRate = dailyRate;
    }

    @Override
    public String toString() {
        return name + " - " + CURRENCY + (int) dailyRate + "/day";
    }
}
