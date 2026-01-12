package com.example.madproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {

    private Context context;
    private List<ReservationItem> list;

    public ReservationAdapter(Context context, List<ReservationItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReservationItem item = list.get(position);

        holder.title.setText(item.getFoodTitle());
        holder.code.setText("Code: " + item.getReservationId());
        holder.qty.setText("Qty: " + item.getQuantityReserved());

        // Update image loading here
        if (item.getFoodImage() != null && !item.getFoodImage().isEmpty()) {
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                    .load(item.getFoodImage())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(holder.imgFood); // Ensure your ViewHolder has this ImageView
        }

        // --- FIX SELLER NAME (Fetch actual name from 'users' collection) ---
        if (item.getSellerId() != null) {
            FirebaseFirestore.getInstance().collection("users").document(item.getSellerId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Get the "fullName" field from the user's document
                            String name = documentSnapshot.getString("fullName");
                            holder.sellerName.setText(name != null ? name : "Unknown Seller");
                        } else {
                            holder.sellerName.setText("Seller not found");
                        }
                    })
                    .addOnFailureListener(e -> {
                        holder.sellerName.setText("Error loading name");
                    });
        } else {
            holder.sellerName.setText("No Seller Info");
        }


        // --- FIX PICKUP TIME ---
        if (item.getPickupTime() != null && !item.getPickupTime().isEmpty()) {
            holder.pickupTime.setText("Pick-up: " + item.getPickupTime());
        } else {
            holder.pickupTime.setText("Pick-up: Not specified");
        }

        if (item.getTotalPrice() == 0) {
            holder.price.setText("Free");
        } else {
            holder.price.setText(String.format(Locale.US, "$%.2f", item.getTotalPrice()));
        }

        // --- TIME REMAINING LOGIC ---
        if (item.getPickupTime() != null && !item.getPickupTime().isEmpty()) {
            try {
                // 1. Get the start time (e.g., "6:00 PM") from "6:00 PM - 8:00 PM"
                String startTimeStr = item.getPickupTime().split("-")[0].trim();
                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
                Date date = sdf.parse(startTimeStr);

                Calendar now = Calendar.getInstance();
                Calendar target = Calendar.getInstance();
                Calendar timeParts = Calendar.getInstance();
                timeParts.setTime(date);

                // Merge today's date with pickup time
                target.set(Calendar.HOUR_OF_DAY, timeParts.get(Calendar.HOUR_OF_DAY));
                target.set(Calendar.MINUTE, timeParts.get(Calendar.MINUTE));
                target.set(Calendar.SECOND, 0);

                long diff = target.getTimeInMillis() - now.getTimeInMillis();

                if (diff > 0) {
                    long hours = diff / (1000 * 60 * 60);
                    long mins = (diff / (1000 * 60)) % 60;
                    holder.timeRemaining.setText(hours + "h " + mins + "m");
                    holder.timeRemaining.setTextColor(Color.parseColor("#2E7D32")); // Green
                } else {
                    holder.timeRemaining.setText("Started");
                    holder.timeRemaining.setTextColor(Color.RED);
                }
            } catch (Exception e) {
                holder.timeRemaining.setText("--");
            }
        } else {
            holder.timeRemaining.setText("N/A");
        }

        // --- STATUS VISUALS ---
        if ("redeemed".equals(item.getStatus())) {
            // Visuals for History
            holder.itemView.setAlpha(0.6f); // Fade out slightly
            holder.statusPill.setVisibility(View.INVISIBLE);
            holder.code.setText("Redeemed");
            holder.code.setTextColor(Color.parseColor("#757575")); // Grey
            holder.timeRemaining.setText("Redeemed");
            holder.timeRemaining.setTextColor(Color.parseColor("#2E7D32"));
            holder.timeRemainingLabel.setText("");

            // Disable Click
            holder.itemView.setOnClickListener(null);

        } else {
            // Visuals for Ready to Redeem
            holder.itemView.setAlpha(1.0f);
            holder.statusPill.setVisibility(View.VISIBLE);
            holder.code.setText("Ready");
            holder.code.setTextColor(Color.parseColor("#2E7D32")); // Green

            // Enable Click -> Open Ticket
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, RedemptionTicketActivity.class);
                intent.putExtra("RESERVATION_ID", item.getReservationId());
                intent.putExtra("FOOD_TITLE", item.getFoodTitle());
                intent.putExtra("SELLER_ID", item.getSellerId());
                intent.putExtra("QUANTITY", item.getQuantityReserved());
                intent.putExtra("TIMESTAMP", item.getTimestamp());
                intent.putExtra("FOOD_LOCATION", item.getLocation()); // Ensure your ReservationItem has location
                intent.putExtra("PICKUP_TIME", item.getPickupTime());
                context.startActivity(intent);
            });
        }


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, code, qty, price, sellerName, pickupTime, timeRemaining,timeRemainingLabel, statusPill;
        ImageView imgFood;
        View divider;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvFoodTitle);
            code = itemView.findViewById(R.id.tvCode); // Using this as the Status text for now
            qty = itemView.findViewById(R.id.tvQty);
            price = itemView.findViewById(R.id.tvPrice);
            sellerName = itemView.findViewById(R.id.tvSellerName);
            pickupTime = itemView.findViewById(R.id.tvDate);
            timeRemaining = itemView.findViewById(R.id.tvTimeRemaining);
            timeRemainingLabel = itemView.findViewById(R.id.tvTimeRemainingLabel);
            statusPill = itemView.findViewById(R.id.tvStatusPill);
            divider = itemView.findViewById(R.id.divider);
            imgFood = itemView.findViewById(R.id.imgFood);
        }
    }
}