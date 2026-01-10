package com.example.madproject;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class FoodDetailsActivity extends AppCompatActivity {

    private String location, title, price, imageUriString, foodId, ownerId, quantity, currentQuantity;
    private MapView mapDetailView;
    private Button btnRequest, btnStopSelling;
    private TextView tvSellerName, tvAvailability;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // OSM Config
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        setContentView(R.layout.activity_food_details);

        // Initialize Views
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvPrice = findViewById(R.id.tvDetailPrice);
        TextView tvDesc = findViewById(R.id.tvDescription);
        TextView tvLoc = findViewById(R.id.tvDetailLocation);
        ImageView imgFood = findViewById(R.id.imgDetailFood);
        mapDetailView = findViewById(R.id.mapDetailView);

        tvSellerName = findViewById(R.id.tvSellerName);
        tvAvailability = findViewById(R.id.tvAvailability);

        btnRequest = findViewById(R.id.btnRequest);
        btnStopSelling = findViewById(R.id.btnStopSelling);

        // --- GET DATA FROM INTENT ---
        Intent intent = getIntent();
        title = intent.getStringExtra("FOOD_TITLE");
        price = intent.getStringExtra("FOOD_PRICE");
        String desc = intent.getStringExtra("FOOD_DESC");
        location = intent.getStringExtra("FOOD_LOCATION");
        imageUriString = intent.getStringExtra("FOOD_IMAGE_URI");

        // CRITICAL DATA
        foodId = intent.getStringExtra("FOOD_ID");
        ownerId = intent.getStringExtra("FOOD_OWNER_ID");

        // Availability Data
        quantity = intent.getStringExtra("FOOD_QUANTITY_INITIAL"); // Total (e.g. 4)
        currentQuantity = intent.getStringExtra("FOOD_QUANTITY_CURRENT"); // Remaining (e.g. 3)

        // Fallback for older data
        if (quantity == null) quantity = intent.getStringExtra("FOOD_QUANTITY");
        if (currentQuantity == null) currentQuantity = quantity;

        // --- SET TEXT DATA ---
        if (title != null) tvTitle.setText(title);
        if (desc != null) tvDesc.setText(desc);
        if (location != null) tvLoc.setText(location);

        // Format Price
        if (price != null) {
            try {
                double priceValue = Double.parseDouble(price);
                tvPrice.setText(String.format(Locale.US, "$%.2f / each", priceValue));
            } catch (NumberFormatException e) {
                tvPrice.setText(price);
            }
        }

        // --- SET AVAILABILITY DISPLAY ---
        if (quantity != null && currentQuantity != null) {
            tvAvailability.setText(currentQuantity + "/" + quantity );
        } else {
            tvAvailability.setText("Checking...");
        }

        // Set Image
        if (imageUriString != null && !imageUriString.isEmpty()) {
            imgFood.setImageURI(Uri.parse(imageUriString));
        } else {
            imgFood.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // Fetch Seller Name
        checkOwnership();
        if (ownerId != null) {
            fetchSellerName(ownerId);
        }

        // Setup Map
        mapDetailView.setTileSource(TileSourceFactory.MAPNIK);
        mapDetailView.setMultiTouchControls(true);
        if (location != null && !location.isEmpty()) {
            geocodeAndPin(location);
        }

        // Buttons
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // --- REQUEST BUTTON (FIXED CHAIN) ---
        btnRequest.setOnClickListener(v -> {
            Intent resIntent = new Intent(FoodDetailsActivity.this, ReservationActivity.class);

            // Pass display data
            resIntent.putExtra("FOOD_TITLE", title);
            resIntent.putExtra("FOOD_PRICE", price);
            resIntent.putExtra("FOOD_IMAGE_URI", imageUriString);

            // *** CRITICAL FIX: Pass the ID and Quantity to the next page ***
            resIntent.putExtra("FOOD_ID", foodId);
            resIntent.putExtra("FOOD_QUANTITY_CURRENT", currentQuantity);

            startActivity(resIntent);
        });

        btnStopSelling.setOnClickListener(v -> stopSellingFood());
    }

    private void fetchSellerName(String userId) {
        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("fullName");
                        tvSellerName.setText(name != null ? name : "Unknown Seller");
                    }
                })
                .addOnFailureListener(e -> tvSellerName.setText("Seller info unavailable"));
    }

    private void checkOwnership() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (ownerId != null && ownerId.equals(currentUserId)) {
            btnRequest.setVisibility(View.GONE);
            btnStopSelling.setVisibility(View.VISIBLE);
        } else {
            btnRequest.setVisibility(View.VISIBLE);
            btnStopSelling.setVisibility(View.GONE);
        }
    }

    private void stopSellingFood() {
        if (foodId == null) return;
        FirebaseFirestore.getInstance().collection("foods").document(foodId)
                .update("status", "stopped")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Food removed from sale", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void geocodeAndPin(String locationName) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                GeoPoint point = new GeoPoint(address.getLatitude(), address.getLongitude());
                mapDetailView.getController().setZoom(17.0);
                mapDetailView.getController().setCenter(point);
                Marker marker = new Marker(mapDetailView);
                marker.setPosition(point);
                marker.setTitle(locationName);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mapDetailView.getOverlays().add(marker);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapDetailView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapDetailView.onPause();
    }
}