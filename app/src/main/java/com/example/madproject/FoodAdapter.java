package com.example.madproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private List<FoodItem> list; // Changed to FoodItem
    private OnItemClickListener listener;

    public FoodAdapter(List<FoodItem> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_card, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem item = list.get(position);

        holder.title.setText(item.getTitle());
        holder.price.setText(item.getPrice());
        com.bumptech.glide.Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl()) // Ensure your FoodItem class has getImageUrl()
                .placeholder(R.drawable.ic_launcher_background) // Shown while loading
                .error(android.R.drawable.stat_notify_error)      // Shown if URL fails
                .centerCrop()
                .into(holder.pic);
        // 3. SET CLICK LISTENER
        holder.itemView.setOnClickListener(v -> {
            listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView title, price;
        ImageView pic;
        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            price = itemView.findViewById(R.id.tvPrice);
            pic = itemView.findViewById(R.id.imgFood);
        }
    }

    // 4. THE INTERFACE
    public interface OnItemClickListener {
        void onItemClick(FoodItem item);
    }
}
