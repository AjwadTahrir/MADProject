package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView; // ADDED: Missing import
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth; // ADDED: Missing import
import com.google.firebase.firestore.FirebaseFirestore; // ADDED: Missing import

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 1. Initialize TextViews
        TextView tvProfileName = findViewById(R.id.tvName);
        TextView tvProfileEmail = findViewById(R.id.tvProfileEmail);
        TextView tvAvatarInitials = findViewById(R.id.tvAvatarInitials);
        // 2. Fetch User Data from Firestore
        // Ensure the user is logged in before getting UID to avoid crashes
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Sets the name and email saved during SignUp
                            String name = documentSnapshot.getString("name");
                            String email = documentSnapshot.getString("email");
                            if (name != null) {
                                tvProfileName.setText(name);
                                // Update avatar initials (e.g., Michael -> M)
                                if (!name.isEmpty()) {
                                    tvAvatarInitials.setText(name.substring(0, 1).toUpperCase());
                                }
                            }
                            if (email != null) {
                                tvProfileEmail.setText(email);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show();
                    });
        }

        // 3. Setup Bottom Navigation Logic
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
            } else if (itemId == R.id.nav_profile) {
                return true; // Stay on current page
            }
            return false;
        });

        // 4. Logout Button
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // CRITICAL FIX: You must tell Firebase to sign out
            FirebaseAuth.getInstance().signOut();

            Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}