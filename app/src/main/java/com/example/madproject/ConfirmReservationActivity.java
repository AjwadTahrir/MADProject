package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class ConfirmReservationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_reservation);

        // 1. Initialize Views
        TextView tvConfirmationId = findViewById(R.id.tvConfirmationId);
        TextView tvFoodName = findViewById(R.id.tvResFoodName);
        TextView tvResQuantity = findViewById(R.id.tvResQuantity);
        TextView tvResTotal = findViewById(R.id.tvResTotal);
        Button btnReturnHome = findViewById(R.id.btnReturnHome);

        // 2. Get Data passed from ReservationActivity
        String confirmId = getIntent().getStringExtra("CONFIRMATION_ID");
        String title = getIntent().getStringExtra("FOOD_TITLE");
        int quantity = getIntent().getIntExtra("QUANTITY", 1);
        double totalPrice = getIntent().getDoubleExtra("TOTAL_PRICE", 0.0);

        // 3. Set Data
        if (confirmId != null) tvConfirmationId.setText(confirmId);
        if (title != null) tvFoodName.setText(title);

        tvResQuantity.setText(quantity + (quantity > 1 ? " packs" : " pack"));

        if (totalPrice == 0.0) {
            tvResTotal.setText("Free");
        } else {
            tvResTotal.setText(String.format(Locale.US, "$%.2f", totalPrice));
        }

        // 4. Logic: Return to Main Screen
        btnReturnHome.setOnClickListener(v -> {
            Intent intent = new Intent(ConfirmReservationActivity.this, MainActivity.class);
            // Clear back stack so they can't swipe back to the confirmation
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}