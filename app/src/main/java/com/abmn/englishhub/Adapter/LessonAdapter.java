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

import com.abmn.englishhub.Activity.LevelMapActivity;
import com.abmn.englishhub.Activity.WordActivity;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Model.LessonModel;
import com.abmn.englishhub.R;

import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.BlogViewHolder> {

    private final List<LessonModel> lessonList;
    private final Activity activity;
    private final String chapterType;
    private final boolean quizPickerMode;

    public LessonAdapter(List<LessonModel> lessonList, Activity activity, String chapterType) {
        this(lessonList, activity, chapterType, false);
    }

    public LessonAdapter(List<LessonModel> lessonList, Activity activity, String chapterType, boolean quizPickerMode) {
        this.lessonList = lessonList;
        this.activity = activity;
        this.chapterType = chapterType;
        this.quizPickerMode = quizPickerMode;
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lyt_item_item, parent, false);
        return new BlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        LessonModel model = lessonList.get(position);
        holder.bind(model, activity, chapterType, quizPickerMode);
    }

    @Override
    public int getItemCount() {
        return lessonList.size();
    }
    static class BlogViewHolder extends RecyclerView.ViewHolder {
        private final CardView rootCV;
        private final TextView titleTV;
        private final TextView premiumBadgeTV;
        private final CardView quickQuizBtnCV;

        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            rootCV = itemView.findViewById(R.id.rootCV);
            titleTV = itemView.findViewById(R.id.titleTV);
            premiumBadgeTV = itemView.findViewById(R.id.premiumBadgeTV);
            quickQuizBtnCV = itemView.findViewById(R.id.quickQuizBtnCV);
        }

        public void bind(LessonModel model, Activity activity, String chapterType, boolean quizPickerMode) {
            // use lesson's own type if set, otherwise fall back to chapter type
            String lessonType = (model.getChapter_type() != null && !model.getChapter_type().isEmpty())
                    ? model.getChapter_type() : chapterType;

            // Row always opens the word list to read, and a quick action
            // still lets you jump straight into the level map for this lesson -
            // even when reached via the "Quick Quiz" home shortcut, so picking
            // a lesson there doesn't cut off the ability to read it first.
            rootCV.setOnClickListener(v -> activity.startActivity(new Intent(activity, WordActivity.class)
                    .putExtra(Constant.FROM, "" + model.getId())
                    .putExtra(Constant.FROM_TITLE, model.getTitle())
                    .putExtra(Constant.FROM_TYPE, lessonType)));
            // Premium+locked lessons now get blocked server-side anyway,
            // but there's no point offering a shortcut that just dead-ends
            // in an error toast - send those users through Word list to
            // unlock first instead.
            if (model.isPremium()) {
                quickQuizBtnCV.setVisibility(View.GONE);
            } else {
                quickQuizBtnCV.setVisibility(View.VISIBLE);
                quickQuizBtnCV.setOnClickListener(v -> activity.startActivity(
                        new Intent(activity, LevelMapActivity.class)
                                .putExtra("lesson_id", model.getId())));
            }

            titleTV.setText(model.getTitle());
            premiumBadgeTV.setVisibility(model.isPremium() ? View.VISIBLE : View.GONE);
        }
    }
}
