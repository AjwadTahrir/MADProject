package com.example.madproject;

public class MyFoodItem {
    private String foodId;
    private String userId;
    private String title;
    private String description;
    private String price;
    private String quantity; // This acts as Initial Quantity (e.g., "4")
    private String currentQuantity; // New Field: Tracks remaining (e.g., "3")
    private String pickupTime;
    private String location;
    private String status;
    private String imageUri;
    private long timestamp;

    // 1. Empty Constructor
    public MyFoodItem() { }

    // 2. Full Constructor
    public MyFoodItem(String foodId, String userId, String title, String description, String price, String quantity, String currentQuantity, String pickupTime, String location, String status, String imageUri, long timestamp) {
        this.foodId = foodId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.currentQuantity = currentQuantity; // Initialize
        this.pickupTime = pickupTime;
        this.location = location;
        this.status = status;
        this.imageUri = imageUri;
        this.timestamp = timestamp;
    }

    // 3. Getters
    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getStatus() { return status; }
    public String getDescription() { return description; }
    public String getPickupTime() { return pickupTime; }
    public String getImageUri() { return imageUri; }
    public long getTimestamp() { return timestamp; }
    public String getLocation() { return location; }
    public String getQuantity() { return quantity; }
    public String getUserId() { return userId; }
    public String getFoodId() { return foodId; }

    // Getter for Current Quantity (Default to Initial if null for backward compatibility)
    public String getCurrentQuantity() {
        return (currentQuantity != null) ? currentQuantity : quantity;
    }

    public void setFoodId(String foodId) { this.foodId = foodId; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
}