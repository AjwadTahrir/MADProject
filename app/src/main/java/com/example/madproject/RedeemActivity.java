package com.example.madproject;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.widget.SeekBar;
import androidx.appcompat.app.AppCompatActivity;

public class RedeemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redeem);

        SeekBar slider = findViewById(R.id.sliderRedeem);

        // Reset slider to 0 at start
        slider.setProgress(0);

        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // If dragged near the end (e.g., > 95%)
                if (progress > 95) {
                    // Go to Success Screen
                    Intent intent = new Intent(RedeemActivity.this, SuccessActivity.class);
                    startActivity(intent);
                    finish(); // Close this screen so they can't go back
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // If they let go before reaching the end, snap back to 0
                if (seekBar.getProgress() <= 95) {
                    seekBar.setProgress(0);
                }
            }
        });
    }
}