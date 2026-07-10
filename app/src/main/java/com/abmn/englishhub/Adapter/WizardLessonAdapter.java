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

import com.abmn.englishhub.Activity.WizardStoryActivity;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Model.WizardStoryModel;
import com.abmn.englishhub.R;

import java.util.List;

public class WizardLessonAdapter extends RecyclerView.Adapter<WizardLessonAdapter.ViewHolder> {

    private final List<WizardStoryModel> storyList;
    private final Activity activity;

    public WizardLessonAdapter(List<WizardStoryModel> storyList, Activity activity) {
        this.storyList = storyList;
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
        holder.bind(storyList.get(position), activity);
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView rootCV;
        private final TextView titleTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rootCV = itemView.findViewById(R.id.rootCV);
            titleTV = itemView.findViewById(R.id.titleTV);
        }

        public void bind(WizardStoryModel model, Activity activity) {
            rootCV.setOnClickListener(v -> activity.startActivity(
                    new Intent(activity, WizardStoryActivity.class)
                            .putExtra(Constant.FROM_ID, model.getId())));
            titleTV.setText(model.getHookTitle());
        }
    }
}
