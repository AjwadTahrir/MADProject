package com.example.madproject;

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
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;

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

        // 1. Initialize Firebase and UI Views
        fStore = FirebaseFirestore.getInstance();
        etSearch = findViewById(R.id.etSearch);
        tvCount = findViewById(R.id.tvCount);
        recyclerSearch = findViewById(R.id.recyclerSearch);

        // 2. Setup RecyclerView
        recyclerSearch.setLayoutManager(new LinearLayoutManager(this));
        searchList = new ArrayList<>();

        // 3. Setup Adapter with click listener
        adapter = new SearchAdapter(this, searchList, item -> {
            Intent intent = new Intent(SearchActivity.this, FoodDetailsActivity.class);
            intent.putExtra("FOOD_TITLE", item.getTitle());
            intent.putExtra("FOOD_PRICE", item.getPrice());
            intent.putExtra("FOOD_IMAGE_URL", item.getImageUrl());
            startActivity(intent);
        });
        recyclerSearch.setAdapter(adapter);

        // 4. Load initial data
        fetchFoodsFromFirebase("");

        // 5. Setup Real-time Search Listener
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

        // 6. Setup Bottom Navigation (Crucial for button clicks!)
        setupBottomNavigation();
    }

    private void fetchFoodsFromFirebase(String searchText) {
        // Get all foods from the collection
        fStore.collection("foods").addSnapshotListener((value, error) -> {
            if (error != null) return;

            if (value != null) {
                searchList.clear();
                for (DocumentSnapshot doc : value) {
                    String title = doc.getString("title");

                    // CLIENT-SIDE FILTERING: Check if the title contains the search text
                    if (searchText.isEmpty() || (title != null && title.toLowerCase().contains(searchText.toLowerCase()))) {
                        searchList.add(new SearchFoodItem(
                                title,
                                doc.getString("category"),
                                doc.getString("location"),
                                doc.getString("price"),
                                doc.getDouble("rating") != null ? doc.getDouble("rating") : 0.0,
                                doc.getString("imageUri")
                        ));
                    }
                }
                adapter.notifyDataSetChanged();
                tvCount.setText(searchList.size() + " foods found");
            }
        });
    }

    private void setupBottomNavigation() {
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Set current tab as selected
        bottomNav.setSelectedItemId(R.id.nav_search);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_search) {
                // We are already here
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