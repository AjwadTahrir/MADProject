package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FoodsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    MyFoodsAdapter adapter;
    List<MyFoodItem> foodList;
    TextView tvActiveCount; // Subtitle text for counting active foods

    FirebaseFirestore fStore;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foods);

        // 1. Init Firebase
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        // 2. Setup Views
        tvActiveCount = findViewById(R.id.tvActiveCount);
        recyclerView = findViewById(R.id.recyclerMyFoods);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        foodList = new ArrayList<>();
        adapter = new MyFoodsAdapter(this, foodList);
        recyclerView.setAdapter(adapter);

        // 3. Load Data
        loadMyFoods();

        // 4. Setup Add Button (Bottom Sheet Dialog)
        ImageButton btnAdd = findViewById(R.id.btnAddFood);
        btnAdd.setOnClickListener(v -> {
            com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                    new com.google.android.material.bottomsheet.BottomSheetDialog(this);
            View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_add, null);
            dialog.setContentView(sheetView);

            sheetView.findViewById(R.id.btnCloseSheet).setOnClickListener(view -> dialog.dismiss());

            // "Sell Food" option
            sheetView.findViewById(R.id.cardSell).setOnClickListener(view -> {
                dialog.dismiss();
                Intent intent = new Intent(FoodsActivity.this, AddFoodActivity.class);
                intent.putExtra("MODE", "SELL");
                startActivity(intent);
            });

            // "List Free Food" option
            sheetView.findViewById(R.id.cardFree).setOnClickListener(view -> {
                dialog.dismiss();
                Intent intent = new Intent(FoodsActivity.this, AddFoodActivity.class);
                intent.putExtra("MODE", "FREE");
                startActivity(intent);
            });

            dialog.show();
        });

        // 5. Navigation Setup
        setupBottomNavigation();
    }

    private void loadMyFoods() {
        if (fAuth.getCurrentUser() == null) return;

        String userId = fAuth.getCurrentUser().getUid();

        fStore.collection("foods")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            return;
                        }

                        foodList.clear();
                        int activeCounter = 0;

                        if (value != null) {
                            for (QueryDocumentSnapshot doc : value) {
                                MyFoodItem item = doc.toObject(MyFoodItem.class);
                                foodList.add(item);

                                // Check active count
                                if ("active".equalsIgnoreCase(item.getStatus())) {
                                    activeCounter++;
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();

                        // Update the text view with the new count
                        if (tvActiveCount != null) {
                            tvActiveCount.setText(activeCounter + " active foods");
                        }
                    }
                });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_foods);
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
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    // --- ADDED: FIX STATUS BAR VISIBILITY ---
    @Override
    protected void onResume() {
        super.onResume();
        // Since background is white/light, make icons BLACK (Dark) so they are visible
        if (getWindow() != null) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(getResources().getColor(android.R.color.white));
        }
    }
}