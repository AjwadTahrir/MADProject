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

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class AddFoodActivity extends AppCompatActivity {

    // Variables for the Image Picker Views
    private LinearLayout uploadContainer;
    private View placeholderState; // The icon and text
    private ImageView ivSelectedImage; // The final image

    // 1. DEFINE THE IMAGE LAUNCHER
    // This handles the result when the user picks a photo from the gallery
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result != null) {
                        // Image picked successfully!
                        placeholderState.setVisibility(View.GONE);  // Hide the "Tap to add" text
                        ivSelectedImage.setVisibility(View.VISIBLE); // Show the image view
                        ivSelectedImage.setImageURI(result);        // Display the image
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        // 2. INITIALIZE ALL VIEWS
        TextView tvTitle = findViewById(R.id.tvAddTitle);
        EditText etPrice = findViewById(R.id.etPrice);
        Button btnList = findViewById(R.id.btnList);
        ImageView btnBack = findViewById(R.id.btnBackAdd);
        ImageView btnClose = findViewById(R.id.btnCloseAdd);

        // Views for image picking
        uploadContainer = findViewById(R.id.uploadContainer);
        placeholderState = findViewById(R.id.placeholderState);
        ivSelectedImage = findViewById(R.id.ivSelectedImage);

        // 3. CHECK INTENT MODE (Sell vs Free)
        String mode = getIntent().getStringExtra("MODE");

        if (mode != null && mode.equals("FREE")) {
            tvTitle.setText("List Free Food");
            btnList.setText("List for Free");

            // Hide the Price input since it's free
            if (etPrice != null) {
                etPrice.setText("0.00");
                etPrice.setEnabled(false);
                etPrice.setVisibility(View.GONE); // Hide the price field completely
            }
        } else {
            tvTitle.setText("Sell Food");
            btnList.setText("List for Sale");
        }

        // 4. HANDLE BACK & CLOSE BUTTONS
        View.OnClickListener closeAction = v -> finish();
        btnBack.setOnClickListener(closeAction);
        btnClose.setOnClickListener(closeAction);

        // 5. HANDLE IMAGE PICKER CLICK
        uploadContainer.setOnClickListener(v -> {
            // Launch the gallery filtering for images only
            pickImageLauncher.launch("image/*");
        });

        // 6. HANDLE "LIST" BUTTON CLICK
        btnList.setOnClickListener(v -> {
            // Simple validation before finishing
            if (etPrice.isEnabled() && etPrice.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please enter a price", Toast.LENGTH_SHORT).show();
                return;
            }

            // Here you would normally save data to a database
            Toast.makeText(this, "Food Listed Successfully!", Toast.LENGTH_SHORT).show();

            // Close this page and go back
            finish();
        });
    }
}