package com.example.madproject;

public class FoodItem {
    private String title;
    private String price;
    private double rating;
    private int picUrl;     // Local resource ID
    private String imageUrl; // Firebase Storage URL
    private String ownerName;

    // Required empty constructor for Firestore
    public FoodItem() {}
    public String getOwnerName() {
        return ownerName; } // Allows the app to read the name from Firestore
    // Updated Constructor
    public FoodItem(String title, String price, double rating, int picUrl,String imageURL) {
        this.title = title;
        this.price = price;
        this.rating = rating;
        this.picUrl = picUrl;
        this.imageUrl = "";
    }

    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public double getRating() { return rating; }
    public int getPicUrl() { return picUrl; }

    // ADD THIS GETTER TO FIX THE ADAPTER ERROR
    public String getImageUrl() {
        return imageUrl;
    }
}