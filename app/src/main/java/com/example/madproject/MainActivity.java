package com.example.madproject;


import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerFoods, recyclerActivity;
    FoodAdapter adapter;
    List<FoodItem> foodList;
    TextView tvHello;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvHello = findViewById(R.id.tvHello);

        // Fetch User Data from Firestore
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name != null) tvHello.setText("Hello, " + name + "!");
                    }
                });

        // Setup Featured Foods (Static for now, fixed constructors)
        recyclerFoods = findViewById(R.id.recyclerFeaturedFoods);
        recyclerFoods.setLayoutManager(new GridLayoutManager(this, 2));

        foodList = new ArrayList<>();
        // Fixed: Added empty string "" as 5th argument for imageUrl

        adapter = new FoodAdapter(foodList, item -> {
            Intent intent = new Intent(MainActivity.this, FoodDetailsActivity.class);
            intent.putExtra("FOOD_TITLE", item.getTitle());
            intent.putExtra("FOOD_PRICE", item.getPrice());
            intent.putExtra("FOOD_IMAGE_URL", item.getImageUrl()); // Use the web URL
            startActivity(intent);
        });
        recyclerFoods.setAdapter(adapter);

        // Setup Recent Activity
        recyclerActivity = findViewById(R.id.recyclerRecentActivity);
        recyclerActivity.setLayoutManager(new LinearLayoutManager(this));
        List<ActivityItem> activityList = new ArrayList<>();
        activityList.add(new ActivityItem("Listed new food", "Fresh Vegetables", "2h ago"));

        ActivityAdapter activityAdapter = new ActivityAdapter(activityList);
        recyclerActivity.setAdapter(activityAdapter);
        recyclerActivity.setNestedScrollingEnabled(false);

        // Bottom Navigation Logic
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) return true;
            if (itemId == R.id.nav_search) startActivity(new Intent(this, SearchActivity.class));
            if (itemId == R.id.nav_foods) startActivity(new Intent(this, FoodsActivity.class));
            if (itemId == R.id.nav_profile) startActivity(new Intent(this, ProfileActivity.class));
            return true;
        });
        loadFeaturedFoods();
    }

    private void loadFeaturedFoods() {
        FirebaseFirestore.getInstance().collection("foods")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        foodList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                            FoodItem item = doc.toObject(FoodItem.class);
                            foodList.add(item);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}