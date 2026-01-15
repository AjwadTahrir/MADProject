package com.example.madproject;

public class SearchFoodItem {
    private String title, category, location, price, imageUri;
    private double rating;

    public SearchFoodItem() {} // Needed for Firebase

    public SearchFoodItem(String title, String category, String location, String price, double rating, String imageUri) {
        this.title = title;
        this.category = category;
        this.location = location;
        this.price = price;
        this.rating = rating;
        this.imageUri = imageUri;
    }

    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }
    public String getPrice() { return price; }
    public double getRating() { return rating; }
    public String getImageUrl() { return imageUri; }
}