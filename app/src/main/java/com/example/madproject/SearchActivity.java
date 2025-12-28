package com.example.madproject;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;

public class SearchActivity extends AppCompatActivity {

    RecyclerView recyclerSearch;
    SearchAdapter adapter;
    List<SearchFoodItem> searchList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. LINK TO YOUR XML LAYOUT
        // Make sure this matches the name of the XML file you created for the search page
        setContentView(R.layout.activity_search);

        // 2. INITIALIZE RECYCLERVIEW
        recyclerSearch = findViewById(R.id.recyclerSearch);
        recyclerSearch.setLayoutManager(new LinearLayoutManager(this));

        // 3. CREATE DATA (Mock data based on your screenshot)
        searchList = new ArrayList<>();
        searchList.add(new SearchFoodItem("Homemade Lasagna", "Meals", "Downtown (1.2mi)", "$8", 4.9, R.drawable.ic_launcher_background));
        searchList.add(new SearchFoodItem("Fresh Salad", "Produce", "Mission District (0.8mi)", "Free", 4.8, R.drawable.ic_launcher_background));
        searchList.add(new SearchFoodItem("Fruit Bowl", "Produce", "Sunset (2.5mi)", "Free", 4.7, R.drawable.ic_launcher_background));
        searchList.add(new SearchFoodItem("Homemade Bread", "Baked Goods", "Castro (1.8mi)", "$5", 4.9, R.drawable.ic_launcher_background));
        searchList.add(new SearchFoodItem("Leftover Pizza", "Meals", "SOMA (0.5mi)", "Free", 4.6, R.drawable.ic_launcher_background));

        // 4. SET ADAPTER (With Click Logic)
        adapter = new SearchAdapter(this, searchList, item -> {
            // This code runs when a user clicks a food item

            // Create Intent to go to Details Page
            android.content.Intent intent = new android.content.Intent(SearchActivity.this, FoodDetailsActivity.class);

            // Pass the data of the clicked item to the new screen
            intent.putExtra("FOOD_TITLE", item.getTitle());
            intent.putExtra("FOOD_PRICE", item.getPrice());
            intent.putExtra("FOOD_IMAGE", item.getImageResId());

            // Launch the screen
            startActivity(intent);
        });

        recyclerSearch.setAdapter(adapter);

            //Bottom Navigation Behaviour

        // Initialize Bottom Navigation
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Set Search selected because we are on the Search Page
        bottomNav.setSelectedItemId(R.id.nav_search);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
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