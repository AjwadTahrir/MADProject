package com.example.madproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddFoodActivity extends AppCompatActivity {

    private LinearLayout uploadContainer;
    private View placeholderState;
    private ImageView ivSelectedImage;
    private EditText etTitle, etDescription, etPrice, etQuantity, etPickup;
    private String mode;
    private Uri imageUri; // Variable to store the picked image URI

    // Image Picker Launcher
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            result -> {
                if (result != null) {
                    imageUri = result;
                    placeholderState.setVisibility(View.GONE);
                    ivSelectedImage.setVisibility(View.VISIBLE);
                    ivSelectedImage.setImageURI(result);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        // 1. Initialize Views
        initViews();

        // 2. Setup Mode (Sell vs Free)
        mode = getIntent().getStringExtra("MODE");
        setupModeUI();

        // 3. Image Picker Click
        uploadContainer.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // 4. Back/Close Actions
        findViewById(R.id.btnBackAdd).setOnClickListener(v -> finish());
        findViewById(R.id.btnCloseAdd).setOnClickListener(v -> finish());

        // 5. List Button Click
        findViewById(R.id.btnList).setOnClickListener(v -> validateAndUpload());
    }

    private void initViews() {
        uploadContainer = findViewById(R.id.uploadContainer);
        placeholderState = findViewById(R.id.placeholderState);
        ivSelectedImage = findViewById(R.id.ivSelectedImage);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etQuantity = findViewById(R.id.etQuantity);
        etPickup = findViewById(R.id.etPickup);
    }

    private void setupModeUI() {
        TextView tvTitle = findViewById(R.id.tvAddTitle);
        Button btnList = findViewById(R.id.btnList);

        if ("FREE".equals(mode)) {
            tvTitle.setText("List Free Food");
            btnList.setText("List for Free");
            etPrice.setText("0.00");
            etPrice.setEnabled(false);
            etPrice.setVisibility(View.GONE);
        } else {
            tvTitle.setText("Sell Food");
            btnList.setText("List for Sale");
        }
    }

    private void validateAndUpload() {
        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String price = etPrice.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Please add a photo, title, and description", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadImageToStorage(title, desc, price);
    }

    private void uploadImageToStorage(String title, String desc, String price) {
        // Show a toast or progress bar here
        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();

        // Create a unique filename for the image
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("food_images/" + fileName);

        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            // Image uploaded! Now get the download URL
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                saveFoodToFirestore(title, desc, price, uri.toString());
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });

        btnSearchLocation.setOnClickListener(v -> {
            String locationName = etLocation.getText().toString();
            if (!locationName.isEmpty()) {
                GeoPoint locationPoint = getGeoPointFromAddress(locationName);
                if (locationPoint != null) {
                    updateMap(locationPoint);
                }
            }
        });

        btnZoomIn.setOnClickListener(v -> mapView.getController().zoomIn());
        btnZoomOut.setOnClickListener(v -> mapView.getController().zoomOut());

        MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                updateMap(p);
                getAddressFromGeoPoint(p);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        mapView.getOverlays().add(new MapEventsOverlay(mapEventsReceiver));

        checkLocationPermission();
    }

    private void updateMap(GeoPoint p) {
        if (currentMarker != null) {
            mapView.getOverlays().remove(currentMarker);
        }
        currentMarker = new Marker(mapView);
        currentMarker.setPosition(p);
        currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(currentMarker);
        mapView.getController().setCenter(p);
        mapView.getController().setZoom(17.0);
        mapView.invalidate();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getAddressFromGeoPoint(GeoPoint geoPoint) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(geoPoint.getLatitude(), geoPoint.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                etLocation.setText(address.getAddressLine(0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private GeoPoint getGeoPointFromAddress(String address) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                return new GeoPoint(location.getLatitude(), location.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    private void saveFoodToFirestore(String title, String desc, String price, String imageUrl) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String uName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        Map<String, Object> food = new HashMap<>();
        food.put("title", title);
        food.put("description", desc);
        food.put("price", "FREE".equals(mode) ? "Free" : "$" + price);
        food.put("imageUrl", imageUrl); // Link to the uploaded image
        food.put("ownerId", uid);
        food.put("ownerName", uName != null ? uName : "Anonymous Seller");
        food.put("status", "active");
        food.put("timestamp", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance().collection("foods").add(food)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Food Listed Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
