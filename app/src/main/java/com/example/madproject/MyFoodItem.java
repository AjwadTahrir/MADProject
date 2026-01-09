package com.example.madproject;

public class MyFoodItem {
    private String title;
    private String price;
    private String status; // "active" or "sold"
    private String date;
    private int imageRes;

    public MyFoodItem(String title, String price, String status, String date, int imageRes) {
        this.title = title;
        this.price = price;
        this.status = status;
        this.date = date;
        this.imageRes = imageRes;
    }

    // Getters
    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getStatus() { return status; }
    public String getDate() { return date; }
    public int getImageRes() { return imageRes; }
}
