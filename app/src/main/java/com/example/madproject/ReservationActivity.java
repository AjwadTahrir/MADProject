package com.example.madproject;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ReservationActivity extends AppCompatActivity {

    int quantity = 1;
    double unitPrice = 0.0;

    TextView tvQuantity, tvTotalCost, tvUnitCost, tvTitle;
    ImageView imgFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        // 1. Initialize Views
        tvQuantity = findViewById(R.id.tvQuantity);
        tvTotalCost = findViewById(R.id.tvTotalCost);
        tvUnitCost = findViewById(R.id.tvUnitCost);
        tvTitle = findViewById(R.id.tvResTitle);
        imgFood = findViewById(R.id.imgResFood);
        ImageButton btnPlus = findViewById(R.id.btnPlus);
        ImageButton btnMinus = findViewById(R.id.btnMinus);
        Button btnConfirm = findViewById(R.id.btnConfirm);
        ImageView btnBack = findViewById(R.id.btnBack);

        // 2. Get Data from Details Page
        String title = getIntent().getStringExtra("FOOD_TITLE");
        String priceString = getIntent().getStringExtra("FOOD_PRICE"); // e.g. "$8"
        int imageResId = getIntent().getIntExtra("FOOD_IMAGE", 0);

        if (title != null) tvTitle.setText(title);
        if (imageResId != 0) imgFood.setImageResource(imageResId);

        // Parse Price (Remove '$' symbol to do math)
        if (priceString != null && priceString.contains("$")) {
            try {
                unitPrice = Double.parseDouble(priceString.replace("$", ""));
            } catch (NumberFormatException e) {
                unitPrice = 0.0; // Fallback if parsing fails or its "Free"
            }
        }

        // Initial Display
        updatePriceDisplay();

        // 3. Button Logic
        btnPlus.setOnClickListener(v -> {
            if (quantity < 4) { // Max 4 packs
                quantity++;
                updatePriceDisplay();
            }
        });

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updatePriceDisplay();
            }
        });

        btnBack.setOnClickListener(v -> finish());

        // 4. Confirm Button -> Go to Success Screen
        btnConfirm.setOnClickListener(v -> {
            btnConfirm.setOnClickListener(view -> {
                Intent intent = new Intent(ReservationActivity.this, ConfirmReservationActivity.class);
                // Pass data...
                intent.putExtra("FOOD_TITLE", tvTitle.getText().toString());
                startActivity(intent);
            });
        });
    }

    private void updatePriceDisplay() {
        tvQuantity.setText(String.valueOf(quantity));
        tvUnitCost.setText("$" + String.format("%.2f", unitPrice));

        double total = unitPrice * quantity;
        tvTotalCost.setText("$" + String.format("%.2f", total));
    }
}