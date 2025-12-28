package com.example.madproject;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MyFoodsAdapter extends RecyclerView.Adapter<MyFoodsAdapter.MyFoodViewHolder> {

    private Context context;
    private List<MyFoodItem> list;

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
        holder.price.setText(item.getPrice());
        holder.date.setText(item.getDate());
        holder.status.setText(item.getStatus());
        holder.img.setImageResource(item.getImageRes());

        // Change Pill Color based on Status
        if (item.getStatus().equalsIgnoreCase("active")) {
            holder.status.setTextColor(Color.parseColor("#2E7D32")); // Green Text
            holder.status.setBackgroundResource(R.drawable.bg_pill_active);
            holder.price.setText(item.getPrice());
        } else {
            holder.status.setTextColor(Color.parseColor("#757575")); // Gray Text
            holder.status.setBackgroundResource(R.drawable.bg_pill_sold);
            // Optional: Hide price if sold, or keep it
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class MyFoodViewHolder extends RecyclerView.ViewHolder {
        TextView title, price, status, date;
        ImageView img;

        public MyFoodViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvMyFoodTitle);
            price = itemView.findViewById(R.id.tvMyFoodPrice);
            status = itemView.findViewById(R.id.tvStatus);
            date = itemView.findViewById(R.id.tvDate);
            img = itemView.findViewById(R.id.imgMyFood);
        }
    }
}