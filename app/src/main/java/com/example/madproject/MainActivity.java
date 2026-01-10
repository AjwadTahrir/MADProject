package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerFoods;
    MyFoodsAdapter adapter;
    List<MyFoodItem> foodList;
    TextView tvHello, tvTotalFoodsCount, tvMealsSavedCount;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- 1. SETUP VIEW ELEMENTS ---
        tvHello = findViewById(R.id.tvHello);
        tvTotalFoodsCount = findViewById(R.id.tvTotalFoodsCount);
        tvMealsSavedCount = findViewById(R.id.tvMealsSavedCount);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        if (fAuth.getCurrentUser() != null) {
            userId = fAuth.getCurrentUser().getUid();
            listenToUserData();   // Real-time listener for Name & Meals Saved
            listenToFoodCount();  // Real-time listener for Total Foods Posted
        }

        // --- 2. MAIN FOOD FEED (Marketplace) ---
        recyclerFoods = findViewById(R.id.recyclerFeaturedFoods);
        recyclerFoods.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        foodList = new ArrayList<>();
        adapter = new MyFoodsAdapter(this, foodList);
        recyclerFoods.setAdapter(adapter);

        loadOtherPeoplesFoods();

        // --- 3. NAVIGATION ---
        setupBottomNavigation();
    }

    // --- REAL-TIME USER DATA LISTENER ---
    private void listenToUserData() {
        DocumentReference documentReference = fStore.collection("users").document(userId);

        // This listener fires automatically whenever the database changes!
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("MainActivity", "Error listening to user data", error);
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    // 1. Update Name
                    String fullName = documentSnapshot.getString("fullName");
                    tvHello.setText("Hello, " + (fullName != null ? fullName : "User") + "!");

                    // 2. Update Meals Saved (This updates instantly when you slide redeem!)
                    Long mealsSaved = documentSnapshot.getLong("mealsSaved");
                    tvMealsSavedCount.setText(mealsSaved != null ? String.valueOf(mealsSaved) : "0");
                }
            }
        });
    }

    // --- REAL-TIME FOOD COUNT LISTENER ---
    private void listenToFoodCount() {
        // Counts how many foods *I* have posted
        fStore.collection("foods")
                .whereEqualTo("userId", userId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) return;
                        if (value != null) {
                            int myFoodCount = value.size();
                            tvTotalFoodsCount.setText(String.valueOf(myFoodCount));
                        }
                    }
                });
    }

    private void loadOtherPeoplesFoods() {
        // Loads foods posted by OTHER people (Marketplace logic)
        fStore.collection("foods")
                .whereEqualTo("status", "active") // Only active foods
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("Firestore", "Error loading foods", error);
                            return;
                        }

                        foodList.clear();
                        if (value != null) {
                            for (QueryDocumentSnapshot doc : value) {
                                MyFoodItem item = doc.toObject(MyFoodItem.class);
                                item.setFoodId(doc.getId());

                                // Filter: Don't show my own food in the "Featured" feed
                                if (item.getUserId() != null && !item.getUserId().equals(userId)) {
                                    foodList.add(item);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) return true;
            else if (itemId == R.id.nav_search) {
                startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_foods) {
                // Navigate to "My Reservations" or "My Foods" depending on your flow
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