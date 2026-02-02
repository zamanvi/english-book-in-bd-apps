package com.abmn.englishhub.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abmn.englishhub.Activity.ChapterActivity;
import com.abmn.englishhub.Activity.VocabularyActivity;
import com.abmn.englishhub.R;


public class HomeFragment extends Fragment {

    private Activity activity;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        define(view);

        return view;
    }

    private void define(View view) {

        activity = getActivity();

        TextView grammarTV = view.findViewById(R.id.grammarTV);
        TextView dailyVocabularyTV = view.findViewById(R.id.dailyVocabularyTV);
        TextView writingAndReadingTV = view.findViewById(R.id.writingAndReadingTV);
        LinearLayout grammarLL = view.findViewById(R.id.grammarLL);
        LinearLayout dailyVocabularyLL = view.findViewById(R.id.dailyVocabularyLL);
        LinearLayout writingAndReadingLL = view.findViewById(R.id.writingAndReadingLL);
        grammarTV.setOnClickListener(v -> startActivity(new Intent(activity, ChapterActivity.class).putExtra("type", "grammar")));
        dailyVocabularyTV.setOnClickListener(v -> startActivity(new Intent(activity, ChapterActivity.class).putExtra("type", "daily_vocabulary")));
        writingAndReadingTV.setOnClickListener(v -> startActivity(new Intent(activity, ChapterActivity.class).putExtra("type", "writing_reading")));
        grammarLL.setOnClickListener(v -> startActivity(new Intent(activity, ChapterActivity.class).putExtra("type", "grammar")));
        dailyVocabularyLL.setOnClickListener(v -> startActivity(new Intent(activity, ChapterActivity.class).putExtra("type", "daily_vocabulary")));
        writingAndReadingLL.setOnClickListener(v -> startActivity(new Intent(activity, ChapterActivity.class).putExtra("type", "writing_reading")));

        TextView vocabularyTV = view.findViewById(R.id.vocabularyTV);
        LinearLayout vocabularyLL = view.findViewById(R.id.vocabularyLL);
        vocabularyTV.setOnClickListener(v -> startActivity(new Intent(activity, VocabularyActivity.class)));
        vocabularyLL.setOnClickListener(v -> startActivity(new Intent(activity, VocabularyActivity.class)));
    }
}