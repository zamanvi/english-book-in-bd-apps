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

import com.abmn.englishhub.Activity.WordActivity;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Model.LessonModel;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;

import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.BlogViewHolder> {

    private final List<LessonModel> lessonList;
    private final Activity activity;

    public LessonAdapter(List<LessonModel> lessonList, Activity activity) {
        this.lessonList = lessonList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lyt_item_lesson, parent, false);
        return new BlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        LessonModel model = lessonList.get(position);
        holder.bind(model, activity);
    }

    @Override
    public int getItemCount() {
        return lessonList.size();
    }
    static class BlogViewHolder extends RecyclerView.ViewHolder {
        private final CardView rootCV;
        private final TextView titleTV;
        private final TextView planTV;

        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            rootCV = itemView.findViewById(R.id.rootCV);
            titleTV = itemView.findViewById(R.id.titleTV);
            planTV = itemView.findViewById(R.id.planTV);
        }

        public void bind(LessonModel model, Activity activity) {
            UConfig uConfig = new UConfig(activity);
            rootCV.setOnClickListener(v-> activity.startActivity(new Intent(activity, WordActivity.class).putExtra(Constant.FROM, "" + model.getId()).putExtra(Constant.FROM_TITLE, model.getTitle()).putExtra(Constant.FROM_TYPE, model.getChapter_type())));
            titleTV.setText(model.getTitle());
            planTV.setText(model.getPlan());
        }
    }
}