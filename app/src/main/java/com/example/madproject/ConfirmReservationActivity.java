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
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ConfirmReservationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_reservation);

        // 1. Initialize Views
        TextView tvFoodName = findViewById(R.id.tvResFoodName);
        TextView tvTotal = findViewById(R.id.tvResTotal);
        Button btnNext = findViewById(R.id.btnGoToRedeem);

        // 2. Get Data (Passed from ReservationActivity)
        String title = getIntent().getStringExtra("FOOD_TITLE");
        // You might want to pass Total Price here too in the future

        if (title != null) tvFoodName.setText(title);
        // For now, Total is hardcoded in XML or you can pass it via Intent

        // 3. Handle Button Click
        btnNext.setOnClickListener(v -> {
            // Navigate to the "Ready to Redeem" screen (The Slider Screen)
            Intent intent = new Intent(ConfirmReservationActivity.this, RedeemActivity.class);
            startActivity(intent);
        });
    }
}