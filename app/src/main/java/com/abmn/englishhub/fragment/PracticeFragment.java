package com.abmn.englishhub.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.abmn.englishhub.Activity.BattleActivity;
import com.abmn.englishhub.Activity.GroupActivity;
import com.abmn.englishhub.Activity.LeaderboardActivity;
import com.abmn.englishhub.Activity.MistakeNotebookActivity;
import com.abmn.englishhub.Activity.VocabularyActivity;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Helper.MistakeNotebook;
import com.abmn.englishhub.R;

/**
 * "Practice" tab: everything that is about testing yourself or competing.
 * These entry points used to sit in Home's "Study Room" grid; the target
 * Activities are unchanged, only where they are reached from moved.
 */
public class PracticeFragment extends Fragment {

    private TextView reviewSubTV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_practice, container, false);
        final Activity activity = getActivity();
        reviewSubTV = view.findViewById(R.id.practiceReviewSubTV);

        // Same one-tap behaviour as Home's "today's quiz" (QuizLauncher).
        view.findViewById(R.id.practiceQuizCard).setOnClickListener(v ->
                com.abmn.englishhub.Helper.QuizLauncher.startQuickQuiz(activity));

        view.findViewById(R.id.practiceReviewCard).setOnClickListener(v ->
                startActivity(new Intent(activity, MistakeNotebookActivity.class)));

        // BattleActivity sends guests to the login screen itself.
        view.findViewById(R.id.practiceBattleCard).setOnClickListener(v ->
                startActivity(new Intent(activity, BattleActivity.class)));

        view.findViewById(R.id.practiceLeaderboardCard).setOnClickListener(v ->
                startActivity(new Intent(activity, LeaderboardActivity.class)));

        // Tournament = a friends / class group ranked by XP earned since joining.
        // GroupActivity itself sends guests to the login screen.
        view.findViewById(R.id.practiceTournamentCard).setOnClickListener(v ->
                startActivity(new Intent(activity, GroupActivity.class)));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Activity activity = getActivity();
        if (activity == null || reviewSubTV == null) return;
        // First visit clears Home's one-time "moved to Practice" pointer.
        activity.getSharedPreferences("app_prefs", Activity.MODE_PRIVATE).edit()
                .putBoolean(Constant.PRACTICE_HINT_DISMISSED, true).apply();
        int mistakes = MistakeNotebook.getAll(activity).size();
        reviewSubTV.setText(mistakes > 0
                ? mistakes + " \u099f\u09bf \u09b6\u09ac\u09cd\u09a6 \u09b0\u09bf\u09ad\u09bf\u0989 \u0995\u09b0\u09cb"
                : "\u098f\u0996\u09a8\u09cb \u0995\u09cb\u09a8\u09cb \u09ad\u09c1\u09b2 \u09b6\u09ac\u09cd\u09a6 \u09a8\u09c7\u0987");
    }
}
