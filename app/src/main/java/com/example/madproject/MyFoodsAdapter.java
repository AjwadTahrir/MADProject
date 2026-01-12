package com.example.madproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MyFoodsAdapter extends RecyclerView.Adapter<MyFoodsAdapter.MyFoodViewHolder> {

    private final Context context;
    private final List<MyFoodItem> list;

    public MyFoodsAdapter(Context context, List<MyFoodItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyFoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_food, parent, false);
        return new MyFoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyFoodViewHolder holder, int position) {
        MyFoodItem item = list.get(position);

        holder.title.setText(item.getTitle());

        // Price Logic
        String originalPrice = item.getPrice();
        if (originalPrice != null && !originalPrice.equalsIgnoreCase("Free")) {
            try {
                double priceValue = Double.parseDouble(originalPrice);
                holder.price.setText(String.format(Locale.US, "$%.2f / each", priceValue));
            } catch (NumberFormatException e) {
                holder.price.setText(originalPrice);
            }
        } else {
            holder.price.setText("Free");
        }

        // --- QUANTITY LOGIC ---
        String initialQty = item.getQuantity(); // e.g. "4"
        String currentQty = item.getCurrentQuantity(); // e.g. "3"

        // Handle fallback if currentQty is missing in DB
        if (currentQty == null) currentQty = initialQty;

        if (initialQty != null && !initialQty.isEmpty()) {
            holder.quantity.setText(currentQty + "/" + initialQty + " available");
        } else {
            holder.quantity.setText("");
        }

        // --- STATUS & GREY OUT LOGIC ---
        // Grey out if inactive OR quantity is 0
        boolean isInactive = (item.getStatus() != null && !item.getStatus().equalsIgnoreCase("active"))
                || (currentQty != null && currentQty.equals("0"));

        if (isInactive) {
            holder.status.setText("inactive");
            holder.status.setTextColor(Color.parseColor("#757575")); // Grey text
            holder.status.setBackgroundResource(R.drawable.bg_pill_sold); // Grey bg
            holder.itemView.setAlpha(0.6f);
        } else {
            holder.status.setText("active");
            holder.status.setTextColor(Color.parseColor("#2E7D32"));
            holder.status.setBackgroundResource(R.drawable.bg_pill_active);
            holder.itemView.setAlpha(1.0f);
        }

        // Date & Image
        long timestamp = item.getTimestamp();
        if (timestamp != 0) {
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(timestamp);
            String date = DateFormat.format("MMM dd", cal).toString();
            holder.date.setText(date);
        } else {
            holder.date.setText("");
        }

        // --- LOAD IMAGE WITH GLIDE ---
        if (item.getImageUri() != null && !item.getImageUri().isEmpty()) {
            Glide.with(context)
                    .load(item.getImageUri())
                    .placeholder(R.drawable.ic_launcher_foreground) // Default while loading
                    .error(android.R.drawable.stat_notify_error)   // If link fails
                    .centerCrop() // Makes image look consistent in the card
                    .into(holder.imgFood);
        } else {
            holder.imgFood.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // --- CLICK LISTENER ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FoodDetailsActivity.class);
            intent.putExtra("FOOD_TITLE", item.getTitle());
            intent.putExtra("FOOD_PRICE", item.getPrice());
            intent.putExtra("FOOD_DESC", item.getDescription());
            intent.putExtra("FOOD_LOCATION", item.getLocation());
            intent.putExtra("FOOD_IMAGE_URI", item.getImageUri());

            // --- SEND QUANTITIES ---
            intent.putExtra("FOOD_QUANTITY_INITIAL", item.getQuantity());
            intent.putExtra("FOOD_QUANTITY_CURRENT", item.getCurrentQuantity());

            // Also send simple quantity for safety
            intent.putExtra("FOOD_QUANTITY", item.getQuantity());

            // --- SEND IDs ---
            intent.putExtra("FOOD_ID", item.getFoodId());
            intent.putExtra("FOOD_OWNER_ID", item.getUserId());

            // Inside MyFoodsAdapter.java -> onBindViewHolder -> setOnClickListener
            intent.putExtra("FOOD_PICKUP_TIME", item.getPickupTime()); // <--- ADD THIS

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyFoodViewHolder extends RecyclerView.ViewHolder {
        TextView title, price, quantity, status, date;
        ImageView imgFood;

        public MyFoodViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvMyFoodTitle);
            price = itemView.findViewById(R.id.tvMyFoodPrice);
            quantity = itemView.findViewById(R.id.tvMyFoodQuantity);
            status = itemView.findViewById(R.id.tvStatus);
            date = itemView.findViewById(R.id.tvDate);
            imgFood = itemView.findViewById(R.id.imgMyFood);
        }
    }
}