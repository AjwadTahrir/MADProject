package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    EditText etFullName, etEmail, etPhone, etAddress, etPassword;
    Button btnRegister;
    TextView tvLoginLink;

    // Firebase Variables
    FirebaseAuth mAuth;
    FirebaseFirestore fStore; // Changed from FirebaseDatabase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Hooks to XML
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        // Initialize Firebase Auth & Firestore
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        btnRegister.setOnClickListener(v -> {
            String name = etFullName.getText().toString();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString();
            String address = etAddress.getText().toString();
            String password = etPassword.getText().toString().trim();

            if(TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(SignUpActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Create User in Auth
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    // 2. Save User Info to Firestore
                    String userId = mAuth.getCurrentUser().getUid();

                    // We create a Document Reference: "users" collection -> "userId" document
                    DocumentReference documentReference = fStore.collection("users").document(userId);

                    // Create data object (You can use a Map OR your UserHelperClass)
                    // Using Map is often safer for Firestore specific updates, but Class works too.
                    Map<String, Object> user = new HashMap<>();
                    user.put("fullName", name);
                    user.put("email", email);
                    user.put("phone", phone);
                    user.put("address", address);

                    // Write to database
                    documentReference.set(user).addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "onSuccess: user Profile is created for " + userId);
                        Toast.makeText(SignUpActivity.this, "Account Created.", Toast.LENGTH_SHORT).show();

                        // Go to Home
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();

                    }).addOnFailureListener(e -> {
                        Log.d(TAG, "onFailure: " + e.toString());
                        Toast.makeText(SignUpActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

                } else {
                    Toast.makeText(SignUpActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        tvLoginLink.setOnClickListener(v -> finish());
    }
}