package com.example.madproject;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerFoods;
    FoodAdapter adapter;
    List<FoodItem> foodList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            //Food Card

        // 1. Initialize RecyclerView
        recyclerFoods = findViewById(R.id.recyclerFeaturedFoods);
        // 2. Set Layout Manager (Grid with 2 columns)
        recyclerFoods.setLayoutManager(new GridLayoutManager(this, 2));

        // 3. Create Data
        foodList = new ArrayList<>();
        foodList.add(new FoodItem("Fresh Salad", "Free", 4.90, R.drawable.ic_launcher_background)); // Replace with real drawable
        foodList.add(new FoodItem("Homemade Pizza", "$5", 4.8, R.drawable.ic_launcher_background));
        foodList.add(new FoodItem("Fruit Bowl", "Free", 4.7, R.drawable.ic_launcher_background));
        foodList.add(new FoodItem("Sandwich", "$3", 4.6, R.drawable.ic_launcher_background));

        // 4. Set Adapter
        adapter = new FoodAdapter(foodList, item -> {
            // This runs when a user clicks a card on the Home Page

            Intent intent = new Intent(MainActivity.this, FoodDetailsActivity.class);

            // Pass the data to the Details Page
            intent.putExtra("FOOD_TITLE", item.getTitle());
            intent.putExtra("FOOD_PRICE", item.getPrice());
            intent.putExtra("FOOD_IMAGE", item.getPicUrl()); // Ensure this matches your getter name

            startActivity(intent);
        });
        recyclerFoods.setAdapter(adapter);

            //Recent Activities

        // 1. Find the RecyclerView (Make sure you added it to XML first! See below)
        RecyclerView recyclerActivity = findViewById(R.id.recyclerRecentActivity);

        // 2. Set Layout Manager (Linear, Vertical)
        recyclerActivity.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        // 3. Create Data
        List<ActivityItem> activityList = new ArrayList<>();
        activityList.add(new ActivityItem("Listed new food", "Fresh Vegetables", "2h ago", "1600 Amphitheatre Parkway, Mountain View, CA"));
        activityList.add(new ActivityItem("Received request", "Leftover Pizza", "5h ago", "1 Infinite Loop, Cupertino, CA"));
        activityList.add(new ActivityItem("Completed sharing", "Homemade Bread", "1d ago", "350 5th Ave, New York, NY"));

        // 4. Set Adapter
        ActivityAdapter activityAdapter = new ActivityAdapter(activityList);
        recyclerActivity.setAdapter(activityAdapter);

        // 5. CRITICAL FIX for scrolling
        recyclerActivity.setNestedScrollingEnabled(false);

            //Bottom Navigation Behaviour

        // Initialize Bottom Navigation
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Set Home selected because we are on the Home Page
        bottomNav.setSelectedItemId(R.id.nav_home);

        // Perform ItemSelectedListener
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_search) {
                startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_foods) {
                startActivity(new Intent(getApplicationContext(), FoodsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}