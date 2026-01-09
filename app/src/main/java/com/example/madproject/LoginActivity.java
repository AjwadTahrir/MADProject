package com.example.madproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvSignUp = findViewById(R.id.tvSignUp);

        // 1. Handle Login Click
        btnLogin.setOnClickListener(v -> {
            // In a real app, you would check Email/Password here
            Toast.makeText(this, "Welcome Back!", Toast.LENGTH_SHORT).show();

            // Navigate to Home Screen
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Prevents user from going back to login screen
        });

        // 2. Handle Sign Up Click

        tvSignUp.setOnClickListener(v -> {
            // OLD CODE: Toast.makeText(...).show();

            // NEW CODE:
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}