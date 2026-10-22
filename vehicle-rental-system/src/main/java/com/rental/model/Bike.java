package com.rental.model;

/** INHERITANCE: a Bike (motorcycle or scooter) is a Vehicle with engine size and gears. */
public class Bike extends Vehicle {

    private int engineCc;
    private boolean geared;

    public Bike(int id, String name, String brand, String category, double dailyRate,
                String imageFile, boolean available, int engineCc, boolean geared) {
        super(id, name, brand, category, dailyRate, imageFile, available);
        setEngineCc(engineCc);
        this.geared = geared;
    }

    public int getEngineCc() { return engineCc; }
    public boolean isGeared() { return geared; }

    public void setEngineCc(int engineCc) {
        if (engineCc < 50 || engineCc > 2000) throw new IllegalArgumentException("Engine size must be between 50 and 2000 cc.");
        this.engineCc = engineCc;
    }

    @Override
    public String getType() { return "BIKE"; }

    @Override
    public String getSpecs() { return engineCc + " cc \u2022 " + (geared ? "Geared" : "Gearless"); }

    /** METHOD OVERRIDING (POLYMORPHISM): bikes get 5% off for rentals of 5 days or more. */
    @Override
    public double calculateRent(int days) {
        double total = super.calculateRent(days);
        return days >= 5 ? round2(total * 0.95) : total;
    }
}
