package com.example.madproject;

public class SearchFoodItem {
    // added id, description, ownerId, pickupTime, quantity
    private String id, title, category, location, price, imageUri, description, ownerId, pickupTime, quantity;
    private double rating;

    public SearchFoodItem() {}

    // Update Constructor to accept all data
    public SearchFoodItem(String id, String title, String category, String location, String price,
                          double rating, String imageUri, String description,
                          String ownerId, String pickupTime, String quantity) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.location = location;
        this.price = price;
        this.rating = rating;
        this.imageUri = imageUri;
        this.description = description;
        this.ownerId = ownerId;
        this.pickupTime = pickupTime;
        this.quantity = quantity;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }
    public String getPrice() { return price; }
    public double getRating() { return rating; }
    public String getImageUrl() { return imageUri; }

    // Add new getters
    public String getDescription() { return description; }
    public String getOwnerId() { return ownerId; }
    public String getPickupTime() { return pickupTime; }
    public String getQuantity() { return quantity; }
}