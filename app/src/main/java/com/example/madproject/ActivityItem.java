package com.example.madproject;

public class ActivityItem {
    String action;   // e.g., "Listed new food"
    String details;  // e.g., "Fresh Vegetables"
    String time;     // e.g., "2h ago"

    public ActivityItem(String action, String details, String time) {
        this.action = action;
        this.details = details;
        this.time = time;
    }

    // Add Getters here if you use private variables
}