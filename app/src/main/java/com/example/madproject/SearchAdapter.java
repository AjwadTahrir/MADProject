package com.example.madproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private Context context;
    private List<SearchFoodItem> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SearchFoodItem item);
    }

    public SearchAdapter(Context context, List<SearchFoodItem> list, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate using R.layout, not R.id
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_card, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        SearchFoodItem item = list.get(position);

        // Set text data
        holder.title.setText(item.getTitle());
        holder.price.setText(item.getPrice());

        // Improved Glide Loading
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getImageUrl())
                    .centerCrop() // Ensures the image fills the square nicely
                    .placeholder(R.drawable.ic_launcher_background) // While loading
                    .error(android.R.drawable.stat_notify_error)   // If link is broken
                    .into(holder.pic);
        } else {
            // Fallback if the database has no URL
            holder.pic.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {
        TextView title, price;
        ImageView pic;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            price = itemView.findViewById(R.id.tvPrice);
            pic = itemView.findViewById(R.id.imgFood);
        }
    }
}