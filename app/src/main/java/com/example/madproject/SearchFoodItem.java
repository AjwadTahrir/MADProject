package com.example.madproject;

public class SearchFoodItem {
    private String title;
    private String category;   // e.g., "Meals"
    private String location;   // e.g., "Downtown (1.2mi)"
    private String price;
    private double rating;
    private int imageResId;

    public SearchFoodItem(String title, String category, String location, String price, double rating, int imageResId) {
        this.title = title;
        this.category = category;
        this.location = location;
        this.price = price;
        this.rating = rating;
        this.imageResId = imageResId;
    }

    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }
    public String getPrice() { return price; }
    public double getRating() { return rating; }
    public int getImageResId() { return imageResId; }
}
