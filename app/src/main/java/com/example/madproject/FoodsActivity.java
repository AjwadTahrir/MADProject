package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;
import android.view.View;

public class FoodsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foods);

        // 1. Setup Bottom Navigation (Linking it to other pages)
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_foods); // You need to add 'nav_foods' to your menu XML later if not there

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

        // 2. Setup List Data
        RecyclerView recyclerView = findViewById(R.id.recyclerMyFoods);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<MyFoodItem> foodList = new ArrayList<>();
        foodList.add(new MyFoodItem("Homemade Lasagna", "$8", "active", "Dec 10", R.drawable.ic_launcher_background));
        foodList.add(new MyFoodItem("Leftover Pizza", "Free", "active", "Dec 9", R.drawable.ic_launcher_background));
        foodList.add(new MyFoodItem("Fresh Bread", "$5", "sold", "Dec 8", R.drawable.ic_launcher_background));
        foodList.add(new MyFoodItem("Fresh Vegetables", "Free", "active", "Dec 7", R.drawable.ic_launcher_background));

        MyFoodsAdapter adapter = new MyFoodsAdapter(this, foodList);
        recyclerView.setAdapter(adapter);

        // 3. Setup Add Button Logic
        ImageButton btnAdd = findViewById(R.id.btnAddFood);
        btnAdd.setOnClickListener(v -> {
            // Create the Bottom Sheet Dialog
            com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                    new com.google.android.material.bottomsheet.BottomSheetDialog(this);

            // Inflate the layout we just created
            View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_add, null);
            dialog.setContentView(sheetView);

            // Handle clicks inside the sheet
            sheetView.findViewById(R.id.btnCloseSheet).setOnClickListener(view -> dialog.dismiss());

            sheetView.findViewById(R.id.cardSell).setOnClickListener(view -> {
                dialog.dismiss();
                // Go to Add Food Page (Mode: Sell)
                Intent intent = new Intent(FoodsActivity.this, AddFoodActivity.class);
                intent.putExtra("MODE", "SELL");
                startActivity(intent);
            });

            sheetView.findViewById(R.id.cardFree).setOnClickListener(view -> {
                dialog.dismiss();
                // Go to Add Food Page (Mode: Free)
                Intent intent = new Intent(FoodsActivity.this, AddFoodActivity.class);
                intent.putExtra("MODE", "FREE");
                startActivity(intent);
            });

            dialog.show();
        });
    }
}