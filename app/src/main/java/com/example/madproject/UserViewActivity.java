package com.example.madproject;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserViewActivity extends AppCompatActivity {

    private String targetUserId, currentUserId;
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private boolean isFollowing = false;

    private TextView tvName, tvBio, tvEmail, tvPhone, tvAddress, tvInitials;
    private TextView tvFoodsCount, tvFollowersCount, tvFollowingCount, tvMealsSaved;
    private ImageView ivProfilePic, ivFollowStatus, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_view);

        // Inside UserViewActivity onCreate
        ImageView btnBack = findViewById(R.id.btnBackAdd);
        btnBack.setOnClickListener(v -> finish()); // This takes you back to the previous screen

        // 1. Get ID from Intent
        targetUserId = getIntent().getStringExtra("TARGET_USER_ID");
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        currentUserId = fAuth.getCurrentUser().getUid();

        // 2. Initialize Views
        initViews();

        // 3. Load Data
        loadTargetUserData();
        listenToSocialCounts();
        checkFollowStatus();
        updateContributions(targetUserId);

        // 4. Click Listeners
        btnBack.setOnClickListener(v -> finish());
        ivFollowStatus.setOnClickListener(v -> toggleFollow());
    }

    private void initViews() {
        tvName = findViewById(R.id.tvName);
        tvBio = findViewById(R.id.tvBioProfile);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        tvInitials = findViewById(R.id.tvInitials);
        tvFoodsCount = findViewById(R.id.tvFoodsCountProfile);
        tvFollowersCount = findViewById(R.id.tvFollowersCount);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);
        tvMealsSaved = findViewById(R.id.tvMealsSavedProfile);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        ivFollowStatus = findViewById(R.id.ivFollowStatus);
        btnBack = findViewById(R.id.btnBackAdd);
    }

    private void loadTargetUserData() {
        fStore.collection("users").document(targetUserId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String name = doc.getString("fullName");
                tvName.setText(name);
                tvBio.setText(doc.getString("bio"));
                tvEmail.setText(doc.getString("email"));
                tvPhone.setText(doc.getString("phone"));
                tvAddress.setText(doc.getString("address"));

                Long meals = doc.getLong("mealsSaved");
                tvMealsSaved.setText(meals != null ? String.valueOf(meals) : "0");

                String photoUrl = doc.getString("profileImageUrl");
                // --- FIXED PICTURE LOGIC ---
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    // If there is a photo: hide initials and yellow background tint
                    tvInitials.setVisibility(View.GONE);
                    ivProfilePic.setImageTintList(null); // Removes the #8BC34A green tint

                    Glide.with(this)
                            .load(photoUrl)
                            .placeholder(R.drawable.bg_circle_yellow) // Fallback while loading
                            .circleCrop()
                            .into(ivProfilePic);
                } else {
                    // If no photo: show initials and keep the yellow background
                    tvInitials.setVisibility(View.VISIBLE);
                    ivProfilePic.setImageResource(R.drawable.bg_circle_yellow);
                    ivProfilePic.setImageTintList(ColorStateList.valueOf(Color.parseColor("#8BC34A")));
                    setInitials(name);
                }
            }
        });

        // Get Food Count
        fStore.collection("foods").whereEqualTo("userId", targetUserId).get()
                .addOnSuccessListener(query -> tvFoodsCount.setText(String.valueOf(query.size())));
    }

    private void checkFollowStatus() {
        fStore.collection("users").document(currentUserId)
                .collection("following").document(targetUserId)
                .addSnapshotListener((doc, e) -> {
                    if (doc != null && doc.exists()) {
                        isFollowing = true;
                        ivFollowStatus.setImageResource(R.drawable.outline_person_24); // Change to your "Followed" icon
                        ivFollowStatus.setImageTintList(ColorStateList.valueOf(Color.BLACK));
                    } else {
                        isFollowing = false;
                        ivFollowStatus.setImageResource(android.R.drawable.ic_input_add);
                        ivFollowStatus.setImageTintList(ColorStateList.valueOf(Color.BLACK));
                    }
                });
    }

    private void toggleFollow() {
        DocumentReference followingRef = fStore.collection("users").document(currentUserId).collection("following").document(targetUserId);
        DocumentReference followerRef = fStore.collection("users").document(targetUserId).collection("followers").document(currentUserId);

        if (isFollowing) {
            followingRef.delete();
            followerRef.delete();
            Toast.makeText(this, "Unfollowed", Toast.LENGTH_SHORT).show();
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("timestamp", FieldValue.serverTimestamp());
            followingRef.set(data);
            followerRef.set(data);
            Toast.makeText(this, "Following", Toast.LENGTH_SHORT).show();
        }
    }

    private void listenToSocialCounts() {
        // Listen to Followers
        fStore.collection("users").document(targetUserId).collection("followers")
                .addSnapshotListener((value, e) -> {
                    if (value != null) tvFollowersCount.setText(String.valueOf(value.size()));
                });

        // Listen to Following
        fStore.collection("users").document(targetUserId).collection("following")
                .addSnapshotListener((value, e) -> {
                    if (value != null) tvFollowingCount.setText(String.valueOf(value.size()));
                });
    }

    private void setInitials(String name) {
        if (name != null && !name.isEmpty()) {
            String[] parts = name.trim().split("\\s+");
            String initials = "" + parts[0].charAt(0);
            if (parts.length > 1) initials += parts[1].charAt(0);
            tvInitials.setText(initials.toUpperCase());
        }
    }

    private void updateContributions(String uid) {
        // 1. Handle Foods Count and Total Shared Quantity
        fStore.collection("foods")
                .whereEqualTo("userId", uid)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    if (value != null) {
                        // Foods Count (how many times they added)
                        int timesAdded = value.size();
                        TextView tvFoods = findViewById(R.id.tvFoodsCountProfile);
                        if (tvFoods != null) tvFoods.setText(String.valueOf(timesAdded));

                        // Shared Count (sum of actual food quantities)
                        long totalSharedAmount = 0;
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value) {
                            Object qtyObj = doc.get("quantity");
                            if (qtyObj != null) {
                                try {
                                    if (qtyObj instanceof Number) {
                                        totalSharedAmount += ((Number) qtyObj).longValue();
                                    } else if (qtyObj instanceof String) {
                                        // If it was stored as a String, convert it to a number
                                        totalSharedAmount += Long.parseLong((String) qtyObj);
                                    }
                                } catch (NumberFormatException e) {
                                    // If the string isn't a valid number (like "5 pieces"), ignore it
                                    e.printStackTrace();
                                }
                            }
                        }

                        TextView tvShared = findViewById(R.id.tvSharedCount);
                        if (tvShared != null) tvShared.setText(String.valueOf(totalSharedAmount));
                    }
                });

        // 2. Handle Meals Saved (Completed Pickups)
        fStore.collection("requests")
                .whereEqualTo("buyerId", uid)
                .whereEqualTo("status", "completed")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        TextView tvMeals = findViewById(R.id.tvMealsSavedProfile);
                        if (tvMeals != null) tvMeals.setText(String.valueOf(value.size()));
                    }
                });
    }

}