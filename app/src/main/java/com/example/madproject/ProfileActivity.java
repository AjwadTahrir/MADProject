package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class ProfileActivity extends AppCompatActivity {

    private com.google.firebase.firestore.ListenerRegistration profileListener;
    TextView tvName, tvEmail, tvPhone, tvAddress, tvInitials, tvMealsSaved, tvFoodsCount, tvBio;
    android.widget.ImageView imgAvatarProfile;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;
    String currentImageUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 1. Initialize TextViews
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        tvInitials = findViewById(R.id.tvInitials);
        tvMealsSaved = findViewById(R.id.tvMealsSavedProfile);
        tvFoodsCount = findViewById(R.id.tvFoodsCountProfile);
        tvBio = findViewById(R.id.tvBioProfile);
        imgAvatarProfile = findViewById(R.id.layoutAvatar).findViewById(R.id.ivProfilePic);

        // 2. Initialize Firebase
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        if (fAuth.getCurrentUser() == null) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
            return;
        }

        userId = fAuth.getCurrentUser().getUid();
        listenToFoodCount();
        listenForFoodRequests();
        listenToSocialCounts();
        updateContributions(userId);


        // 3. Fetch Data
        DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) return;
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("fullName");
                    tvName.setText(name);
                    tvEmail.setText(documentSnapshot.getString("email"));
                    tvPhone.setText(documentSnapshot.getString("phone"));
                    tvAddress.setText(documentSnapshot.getString("address"));

                    // ADD THIS: Fetch Bio from Firestore
                    String bio = documentSnapshot.getString("bio");
                    if (bio != null && !bio.isEmpty()) {
                        tvBio.setText(bio);
                    } else {
                        tvBio.setText("Food Saver"); // Default if bio is empty
                    }

                    // --- ADD THIS: FETCH MEALS SAVED ---
                    Long mealsSaved = documentSnapshot.getLong("mealsSaved");
                    if (tvMealsSaved != null) {
                        tvMealsSaved.setText(mealsSaved != null ? String.valueOf(mealsSaved) : "0");
                    }

                    if (name != null && !name.isEmpty()) {
                        String initials = "";
                        String[] splitName = name.split(" ");
                        if (splitName.length > 0) initials += splitName[0].charAt(0);
                        if (splitName.length > 1) initials += splitName[1].charAt(0);
                        tvInitials.setText(initials.toUpperCase());
                    }
                }
            }
        });

        // 4. Edit Profile Button
        Button btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            intent.putExtra("fullName", tvName.getText().toString());
            intent.putExtra("email", tvEmail.getText().toString());
            intent.putExtra("phone", tvPhone.getText().toString());
            intent.putExtra("address", tvAddress.getText().toString());
            intent.putExtra("bio", tvBio.getText().toString());
            intent.putExtra("profileImageUrl", currentImageUrl);
            startActivity(intent);
        });

        // 5. REDEEM BUTTON - Links to MyReservationsActivity
        Button btnRedeem = findViewById(R.id.btnRedeem);
        btnRedeem.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MyReservationsActivity.class);
            startActivity(intent);
        });

        // 6. Logout Button
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });

        // 7. Bottom Nav
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_search) {
                startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_foods) {
                startActivity(new Intent(getApplicationContext(), FoodsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return itemId == R.id.nav_profile;
        });
    }

    private void listenToFoodCount() {
        fStore.collection("foods")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        // This counts how many food documents you have posted
                        int count = value.size();
                        tvFoodsCount.setText(String.valueOf(count));
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume(); // Fixed: must be super.onResume()
        startListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (profileListener != null) {
            profileListener.remove(); // Stops listening when screen is hidden
        }
    }

    private void startListening() {
        DocumentReference docRef = fStore.collection("users").document(userId);

        profileListener = docRef.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null) return;
            if (documentSnapshot != null && documentSnapshot.exists()) {
                String name = documentSnapshot.getString("fullName");
                tvName.setText(name);
                tvEmail.setText(documentSnapshot.getString("email"));
                tvPhone.setText(documentSnapshot.getString("phone"));
                tvAddress.setText(documentSnapshot.getString("address"));

                String bio = documentSnapshot.getString("bio");
                tvBio.setText(bio != null && !bio.isEmpty() ? bio : "Food Saver");

                Long mealsSaved = documentSnapshot.getLong("mealsSaved");
                tvMealsSaved.setText(mealsSaved != null ? String.valueOf(mealsSaved) : "0");

                // 2. UPDATE THE VARIABLE HERE
                currentImageUrl = documentSnapshot.getString("profileImageUrl");

                if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                    // A. WE HAVE A PHOTO
                    tvInitials.setVisibility(View.GONE);
                    imgAvatarProfile.setImageTintList(null); // REMOVE TINT

                    Glide.with(this)
                            .load(currentImageUrl)
                            .circleCrop()
                            // Force refresh so it doesn't show the old photo
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(imgAvatarProfile);
                } else {
                    // B. NO PHOTO (Default Initials Mode)
                    tvInitials.setVisibility(View.VISIBLE);
                    // Add the green tint back (using your color hex or resource)
                    imgAvatarProfile.setImageTintList(android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#8BC34A")));
                }

                if (name != null && !name.isEmpty()) {
                    String initials = "";
                    String[] splitName = name.trim().split("\\s+");
                    if (splitName.length > 0) initials += splitName[0].charAt(0);
                    if (splitName.length > 1) initials += splitName[1].charAt(0);
                    tvInitials.setText(initials.toUpperCase());
                }
            }
        });
        listenToFoodCount();
    }

    private void listenForFoodRequests() {
        LinearLayout dynamicList = findViewById(R.id.dynamicRequestList);
        TextView tvBadge = findViewById(R.id.tvRequestBadge);
        androidx.cardview.widget.CardView cardRequests = findViewById(R.id.cardRequests);

        fStore.collection("reservations")
                .whereEqualTo("sellerId", userId)
                .whereEqualTo("status", "pending")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    if (value != null && !value.isEmpty()) {
                        cardRequests.setVisibility(View.VISIBLE);
                        tvBadge.setText(String.valueOf(value.size()));

                        dynamicList.removeAllViews();

                        for (DocumentSnapshot doc : value.getDocuments()) {
                            String foodTitle = doc.getString("foodTitle");
                            String buyerId = doc.getString("buyerId");

                            View requestRow = getLayoutInflater().inflate(R.layout.item_request_row, null);

                            ImageView ivRequesterPhoto = requestRow.findViewById(R.id.ivRequesterPhoto);
                            TextView tvRowInitials = requestRow.findViewById(R.id.tvRowInitials);
                            TextView nameTxt = requestRow.findViewById(R.id.tvRequesterName);
                            TextView detailTxt = requestRow.findViewById(R.id.tvRequestDetail);
                            // FIND THE NEW PHONE VIEW
                            TextView phoneTxt = requestRow.findViewById(R.id.tvRequesterPhone);

                            fStore.collection("users").document(buyerId).get()
                                    .addOnSuccessListener(userDoc -> {
                                        if (userDoc.exists()) {
                                            String fullName = userDoc.getString("fullName");
                                            String photoUrl = userDoc.getString("profileImageUrl");
                                            // GET PHONE FROM FIRESTORE
                                            String phone = userDoc.getString("phone");

                                            nameTxt.setText(fullName);
                                            detailTxt.setText("requested " + foodTitle);

                                            // SET THE PHONE TEXT
                                            if (phone != null && !phone.isEmpty()) {
                                                phoneTxt.setText(phone);
                                            } else {
                                                phoneTxt.setText("No phone provided");
                                            }

                                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                                tvRowInitials.setVisibility(View.GONE);
                                                ivRequesterPhoto.setVisibility(View.VISIBLE);
                                                Glide.with(ProfileActivity.this).load(photoUrl).circleCrop().into(ivRequesterPhoto);
                                            } else {
                                                ivRequesterPhoto.setVisibility(View.GONE);
                                                tvRowInitials.setVisibility(View.VISIBLE);
                                                if (fullName != null && !fullName.isEmpty()) {
                                                    String[] parts = fullName.trim().split("\\s+");
                                                    String initials = "" + parts[0].charAt(0);
                                                    if (parts.length > 1) initials += parts[1].charAt(0);
                                                    tvRowInitials.setText(initials.toUpperCase());
                                                }
                                            }

                                            // 1. Define the action
                                            View.OnClickListener openProfile = v -> {
                                                Intent intent = new Intent(ProfileActivity.this, UserViewActivity.class);
                                                intent.putExtra("TARGET_USER_ID", buyerId); // Pass the requester's ID
                                                startActivity(intent);
                                            };

                                            // 2. Attach it to the views you want clickable
                                            ivRequesterPhoto.setOnClickListener(openProfile);
                                            tvRowInitials.setOnClickListener(openProfile);
                                            nameTxt.setOnClickListener(openProfile);

                                            // Optional: Make the whole row clickable
                                            //requestRow.setOnClickListener(openProfile);
                                        }
                                    });
                            dynamicList.addView(requestRow);
                        }
                    } else {
                        cardRequests.setVisibility(View.GONE);
                    }
                });
    }

    private void listenToSocialCounts() {
        // 1. Listen to YOUR Followers
        fStore.collection("users").document(userId).collection("followers")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        // Find the TextView for Followers in your activity_profile.xml
                        // Make sure the ID matches what you have in your XML
                        TextView tvFollowers = findViewById(R.id.tvFollowersCount);
                        if (tvFollowers != null) tvFollowers.setText(String.valueOf(value.size()));
                    }
                });

        // 2. Listen to who YOU are Following
        fStore.collection("users").document(userId).collection("following")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        TextView tvFollowing = findViewById(R.id.tvFollowingCount);
                        if (tvFollowing != null) tvFollowing.setText(String.valueOf(value.size()));
                    }
                });
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