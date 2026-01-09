package com.example.madproject;



    public class FoodItem {
        private String title;
        private String price;
        private double rating; //
        private int picUrl;

        // 2. Updated Constructor to accept 4 arguments
        public FoodItem(String title, String price, double rating, int picUrl) {
            this.title = title;
            this.price = price;
            this.rating = rating;
            this.picUrl = picUrl;
        }

        public String getTitle() {
            return title;
        }

        public String getPrice() {
            return price;
        }

        // 3. New Getter for Rating
        public double getRating() {
            return rating;
        }

        public int getPicUrl() {
            return picUrl;
        }
    }
