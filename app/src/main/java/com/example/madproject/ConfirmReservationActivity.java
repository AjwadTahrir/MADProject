package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ConfirmReservationActivity extends AppCompatActivity {

    private TextView tvHours, tvMinutes, tvSeconds;
    private android.os.CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_reservation);

        // 1. Initialize Views
        TextView tvConfirmationId = findViewById(R.id.tvConfirmationId);
        TextView tvFoodName = findViewById(R.id.tvResFoodName);
        TextView tvResQuantity = findViewById(R.id.tvResQuantity);
        TextView tvResTotal = findViewById(R.id.tvResTotal);
        TextView tvResPickup = findViewById(R.id.tvResPickupTime);
        Button btnReturnHome = findViewById(R.id.btnReturnHome);

        // 2. Get Data passed from ReservationActivity
        String confirmId = getIntent().getStringExtra("CONFIRMATION_ID");
        String title = getIntent().getStringExtra("FOOD_TITLE");
        int quantity = getIntent().getIntExtra("QUANTITY", 1);
        double totalPrice = getIntent().getDoubleExtra("TOTAL_PRICE", 0.0);
        String pickupTime = getIntent().getStringExtra("FOOD_PICKUP_TIME");

        tvHours = findViewById(R.id.tvHours);
        tvMinutes = findViewById(R.id.tvMinutes);
        tvSeconds = findViewById(R.id.tvSeconds);

        startCountdown(pickupTime);



        // 3. Set Data
        if (confirmId != null) tvConfirmationId.setText(confirmId);
        if (title != null) tvFoodName.setText(title);

        tvResQuantity.setText(quantity + (quantity > 1 ? " packs" : " pack"));

        if (totalPrice == 0.0) {
            tvResTotal.setText("Free");
        } else {
            tvResTotal.setText(String.format(Locale.US, "$%.2f", totalPrice));
        }

        // --- DISPLAY PICKUP TIME ---
        if (pickupTime != null && !pickupTime.isEmpty()) {
            tvResPickup.setText("Pick-up: Today," + pickupTime);
        } else {
            tvResPickup.setText("Pick-up: Today, Not specified");
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

    private void startCountdown(String pickupTimeString) {
        if (pickupTimeString == null || pickupTimeString.isEmpty()) return;

        try {
            // 1. Get the start time (e.g., "8:00 PM")
            String startTimeStr = pickupTimeString.split("-")[0].trim();
            SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.US);
            Date parsedDate = format.parse(startTimeStr);

            // 2. Get the current date and time
            Calendar now = Calendar.getInstance();

            // 3. Create the "Target" calendar for TODAY
            Calendar targetTime = Calendar.getInstance();
            Calendar timeParts = Calendar.getInstance();
            timeParts.setTime(parsedDate);

            // Merge today's Date with the Parsed Time
            targetTime.set(Calendar.HOUR_OF_DAY, timeParts.get(Calendar.HOUR_OF_DAY));
            targetTime.set(Calendar.MINUTE, timeParts.get(Calendar.MINUTE));
            targetTime.set(Calendar.SECOND, 0);
            targetTime.set(Calendar.MILLISECOND, 0);

            // 4. Calculate the difference
            long millisUntilTarget = targetTime.getTimeInMillis() - now.getTimeInMillis();

            // If the target time is already in the past for today, don't start
            if (millisUntilTarget > 0) {
                if (countDownTimer != null) countDownTimer.cancel(); // Safety clear

                countDownTimer = new android.os.CountDownTimer(millisUntilTarget, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        long hours = (millisUntilFinished / (1000 * 60 * 60));
                        long mins = (millisUntilFinished / (1000 * 60)) % 60;
                        long secs = (millisUntilFinished / 1000) % 60;

                        tvHours.setText(String.format(Locale.US, "%02d", hours));
                        tvMinutes.setText(String.format(Locale.US, "%02d", mins));
                        tvSeconds.setText(String.format(Locale.US, "%02d", secs));
                    }

                    @Override
                    public void onFinish() {
                        tvHours.setText("00");
                        tvMinutes.setText("00");
                        tvSeconds.setText("00");
                    }
                }.start();
            } else {
                // If the time is already passed or is happening now
                tvHours.setText("00");
                tvMinutes.setText("00");
                tvSeconds.setText("00");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}