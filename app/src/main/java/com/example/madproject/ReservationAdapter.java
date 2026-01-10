package com.example.madproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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

        if (item.getTotalPrice() == 0) {
            holder.price.setText("Free");
        } else {
            holder.price.setText(String.format(Locale.US, "$%.2f", item.getTotalPrice()));
        }

        // --- STATUS VISUALS ---
        if ("redeemed".equals(item.getStatus())) {
            // Visuals for History
            holder.itemView.setAlpha(0.6f); // Fade out slightly
            holder.code.setText("Redeemed");
            holder.code.setTextColor(Color.parseColor("#757575")); // Grey

            // Disable Click
            holder.itemView.setOnClickListener(null);

        } else {
            // Visuals for Ready to Redeem
            holder.itemView.setAlpha(1.0f);
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
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, code, qty, price;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvFoodTitle);
            code = itemView.findViewById(R.id.tvCode); // Using this as the Status text for now
            qty = itemView.findViewById(R.id.tvQty);
            price = itemView.findViewById(R.id.tvPrice);
        }
    }
}