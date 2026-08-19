package com.abmn.englishhub.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.abmn.englishhub.Activity.ChapterActivity;
import com.abmn.englishhub.Activity.VocabularyActivity;
import com.abmn.englishhub.R;

public class LearnFragment extends Fragment {

    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_learn, container, false);
        com.abmn.englishhub.Helper.WindowInsetsHelper.applyStatusBarInsets(view);
        activity = getActivity();

        view.findViewById(R.id.vocabCard).setOnClickListener(v ->
                startActivity(new Intent(activity, VocabularyActivity.class)));

        view.findViewById(R.id.grammarCard).setOnClickListener(v ->
                startActivity(new Intent(activity, ChapterActivity.class)
                        .putExtra("type", "grammar")));

        view.findViewById(R.id.speakingCard).setOnClickListener(v ->
                startActivity(new Intent(activity, ChapterActivity.class)
                        .putExtra("type", "daily_vocabulary")));

        view.findViewById(R.id.writingCard).setOnClickListener(v ->
                startActivity(new Intent(activity, ChapterActivity.class)
                        .putExtra("type", "writing_reading")));

        return view;
    }
}
