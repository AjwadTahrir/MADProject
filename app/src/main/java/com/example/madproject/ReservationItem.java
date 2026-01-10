package com.example.madproject;

public class ReservationItem {
    private String reservationId;
    private String foodTitle;
    private String foodImage;
    private String sellerId; // <--- Critical addition
    private String buyerId;
    private int quantityReserved;
    private double totalPrice;
    private String status;
    private long timestamp;

    // 1. Empty Constructor (Required for Firestore)
    public ReservationItem() { }

    // 2. Full Constructor
    public ReservationItem(String reservationId, String foodTitle, String foodImage, String sellerId, String buyerId, int quantityReserved, double totalPrice, String status, long timestamp) {
        this.reservationId = reservationId;
        this.foodTitle = foodTitle;
        this.foodImage = foodImage;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.quantityReserved = quantityReserved;
        this.totalPrice = totalPrice;
        this.status = status;
        this.timestamp = timestamp;
    }

    // 3. Getters & Setters
    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }

    public String getFoodTitle() { return foodTitle; }
    public void setFoodTitle(String foodTitle) { this.foodTitle = foodTitle; }

    public String getFoodImage() { return foodImage; }
    public void setFoodImage(String foodImage) { this.foodImage = foodImage; }

    public String getSellerId() { return sellerId; } // <--- Getter for Seller
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }

    public int getQuantityReserved() { return quantityReserved; }
    public void setQuantityReserved(int quantityReserved) { this.quantityReserved = quantityReserved; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}