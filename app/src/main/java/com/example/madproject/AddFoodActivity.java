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

import androidx.activity.result.ActivityResultCallback;
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

public class AddFoodActivity extends AppCompatActivity {

    private NestedScrollView nestedScrollView;
    private LinearLayout uploadContainer;
    private View placeholderState;
    private ImageView ivSelectedImage;
    private MapView mapView;
    private EditText etLocation;
    private Button btnSearchLocation;
    private ImageButton btnZoomIn, btnZoomOut;
    private Marker currentMarker;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result != null) {
                        placeholderState.setVisibility(View.GONE);
                        ivSelectedImage.setVisibility(View.VISIBLE);
                        ivSelectedImage.setImageURI(result);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        nestedScrollView = findViewById(R.id.nestedScrollView);
        TextView tvTitle = findViewById(R.id.tvAddTitle);
        EditText etPrice = findViewById(R.id.etPrice);
        Button btnList = findViewById(R.id.btnList);
        ImageView btnBack = findViewById(R.id.btnBackAdd);
        ImageView btnClose = findViewById(R.id.btnCloseAdd);
        uploadContainer = findViewById(R.id.uploadContainer);
        placeholderState = findViewById(R.id.placeholderState);
        ivSelectedImage = findViewById(R.id.ivSelectedImage);
        etLocation = findViewById(R.id.etLocation);
        btnSearchLocation = findViewById(R.id.btnSearchLocation);
        mapView = findViewById(R.id.mapView);
        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        String mode = getIntent().getStringExtra("MODE");
        if (mode != null && mode.equals("FREE")) {
            tvTitle.setText("List Free Food");
            btnList.setText("List for Free");
            if (etPrice != null) {
                etPrice.setText("0.00");
                etPrice.setEnabled(false);
                etPrice.setVisibility(View.GONE);
            }
        } else {
            tvTitle.setText("Sell Food");
            btnList.setText("List for Sale");
        }

        View.OnClickListener closeAction = v -> finish();
        btnBack.setOnClickListener(closeAction);
        btnClose.setOnClickListener(closeAction);

        uploadContainer.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnList.setOnClickListener(v -> {
            String title = ((EditText) findViewById(R.id.etTitle)).getText().toString();
            String description = ((EditText) findViewById(R.id.etDescription)).getText().toString();
            String price = etPrice.getText().toString();
            String quantity = ((EditText) findViewById(R.id.etQuantity)).getText().toString();
            String pickupTime = ((EditText) findViewById(R.id.etPickupTime)).getText().toString();
            String location = etLocation.getText().toString();

            if (title.isEmpty() || description.isEmpty() || price.isEmpty() || quantity.isEmpty() || pickupTime.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Food Listed Successfully!", Toast.LENGTH_SHORT).show();
            finish();
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
}
