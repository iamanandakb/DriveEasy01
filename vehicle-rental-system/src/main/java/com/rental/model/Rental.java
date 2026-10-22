package com.rental.model;

import java.time.LocalDate;

/** One booking: which customer rented which vehicle, for how long, and what it cost. */
public class Rental {

    public static final String ACTIVE = "ACTIVE";
    public static final String RETURNED = "RETURNED";

    private int id;
    private int customerId;
    private int vehicleId;
    private int days;
    private String customerName;
    private String vehicleName;
    private String paymentMethod;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;      // date the vehicle is due back
    private LocalDate returnDate;   // null until the vehicle is returned
    private double discount;
    private double lateFee;
    private double totalAmount;

    public boolean isActive() { return ACTIVE.equals(status); }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }
    public int getDays() { return days; }
    public void setDays(int days) { this.days = days; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }
    public double getLateFee() { return lateFee; }
    public void setLateFee(double lateFee) { this.lateFee = lateFee; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
}
