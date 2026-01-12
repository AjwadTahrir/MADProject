package com.example.madproject;

import android.app.Activity; // Add this
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher; // Add this
import androidx.activity.result.contract.ActivityResultContracts; // Add this
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private Button btnSave, btnCancel;
    private TextView tvTopName, tvAvatarInitials;
    private EditText etFullName, etEmail, etPhoneNumber, etAddress, etBio;

    private Uri imageUri;
    private StorageReference storageRef;
    private ImageView imgAvatarCircle;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // 1. Initialize Views
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etAddress = findViewById(R.id.etAddress);
        etBio = findViewById(R.id.etBio);
        TextView tvChangeProfile = findViewById(R.id.tvChangeProfilePicture);
        tvTopName = findViewById(R.id.tvProfilePhoto);
        tvAvatarInitials = findViewById(R.id.tvAvatarInitials);

        // Find the ImageView. Note: if it's inside layoutAvatar, use that ID.
        // Assuming your ImageView inside the FrameLayout is @id/imgAvatar
        imgAvatarCircle = findViewById(R.id.layoutAvatar).findViewById(R.id.imgAvatarBackground);

        // 2. Initialize Firebase Storage
        storageRef = FirebaseStorage.getInstance().getReference("profile_pics");

        // 3. Define the Image Picker Launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();

                        // 1. Remove the green tint so the photo looks natural
                        imgAvatarCircle.setImageTintList(null);

                        // 2. Hide initials
                        tvAvatarInitials.setVisibility(View.GONE);

                        // 3. Load the preview
                        Glide.with(this).load(imageUri).circleCrop().into(imgAvatarCircle);

                        btnSave.setEnabled(true);
                    }
                }
        );

        // 4. Setup Image Picker Trigger
        findViewById(R.id.editButton).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // 5. Setup Back Buttons
        findViewById(R.id.UpBackButton).setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());

        // 6. Pre-fill data
        if(getIntent() != null) {
            String nameExtra = getIntent().getStringExtra("fullName");
            etFullName.setText(nameExtra);
            tvTopName.setText(nameExtra);
            updateInitials(nameExtra);
            etEmail.setText(getIntent().getStringExtra("email"));
            etPhoneNumber.setText(getIntent().getStringExtra("phone"));
            etAddress.setText(getIntent().getStringExtra("address"));
            etBio.setText(getIntent().getStringExtra("bio"));
            String imageUrl = getIntent().getStringExtra("profileImageUrl");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                tvAvatarInitials.setVisibility(View.GONE);
                imgAvatarCircle.setImageTintList(null); // Remove the green tint

                Glide.with(this)
                        .load(imageUrl)
                        .circleCrop()
                        .into(imgAvatarCircle);
            }
        }

        // 7. Text Watcher
        TextWatcher editWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnSave.setEnabled(true);
                String currentName = etFullName.getText().toString();
                tvTopName.setText(currentName);
                updateInitials(currentName);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        etFullName.addTextChangedListener(editWatcher);

        btnSave.setOnClickListener(v -> saveProfileChanges());
    }

    private void saveProfileChanges() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        btnSave.setEnabled(false); // Prevent double clicks

        // 1. Prepare the data map
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", etFullName.getText().toString());
        updates.put("email", etEmail.getText().toString());
        updates.put("phone", etPhoneNumber.getText().toString());
        updates.put("address", etAddress.getText().toString());
        updates.put("bio", etBio.getText().toString());

        // 2. Check if we need to upload an image first
        if (imageUri != null) {
            StorageReference fileRef = storageRef.child(userId + ".jpg");

            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Add URL to map and save
                    updates.put("profileImageUrl", uri.toString());
                    submitToFirestore(userId, updates);
                });
            }).addOnFailureListener(e -> {
                // This is where you see "Upload Failed"
                // Usually because of Firebase Console Storage Rules!
                Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                btnSave.setEnabled(true);
            });
        } else {
            // No new image, just save the text
            submitToFirestore(userId, updates);
        }
    }

    private void submitToFirestore(String userId, Map<String, Object> updates) {
        FirebaseFirestore.getInstance()
                .collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Firestore error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                });
    }

    private void updateInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            tvAvatarInitials.setText("?");
            return;
        }
        String[] split = name.trim().split("\\s+");
        String initials = "" + split[0].charAt(0);
        if (split.length > 1) initials += split[1].charAt(0);
        tvAvatarInitials.setText(initials.toUpperCase());
    }
}