package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import android.view.View; // Needed for View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

public class SearchActivity extends AppCompatActivity {

    private RecyclerView recyclerSearch;
    private SearchAdapter adapter;
    private List<SearchFoodItem> searchList;
    private FirebaseFirestore fStore;
    private EditText etSearch;
    private TextView tvCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        fStore = FirebaseFirestore.getInstance();
        etSearch = findViewById(R.id.etSearch);
        tvCount = findViewById(R.id.tvCount);
        recyclerSearch = findViewById(R.id.recyclerSearch);

        recyclerSearch.setLayoutManager(new LinearLayoutManager(this));
        searchList = new ArrayList<>();

        // --- CLICK LISTENER ---
        adapter = new SearchAdapter(this, searchList, item -> {
            Intent intent = new Intent(SearchActivity.this, FoodDetailsActivity.class);

            // Pass ALL data required by FoodDetailsActivity
            intent.putExtra("FOOD_ID", item.getId());
            intent.putExtra("FOOD_TITLE", item.getTitle());
            intent.putExtra("FOOD_PRICE", item.getPrice());
            intent.putExtra("FOOD_IMAGE_URI", item.getImageUrl());
            intent.putExtra("FOOD_LOCATION", item.getLocation());
            intent.putExtra("FOOD_DESC", item.getDescription());
            intent.putExtra("FOOD_OWNER_ID", item.getOwnerId()); // This will now contain the correct ID
            intent.putExtra("FOOD_PICKUP_TIME", item.getPickupTime());
            intent.putExtra("FOOD_QUANTITY_INITIAL", item.getQuantity());
            intent.putExtra("FOOD_QUANTITY_CURRENT", item.getQuantity());

            startActivity(intent);
        });

        recyclerSearch.setAdapter(adapter);

        fetchFoodsFromFirebase("");

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                fetchFoodsFromFirebase(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        setupBottomNavigation();
    }

    private void fetchFoodsFromFirebase(String searchText) {
        fStore.collection("foods").addSnapshotListener((value, error) -> {
            if (error != null) return;

            if (value != null) {
                searchList.clear();
                for (DocumentSnapshot doc : value) {
                    String title = doc.getString("title");

                    if (searchText.isEmpty() || (title != null && title.toLowerCase().contains(searchText.toLowerCase()))) {

                        // --- THE FIX IS BELOW ---
                        searchList.add(new SearchFoodItem(
                                doc.getId(),
                                title,
                                doc.getString("category"),
                                doc.getString("location"),
                                doc.getString("price"),
                                doc.getDouble("rating") != null ? doc.getDouble("rating") : 0.0,
                                doc.getString("imageUri"),
                                doc.getString("description"),

                                // CHANGE "ownerId" TO "userId"
                                // Your database field is named "userId", so we must read that.
                                doc.getString("userId"),

                                doc.getString("pickupTime"),
                                doc.getString("quantity")
                        ));
                    }
                }
                adapter.notifyDataSetChanged();
                tvCount.setText(searchList.size() + " foods found");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Make Status Bar Text Dark (Black) for visibility on white background
        if (getWindow() != null) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(getResources().getColor(android.R.color.white));
        }
    }

    private void setupBottomNavigation() {
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_search);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_search) {
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