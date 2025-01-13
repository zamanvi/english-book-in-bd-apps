package com.abmn.englishhub.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.abmn.englishhub.Activity.ItemDetailsActivity;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Model.ItemModel;
import com.abmn.englishhub.R;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder>{
    private final List<ItemModel> itemList;
    private final Activity activity;

    public ItemAdapter(List<ItemModel> itemList, Activity activity) {
        this.itemList = itemList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lyt_item_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ItemModel model = itemList.get(position);
        holder.bind(model, activity);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final CardView rootCV;
        private final TextView titleTV;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            rootCV = itemView.findViewById(R.id.rootCV);
            titleTV = itemView.findViewById(R.id.titleTV);
        }

        public void bind(ItemModel model, Activity activity) {
            rootCV.setOnClickListener(v-> {
                activity.startActivity(new Intent(activity, ItemDetailsActivity.class).putExtra(Constant.FROM, model.getSlug()));
            });
            titleTV.setText(model.getTitle());
        }
    }
}