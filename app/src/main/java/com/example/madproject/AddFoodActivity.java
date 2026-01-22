package com.example.madproject;

import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager; // Important for OSM
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.UUID;

public class AddFoodActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etPrice, etQuantity, etPickupTime, etLocation;
    private ImageView ivSelectedImage;
    private View placeholderState;
    private MapView mapView; // OSM Map
    private Uri selectedImageUri;
    private Marker currentMarker;

    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            result -> {
                if (result != null) {
                    selectedImageUri = result;
                    placeholderState.setVisibility(View.GONE);
                    ivSelectedImage.setVisibility(View.VISIBLE);
                    ivSelectedImage.setImageURI(result);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. IMPORTANT: Initialize OSM Configuration BEFORE setting content view
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        setContentView(R.layout.activity_add_food);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        // Init Views
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etQuantity = findViewById(R.id.etQuantity);
        etPickupTime = findViewById(R.id.etPickupTime);
        etLocation = findViewById(R.id.etLocation);
        ivSelectedImage = findViewById(R.id.ivSelectedImage);
        placeholderState = findViewById(R.id.placeholderState);
        LinearLayout uploadContainer = findViewById(R.id.uploadContainer);

        // 2. Setup OSM Map
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);

        // Default Start Point (KL)
        GeoPoint startPoint = new GeoPoint(3.1390, 101.6869);
        mapView.getController().setCenter(startPoint);

        // Image Picker
        uploadContainer.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Back / Close
        findViewById(R.id.btnBackAdd).setOnClickListener(v -> finish());
        findViewById(R.id.btnCloseAdd).setOnClickListener(v -> finish());

        // Mode Check
        String mode = getIntent().getStringExtra("MODE");
        TextView tvTitle = findViewById(R.id.tvAddTitle);
        Button btnList = findViewById(R.id.btnList);
        if ("FREE".equals(mode)) {
            tvTitle.setText("List Free Food");
            btnList.setText("List for Free");
            etPrice.setText("0.00");
            etPrice.setVisibility(View.GONE);
        }

        // Search Location Button
        findViewById(R.id.btnSearchLocation).setOnClickListener(v -> {
            String location = etLocation.getText().toString();
            searchLocation(location);
        });

        // List Button
        btnList.setOnClickListener(v -> saveFoodToFirebase(mode));
    }

    private boolean isValidTimeFormat(String time) {
        // This Regex checks for: (Hour):(Min)(AM/PM) - (Hour):(Min)(AM/PM)
        // Matches: "6:00 PM - 8:00 PM" or "10:30 AM - 12:30 PM"
        String timePattern = "^(1[012]|[1-9]):[0-5][0-9] (AM|PM) - (1[012]|[1-9]):[0-5][0-9] (AM|PM)$";
        return time.matches(timePattern);
    }

    private void searchLocation(String locationName) {
        if (locationName.isEmpty()) return;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                GeoPoint point = new GeoPoint(address.getLatitude(), address.getLongitude());

                // Update Map
                mapView.getController().animateTo(point);

                // Add Pin
                if (currentMarker != null) mapView.getOverlays().remove(currentMarker);
                currentMarker = new Marker(mapView);
                currentMarker.setPosition(point);
                currentMarker.setTitle(locationName);
                currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mapView.getOverlays().add(currentMarker);
                mapView.invalidate(); // Refresh map

            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFoodToFirebase(String mode) {
        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString();
        String price = etPrice.getText().toString();
        String qty = etQuantity.getText().toString();
        String time = etPickupTime.getText().toString().trim();
        String loc = etLocation.getText().toString();

        // Validation
        if (title.isEmpty() || desc.isEmpty() || qty.isEmpty() || time.isEmpty() || loc.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidTimeFormat(time)) {
            etPickupTime.setError("Format must be: 6:00 PM - 8:00 PM");
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- START IMAGE UPLOAD PROCESS ---
        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();

        // Create a unique name for the image in Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("food_images/" + UUID.randomUUID().toString() + ".jpg");

        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the permanent URL
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        // Now save everything to Firestore
                        submitDataToFirestore(mode, title, desc, price, qty, time, loc, downloadUrl);
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void submitDataToFirestore(String mode, String title, String desc, String price, String qty, String time, String loc, String imageUrl) {
        String userId = fAuth.getCurrentUser().getUid();
        long timestamp = System.currentTimeMillis();

        Map<String, Object> food = new HashMap<>();
        food.put("userId", userId);
        food.put("title", title);
        food.put("description", desc);
        food.put("price", "FREE".equals(mode) ? "Free" : price);
        food.put("quantity", qty);
        food.put("pickupTime", time);
        food.put("location", loc);
        food.put("status", "active");
        food.put("imageUri", imageUrl);
        food.put("timestamp", timestamp);

        fStore.collection("foods").add(food)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Listed Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }

        // --- ADDED: FIX STATUS BAR VISIBILITY ---
        // Since background is likely white, make icons BLACK (Dark)
        if (getWindow() != null) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(getResources().getColor(android.R.color.white));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }
}