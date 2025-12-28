package com.example.madproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    List<ActivityItem> list;

    public ActivityAdapter(List<ActivityItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityItem item = list.get(position);
        holder.tvAction.setText(item.action);
        holder.tvDetails.setText(item.details);
        holder.tvTime.setText(item.time);
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvAction, tvDetails, tvTime;
        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAction = itemView.findViewById(R.id.tvAction);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}