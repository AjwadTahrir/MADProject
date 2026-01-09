package com.example.madproject;

public class ActivityItem {
    String action;   // e.g., "Listed new food"
    String details;  // e.g., "Fresh Vegetables"
    String time;     // e.g., "2h ago"
    String location;

    public ActivityItem(String action, String details, String time, String location) {
        this.action = action;
        this.details = details;
        this.time = time;
        this.location = location;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

    public String getTime() {
        return time;
    }

    public String getLocation() {
        return location;
    }
}