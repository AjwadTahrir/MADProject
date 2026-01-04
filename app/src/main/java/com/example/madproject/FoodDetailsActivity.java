package com.example.madproject;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

public class FoodDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_details);

        // 1. Initialize Views
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvPrice = findViewById(R.id.tvDetailPrice);
        TextView tvSellerName = findViewById(R.id.tvSellerName);
        ImageView imgFood = findViewById(R.id.imgDetailFood);
        ImageView btnBack = findViewById(R.id.btnBack);

        // 2. Get Data passed from Search Page
        String title = getIntent().getStringExtra("FOOD_TITLE");
        String price = getIntent().getStringExtra("FOOD_PRICE");
        String imageUrl = getIntent().getStringExtra("FOOD_IMAGE_URL");
        String sellerName = getIntent().getStringExtra("OWNER_NAME");
        int imageResId = getIntent().getIntExtra("FOOD_IMAGE", 0);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(imgFood);
        } else if (imageResId != 0) {
            // Fallback to local resource for static items
            imgFood.setImageResource(imageResId);
        }
        // 3. Set the Data to the Views
        if (title != null) tvTitle.setText(title);
        if (price != null) tvPrice.setText(price);
        if (imageResId != 0) imgFood.setImageResource(imageResId);
        if (sellerName != null) tvSellerName.setText(sellerName);

        // 4. Handle Back Button Click
        btnBack.setOnClickListener(v -> finish()); // Closes this screen and goes back

        // 5. Handle Request Button (Placeholder for next step){
            findViewById(R.id.btnRequest).setOnClickListener(view -> {
                Intent intent = new Intent(FoodDetailsActivity.this, ReservationActivity.class);
                // Pass the same data forward
                intent.putExtra("FOOD_TITLE", title);
                intent.putExtra("FOOD_PRICE", price);
                intent.putExtra("FOOD_IMAGE", imageResId);
                intent.putExtra("OWNER_NAME", sellerName);
                startActivity(intent);
            });

    }
}