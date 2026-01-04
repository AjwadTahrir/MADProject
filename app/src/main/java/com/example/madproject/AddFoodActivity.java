package com.example.madproject;

import android.net.Uri;
import android.os.Bundle;
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
            }
    );

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