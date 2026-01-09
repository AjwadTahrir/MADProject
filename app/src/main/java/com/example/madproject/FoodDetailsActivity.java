package com.example.madproject;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class FoodDetailsActivity extends AppCompatActivity {

    private NestedScrollView nestedScrollView;
    private MapView mapDetailView;
    private String location;
    private ImageButton btnZoomInDetail, btnZoomOutDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_details);

        nestedScrollView = findViewById(R.id.nestedScrollView);
        TextView tvDetailLocation = findViewById(R.id.tvDetailLocation);
        mapDetailView = findViewById(R.id.mapDetailView);
        btnZoomInDetail = findViewById(R.id.btnZoomInDetail);
        btnZoomOutDetail = findViewById(R.id.btnZoomOutDetail);
        ImageView btnBack = findViewById(R.id.btnBack);

        // --- FIX FOR BACK BUTTON --- 
        btnBack.setOnClickListener(v -> finish());

        // Retrieve the location from the intent
        location = getIntent().getStringExtra("location");

        if (location != null && !location.isEmpty()) {
            tvDetailLocation.setText(location);
            setupMap();
        } else {
            tvDetailLocation.setText("Location not available");
        }

        btnZoomInDetail.setOnClickListener(v -> mapDetailView.getController().zoomIn());
        btnZoomOutDetail.setOnClickListener(v -> mapDetailView.getController().zoomOut());
    }

    private void setupMap() {
        mapDetailView.setTileSource(TileSourceFactory.MAPNIK);
        mapDetailView.setMultiTouchControls(true);

        GeoPoint locationPoint = getGeoPointFromAddress(location);
        if (locationPoint != null) {
            Marker marker = new Marker(mapDetailView);
            marker.setPosition(locationPoint);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapDetailView.getOverlays().add(marker);
            mapDetailView.getController().setZoom(17.0);
            mapDetailView.getController().setCenter(locationPoint);
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