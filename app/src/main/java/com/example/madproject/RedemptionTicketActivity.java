package com.example.madproject;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.Calendar;
import java.util.Locale;

public class RedemptionTicketActivity extends AppCompatActivity {

    private TextView tvRedeemCode, tvFoodTitle, tvSellerName, tvDate, tvMealsSavedText;
    private SeekBar sliderRedeem;

    private String reservationId, sellerId;
    private int quantityReserved;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redeem);

        db = FirebaseFirestore.getInstance();

        // 1. Initialize Views
        tvRedeemCode = findViewById(R.id.tvRedeemCode);
        tvFoodTitle = findViewById(R.id.tvTicketFoodTitle);    // Ensure ID matches XML
        tvSellerName = findViewById(R.id.tvTicketSellerName);  // Ensure ID matches XML
        tvDate = findViewById(R.id.tvTicketDate);              // Ensure ID matches XML
        tvMealsSavedText = findViewById(R.id.tvMealsSavedText);// "You're saving 1 meal"
        sliderRedeem = findViewById(R.id.sliderRedeem);
        ImageButton btnClose = findViewById(R.id.btnCloseRedeem);

        // 2. Get Data passed from Adapter
        reservationId = getIntent().getStringExtra("RESERVATION_ID");
        String foodTitle = getIntent().getStringExtra("FOOD_TITLE");
        sellerId = getIntent().getStringExtra("SELLER_ID");
        quantityReserved = getIntent().getIntExtra("QUANTITY", 1);
        long timestamp = getIntent().getLongExtra("TIMESTAMP", 0);

        // 3. Set UI Data
        if (reservationId != null) tvRedeemCode.setText(reservationId);
        if (foodTitle != null) tvFoodTitle.setText(foodTitle);

        // Set "You're saving X meals" text
        String savedText = (quantityReserved == 1) ? "1 meal" : quantityReserved + " meals";
        tvMealsSavedText.setText(savedText);

        // Format Date (e.g., "December 14, 2025")
        if (timestamp != 0) {
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(timestamp);
            String date = DateFormat.format("MMMM dd, yyyy", cal).toString();
            tvDate.setText(date);
        }

        // Fetch Seller Name (Async)
        if (sellerId != null) {
            db.collection("users").document(sellerId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("fullName"); // Assuming field is 'fullName'
                            tvSellerName.setText("Seller: " + (name != null ? name : "Unknown"));
                        }
                    });
        }

        // 4. Close Button
        btnClose.setOnClickListener(v -> finish());

        // 5. Slider Logic
        sliderRedeem.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress() > 85) {
                    completeRedemption();
                    seekBar.setProgress(100);
                    seekBar.setEnabled(false); // Lock slider
                } else {
                    seekBar.setProgress(0); // Snap back
                }
            }
        });
    }

    private void completeRedemption() {
        if (reservationId == null) return;
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // START BATCH WRITE
        // This ensures the status update AND the counter increment happen together
        WriteBatch batch = db.batch();

        // 1. Update Reservation Status -> "redeemed"
        // This will make it disappear from the "My Reservations" list automatically
        DocumentReference resRef = db.collection("reservations").document(reservationId);
        batch.update(resRef, "status", "redeemed");

        // 2. Increment User's Meal Counter
        // FieldValue.increment(x) is math-safe. If field doesn't exist, it starts at x.
        DocumentReference userRef = db.collection("users").document(currentUserId);
        batch.update(userRef, "mealsSaved", FieldValue.increment(quantityReserved));

        // Commit Changes
        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Redeemed! " + quantityReserved + " meals added to your stats.", Toast.LENGTH_LONG).show();
            finish(); // Go back to list
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            sliderRedeem.setProgress(0);
            sliderRedeem.setEnabled(true);
        });
    }
}