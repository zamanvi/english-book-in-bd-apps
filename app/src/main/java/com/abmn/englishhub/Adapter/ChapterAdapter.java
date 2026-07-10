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

import com.abmn.englishhub.Activity.ItemActivity;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Model.ChapterModel;
import com.abmn.englishhub.R;

import java.util.List;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ViewHolder>{
    private final List<ChapterModel> chapterList;
    private final Activity activity;

    public ChapterAdapter(List<ChapterModel> chapterList, Activity activity) {
        this.chapterList = chapterList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lyt_item_chapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChapterModel model = chapterList.get(position);
        holder.bind(model, activity);
    }

    @Override
    public int getItemCount() {
        return chapterList.size();
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView rootCV;
        private final TextView titleTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rootCV = itemView.findViewById(R.id.rootCV);
            titleTV = itemView.findViewById(R.id.titleTV);
        }

        public void bind(ChapterModel model, Activity activity) {
            rootCV.setOnClickListener(v-> {
                activity.startActivity(new Intent(activity, ItemActivity.class)
                        .putExtra(Constant.FROM, model.getSlug())
                        .putExtra(Constant.FROM_TITLE, model.getTitle()));
            });
            titleTV.setText(model.getTitle());
        }
    }
}
