package com.example.madproject;

import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends AppCompatActivity {

    private Button btnSave, btnCancel;
    private EditText etFullName, etEmail, etPhoneNumber, etAddress, etBio;

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

        // 2. Setup Back Buttons
        findViewById(R.id.UpBackButton).setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());

        // 3. Pre-fill data if passed from ProfileActivity
        if(getIntent() != null) {
            if(getIntent().hasExtra("fullName")) etFullName.setText(getIntent().getStringExtra("fullName"));
            if(getIntent().hasExtra("email")) etEmail.setText(getIntent().getStringExtra("email"));
            if(getIntent().hasExtra("phone")) etPhoneNumber.setText(getIntent().getStringExtra("phone"));
            if(getIntent().hasExtra("address")) etAddress.setText(getIntent().getStringExtra("address"));
        }

        // 4. Handle "Change Photo" Click
        tvChangeProfile.setOnClickListener(v -> {
            setUnderline(tvChangeProfile, true);
            Toast.makeText(EditProfileActivity.this, "Opening photo options...", Toast.LENGTH_SHORT).show();
            tvChangeProfile.postDelayed(() -> setUnderline(tvChangeProfile, false), 500);
        });

        // Handle Hover (Mouse/Emulator)
        tvChangeProfile.setOnGenericMotionListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    setUnderline((TextView) v, true);
                    return true;
                case MotionEvent.ACTION_HOVER_EXIT:
                    setUnderline((TextView) v, false);
                    return true;
            }
            return false;
        });

        // 5. Setup Text Filters and Watchers
        // Forces a hard limit of 200 characters for Bio
        etBio.setFilters(new InputFilter[] { new InputFilter.LengthFilter(200) });

        // Enable Save button when text changes
        TextWatcher editWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnSave.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etFullName.addTextChangedListener(editWatcher);
        etEmail.addTextChangedListener(editWatcher);
        etPhoneNumber.addTextChangedListener(editWatcher);
        etAddress.addTextChangedListener(editWatcher);
        etBio.addTextChangedListener(editWatcher);
    }

    /**
     * Helper method to toggle the underline
     */
    private void setUnderline(TextView textView, boolean isUnderlined) {
        if (isUnderlined) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
        }
    }
}