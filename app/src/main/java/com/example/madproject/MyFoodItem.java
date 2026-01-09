package com.example.madproject;

import com.google.firebase.firestore.Exclude;

public class MyFoodItem {
    private String title;
    private String price;
    private String status; // "active" or "sold"
    private String date;
    private String imageUrl; // For Firebase Storage URLs
    private int imageRes;    // Keep for local default images if needed

    // 1. CRITICAL: Empty constructor required for Firestore toObject()
    public MyFoodItem() {}

    // 2. Updated Constructor for Firebase data
    public MyFoodItem(String title, String price, String status, String date, String imageUrl) {
        this.title = title;
        this.price = price;
        this.status = status;
        this.date = date;
        this.imageUrl = imageUrl;
    }

    // 3. Getters (Firestore uses these to map database fields to this class)
    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getStatus() { return status; }
    public String getDate() { return date; }
    public String getImageUrl() { return imageUrl; }

    @Exclude // Tells Firestore not to try and save the local resource ID
    public int getImageRes() { return imageRes; }
}