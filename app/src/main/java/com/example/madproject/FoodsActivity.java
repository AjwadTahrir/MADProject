package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class FoodsActivity extends AppCompatActivity {

    // Member variables for Firestore data handling
    private MyFoodsAdapter adapter;
    private List<MyFoodItem> foodList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foods);

        // 1. Setup RecyclerView and Adapter
        RecyclerView recyclerView = findViewById(R.id.recyclerMyFoods);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        foodList = new ArrayList<>();
        adapter = new MyFoodsAdapter(this, foodList);
        recyclerView.setAdapter(adapter);

        // 2. Fetch data from Firebase
        loadMyFoods();

        // 3. Setup Bottom Navigation
        setupBottomNav();

        // 4. Setup Add Button (Bottom Sheet)
        setupAddButton();
    }

    private void loadMyFoods() {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Listens for real-time updates in the "foods" collection
        FirebaseFirestore.getInstance().collection("foods")
                .whereEqualTo("ownerId", currentUid)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    if (value != null) {
                        foodList.clear(); // Clear static/old data
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            // Converts Firestore document to your MyFoodItem object
                            MyFoodItem item = doc.toObject(MyFoodItem.class);
                            foodList.add(item);
                        }
                        adapter.notifyDataSetChanged(); // Refresh UI
                    }
                });
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_foods);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (itemId == R.id.nav_search) {
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return itemId == R.id.nav_foods;
        });
    }

    private void setupAddButton() {
        ImageButton btnAdd = findViewById(R.id.btnAddFood);
        btnAdd.setOnClickListener(v -> {
            com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                    new com.google.android.material.bottomsheet.BottomSheetDialog(this);
            View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_add, null);
            dialog.setContentView(sheetView);

            sheetView.findViewById(R.id.btnCloseSheet).setOnClickListener(view -> dialog.dismiss());
            sheetView.findViewById(R.id.cardSell).setOnClickListener(view -> openAddFood("SELL", dialog));
            sheetView.findViewById(R.id.cardFree).setOnClickListener(view -> openAddFood("FREE", dialog));
            dialog.show();
        });
    }

    private void openAddFood(String mode, com.google.android.material.bottomsheet.BottomSheetDialog dialog) {
        dialog.dismiss();
        Intent intent = new Intent(FoodsActivity.this, AddFoodActivity.class);
        intent.putExtra("MODE", mode);
        startActivity(intent);
    }
}