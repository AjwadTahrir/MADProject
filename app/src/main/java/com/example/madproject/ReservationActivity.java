package com.example.madproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class ReservationActivity extends AppCompatActivity {

    int quantity = 1;
    int maxAvailable = 1;
    double unitPrice = 0.0;
    String foodId, foodTitle, imageUrl;

    TextView tvQuantity, tvTotalCost, tvUnitCost, tvTitle, tvMaxQuantity;
    ImageView imgFood;
    Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        // 1. Initialize Views
        tvQuantity = findViewById(R.id.tvQuantity);
        tvTotalCost = findViewById(R.id.tvTotalCost);
        tvUnitCost = findViewById(R.id.tvUnitCost);
        tvTitle = findViewById(R.id.tvResTitle);
        tvMaxQuantity = findViewById(R.id.tvMaxQuantity);
        imgFood = findViewById(R.id.imgResFood);
        ImageButton btnPlus = findViewById(R.id.btnPlus);
        ImageButton btnMinus = findViewById(R.id.btnMinus);
        btnConfirm = findViewById(R.id.btnConfirm);
        ImageView btnBack = findViewById(R.id.btnBack);

        // 2. Get Data from Intent
        foodTitle = getIntent().getStringExtra("FOOD_TITLE");
        String priceString = getIntent().getStringExtra("FOOD_PRICE");
        imageUrl = getIntent().getStringExtra("FOOD_IMAGE_URI");
        foodId = getIntent().getStringExtra("FOOD_ID");

        // Parse Max Quantity
        String qtyString = getIntent().getStringExtra("FOOD_QUANTITY_CURRENT");
        if (qtyString == null) qtyString = getIntent().getStringExtra("FOOD_QUANTITY");

        try {
            if (qtyString != null) {
                if (qtyString.contains("/")) {
                    maxAvailable = Integer.parseInt(qtyString.split("/")[0]);
                } else {
                    maxAvailable = Integer.parseInt(qtyString);
                }
            }
        } catch (Exception e) {
            maxAvailable = 1;
        }

        // 3. UI Setup
        if (foodTitle != null) tvTitle.setText(foodTitle);
        tvMaxQuantity.setText(maxAvailable + " packs");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            imgFood.setImageURI(Uri.parse(imageUrl));
        }

        // Parse Price
        if (priceString != null) {
            if (priceString.equalsIgnoreCase("Free")) {
                unitPrice = 0.0;
            } else {
                String cleanPrice = priceString.replace("$", "").replace("/ each", "").trim();
                try {
                    unitPrice = Double.parseDouble(cleanPrice);
                } catch (NumberFormatException e) {
                    unitPrice = 0.0;
                }
            }
        }

        updatePriceDisplay();

        // 4. Buttons
        btnPlus.setOnClickListener(v -> {
            if (quantity < maxAvailable) {
                quantity++;
                updatePriceDisplay();
            } else {
                Toast.makeText(this, "Max available quantity reached", Toast.LENGTH_SHORT).show();
            }
        });

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updatePriceDisplay();
            }
        });

        btnBack.setOnClickListener(v -> finish());

        btnConfirm.setOnClickListener(view -> {
            if (foodId == null) {
                Toast.makeText(this, "Error: Food ID missing", Toast.LENGTH_SHORT).show();
                return;
            }
            // Start the new process
            checkAndProcessReservation();
        });
    }

    private void updatePriceDisplay() {
        tvQuantity.setText(String.valueOf(quantity));
        if (unitPrice == 0.0) {
            tvUnitCost.setText("Free");
            tvTotalCost.setText("Free");
        } else {
            tvUnitCost.setText(String.format(Locale.US, "$%.2f", unitPrice));
            double total = unitPrice * quantity;
            tvTotalCost.setText(String.format(Locale.US, "$%.2f", total));
        }
    }

    /**
     * STEP 1: Check if a pending reservation already exists for this user + food.
     */
    private void checkAndProcessReservation() {
        btnConfirm.setEnabled(false);
        btnConfirm.setText("Checking...");

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("reservations")
                .whereEqualTo("buyerId", currentUserId)
                .whereEqualTo("foodId", foodId)
                .whereEqualTo("status", "pending") // Only group if it's still pending
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    String existingResId = null;
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Found an existing pending reservation!
                        existingResId = queryDocumentSnapshots.getDocuments().get(0).getId();
                    }
                    // Proceed to transaction, passing the ID (if found) or null (if not)
                    runTransaction(existingResId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking reservations", Toast.LENGTH_SHORT).show();
                    btnConfirm.setEnabled(true);
                    btnConfirm.setText("Confirm Reservation");
                });
    }

    /**
     * STEP 2: Run the transaction to update Food Stock AND Reservation (Create or Update)
     */
    private void runTransaction(String existingReservationId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference foodRef = db.collection("foods").document(foodId);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.runTransaction(new Transaction.Function<String>() {
            @Override
            public String apply(Transaction transaction) throws FirebaseFirestoreException {
                // A. Read Food Document (Check Stock)
                DocumentSnapshot foodSnap = transaction.get(foodRef);
                String currentQtyStr = foodSnap.getString("currentQuantity");
                if (currentQtyStr == null) currentQtyStr = foodSnap.getString("quantity");
                String sellerId = foodSnap.getString("userId");

                int dbQuantity = Integer.parseInt(currentQtyStr != null ? currentQtyStr : "0");

                if (dbQuantity < quantity) {
                    throw new FirebaseFirestoreException("Not enough food available", FirebaseFirestoreException.Code.ABORTED);
                }

                // B. Update Food Stock (Decrease)
                int newFoodQuantity = dbQuantity - quantity;
                transaction.update(foodRef, "currentQuantity", String.valueOf(newFoodQuantity));
                if (newFoodQuantity == 0) {
                    transaction.update(foodRef, "status", "inactive");
                }

                // C. Handle Reservation (Update Existing OR Create New)
                String finalConfirmationId;

                if (existingReservationId != null) {
                    // --- UPDATE EXISTING ---
                    DocumentReference resRef = db.collection("reservations").document(existingReservationId);
                    DocumentSnapshot resSnap = transaction.get(resRef);

                    // Calculate new totals
                    long oldResQty = resSnap.getLong("quantityReserved");
                    double oldTotalPrice = resSnap.getDouble("totalPrice");

                    long newResQty = oldResQty + quantity;
                    double newTotalPrice = oldTotalPrice + (unitPrice * quantity);

                    transaction.update(resRef, "quantityReserved", newResQty);
                    transaction.update(resRef, "totalPrice", newTotalPrice);
                    transaction.update(resRef, "timestamp", System.currentTimeMillis()); // Update time so it goes to top of list

                    finalConfirmationId = existingReservationId;

                } else {
                    // --- CREATE NEW ---
                    String uniqueId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                    finalConfirmationId = "FS-" + uniqueId;
                    DocumentReference resRef = db.collection("reservations").document(finalConfirmationId);

                    Map<String, Object> resData = new HashMap<>();
                    resData.put("reservationId", finalConfirmationId);
                    resData.put("foodId", foodId);
                    resData.put("foodTitle", foodTitle);
                    resData.put("foodImage", imageUrl);
                    resData.put("buyerId", currentUserId);
                    resData.put("sellerId", sellerId);
                    resData.put("quantityReserved", quantity);
                    resData.put("totalPrice", unitPrice * quantity);
                    resData.put("status", "pending");
                    resData.put("timestamp", System.currentTimeMillis());

                    transaction.set(resRef, resData);
                }

                return finalConfirmationId;
            }
        }).addOnSuccessListener(confId -> {
            Intent intent = new Intent(ReservationActivity.this, ConfirmReservationActivity.class);
            intent.putExtra("CONFIRMATION_ID", confId);
            intent.putExtra("FOOD_TITLE", foodTitle);
            intent.putExtra("QUANTITY", quantity); // Show what was just added in confirmation
            intent.putExtra("TOTAL_PRICE", unitPrice * quantity);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            btnConfirm.setEnabled(true);
            btnConfirm.setText("Confirm Reservation");
            Toast.makeText(ReservationActivity.this, "Reservation Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}