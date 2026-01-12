package com.example.madproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class MyReservationsActivity extends AppCompatActivity {

    // Views
    RecyclerView recyclerReady, recyclerRedeemed;
    TextView tvTotalCount, tvHeaderReady, tvHeaderRedeemed;

    // Adapters & Lists
    ReservationAdapter adapterReady, adapterRedeemed;
    List<ReservationItem> listReady, listRedeemed;

    // Firebase
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reservations);

        // 1. Initialize Views
        recyclerReady = findViewById(R.id.recyclerReady);
        recyclerRedeemed = findViewById(R.id.recyclerRedeemed);
        tvHeaderReady = findViewById(R.id.tvHeaderReady);
        tvHeaderRedeemed = findViewById(R.id.tvHeaderRedeemed);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 2. Setup Firebase
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        // 3. Setup Lists & Adapters
        listReady = new ArrayList<>();
        listRedeemed = new ArrayList<>();

        adapterReady = new ReservationAdapter(this, listReady);
        adapterRedeemed = new ReservationAdapter(this, listRedeemed);

        recyclerReady.setLayoutManager(new LinearLayoutManager(this));
        recyclerReady.setAdapter(adapterReady);

        recyclerRedeemed.setLayoutManager(new LinearLayoutManager(this));
        recyclerRedeemed.setAdapter(adapterRedeemed);

        // 4. Load Data
        if (userId != null) {
            loadReadyItems();
            loadRedeemedItems();
        }
    }

    // --- LISTENER 1: READY ITEMS (Status = pending) ---
    private void loadReadyItems() {
        fStore.collection("reservations")
                .whereEqualTo("buyerId", userId)
                .whereEqualTo("status", "pending")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("FirestoreError", "Error: " + error.getMessage());
                            return;
                        }

                        // This clears the list so you don't get duplicates
                        listReady.clear();

                        if (value != null && !value.isEmpty()) {
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                ReservationItem item = doc.toObject(ReservationItem.class);
                                if (item != null) {
                                    listReady.add(item);
                                }
                            }
                        } else {
                            // If value is null or empty, it means the database is empty!
                            Log.d("Firestore", "No pending reservations found.");
                        }

                        adapterReady.notifyDataSetChanged();
                        updateCounts();
                    }
                });
    }

    // --- LISTENER 2: REDEEMED ITEMS (Status = redeemed) ---
    private void loadRedeemedItems() {
        fStore.collection("reservations")
                .whereEqualTo("buyerId", userId)
                .whereEqualTo("status", "redeemed")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Newest first
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) return;

                        listRedeemed.clear();
                        if (value != null) {
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                listRedeemed.add(doc.toObject(ReservationItem.class));
                            }
                        }
                        adapterRedeemed.notifyDataSetChanged();
                        updateCounts();
                    }
                });
    }

    // --- UPDATE UI COUNTS ---
    private void updateCounts() {
        int readyCount = listReady.size();
        int redeemedCount = listRedeemed.size();
        int total = readyCount + redeemedCount;


        tvHeaderReady.setText("Ready to Redeem (" + readyCount + ")");
        tvHeaderRedeemed.setText("Redeemed History (" + redeemedCount + ")");
    }
}