package com.abmn.englishhub.Helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Asks for a Play Store rating via the in-app review sheet, but only at a good
 * moment: after the user has finished a few quizzes and just did well on one.
 * Google decides whether the sheet actually appears (it is quota-limited), so
 * this only controls *when we ask*, never guarantees a dialog.
 */
public class ReviewPromptHelper {

    private static final String PREFS = "app_prefs";
    private static final String KEY_QUIZ_COUNT = "review_quiz_completions";
    private static final String KEY_LAST_PROMPT = "review_last_prompt_ms";

    private static final int MIN_COMPLETIONS = 3;
    private static final long MIN_GAP_MS = TimeUnit.DAYS.toMillis(60);

    private ReviewPromptHelper() {}

    /** Call once per finished quiz result screen. {@code wentWell} gates the ask. */
    public static void onQuizCompleted(Activity activity, boolean wentWell) {
        if (activity == null) return;
        SharedPreferences prefs = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        int completions = prefs.getInt(KEY_QUIZ_COUNT, 0) + 1;
        prefs.edit().putInt(KEY_QUIZ_COUNT, completions).apply();

        if (!wentWell || completions < MIN_COMPLETIONS) return;

        long now = System.currentTimeMillis();
        long last = prefs.getLong(KEY_LAST_PROMPT, 0L);
        if (last != 0L && now - last < MIN_GAP_MS) return;

        // Stamp first: if the request below fails or Google declines to show the
        // sheet, we still don't retry on every following quiz.
        prefs.edit().putLong(KEY_LAST_PROMPT, now).apply();

        try {
            final ReviewManager manager = ReviewManagerFactory.create(activity.getApplicationContext());
            manager.requestReviewFlow().addOnCompleteListener(task -> {
                if (!task.isSuccessful() || activity.isFinishing() || activity.isDestroyed()) return;
                try {
                    ReviewInfo info = task.getResult();
                    manager.launchReviewFlow(activity, info);
                } catch (RuntimeException e) {
                    Log.w("ReviewPrompt", "launchReviewFlow failed", e);
                }
            });
        } catch (RuntimeException e) {
            Log.w("ReviewPrompt", "requestReviewFlow failed", e);
        }
    }
}
