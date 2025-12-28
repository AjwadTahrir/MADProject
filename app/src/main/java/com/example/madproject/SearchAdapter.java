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

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private Context context;
    private List<SearchFoodItem> searchList;
    private OnItemClickListener listener; // <--- 1. NEW LISTENER VARIABLE

    // 2. UPDATED CONSTRUCTOR
    public SearchAdapter(Context context, List<SearchFoodItem> searchList, OnItemClickListener listener) {
        this.context = context;
        this.searchList = searchList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_food, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        SearchFoodItem item = searchList.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvCategory.setText(item.getCategory());
        holder.tvLocation.setText(item.getLocation());
        holder.tvPrice.setText(item.getPrice());
        holder.tvRating.setText(String.valueOf(item.getRating()));
        holder.imgFood.setImageResource(item.getImageResId());

        if (item.getPrice().equalsIgnoreCase("Free")) {
            holder.tvPrice.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvPrice.setTextColor(0xFF00897B);
        }

        // 3. CLICK LISTENER (The Magic Part)
        holder.itemView.setOnClickListener(v -> {
            listener.onItemClick(item); // Pass the clicked item back to the Activity
        });
    }

    @Override
    public int getItemCount() { return searchList.size(); }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvLocation, tvPrice, tvRating;
        ImageView imgFood;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvRating = itemView.findViewById(R.id.tvRating);
            imgFood = itemView.findViewById(R.id.imgFood);
        }
    }

    // 4. THE INTERFACE
    public interface OnItemClickListener {
        void onItemClick(SearchFoodItem item);
    }
}
