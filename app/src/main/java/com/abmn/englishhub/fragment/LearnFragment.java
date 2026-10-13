package com.abmn.englishhub.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.abmn.englishhub.Activity.ChapterActivity;
import com.abmn.englishhub.Activity.VocabularyChapterActivity;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Activity.WizardChapterActivity;
import com.abmn.englishhub.R;

public class LearnFragment extends Fragment {

    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_learn, container, false);
        // MainActivity already pads its root by the system-bar insets; applying
        // them again here doubled the gap under the toolbar on every tab.
        activity = getActivity();

        // Straight to the chapter list - the Vocabulary / Verb chooser screen
        // in between was one extra tap for every visit (Verb has its own tile).
        view.findViewById(R.id.vocabCard).setOnClickListener(v ->
                startActivity(new Intent(activity, VocabularyChapterActivity.class)
                        .putExtra(Constant.FROM_TYPE, "vocabulary")
                        .putExtra(Constant.FROM_TITLE, "Vocabulary")));

        view.findViewById(R.id.verbCard).setOnClickListener(v ->
                startActivity(new Intent(activity, VocabularyChapterActivity.class)
                        .putExtra(Constant.FROM_TYPE, "verb")
                        .putExtra(Constant.FROM_TITLE, "Verb")));

        view.findViewById(R.id.grammarCard).setOnClickListener(v ->
                startActivity(new Intent(activity, ChapterActivity.class)
                        .putExtra("type", "grammar")));

        view.findViewById(R.id.speakingCard).setOnClickListener(v ->
                startActivity(new Intent(activity, ChapterActivity.class)
                        .putExtra("type", "daily_vocabulary")));

        view.findViewById(R.id.wizardCard).setOnClickListener(v ->
                startActivity(new Intent(activity, WizardChapterActivity.class)));

        view.findViewById(R.id.writingCard).setOnClickListener(v ->
                startActivity(new Intent(activity, ChapterActivity.class)
                        .putExtra("type", "writing_reading")));

        return view;
    }
}
