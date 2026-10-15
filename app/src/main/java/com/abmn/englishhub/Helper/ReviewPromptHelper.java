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
 * moment: after the user has finished a few quizzes and just did well on one,
 * and only once the app has been installed for a week. Google decides whether
 * the sheet actually appears (it is quota-limited), so this only controls
 * *when we ask*, never guarantees a dialog.
 *
 * Restraint: at most {@link #MAX_PROMPTS} asks in a lifetime, at least
 * {@link #MIN_GAP_MS} apart. A failed or declined request still counts as an
 * ask, so we never nag on every following quiz.
 */
public class ReviewPromptHelper {

    private static final String PREFS = "app_prefs";
    private static final String KEY_QUIZ_COUNT = "review_quiz_completions";
    private static final String KEY_LAST_PROMPT = "review_last_prompt_ms";
    private static final String KEY_PROMPT_COUNT = "review_prompt_count";

    static final int MIN_COMPLETIONS = 3;
    static final int MAX_PROMPTS = 2;
    static final long MIN_GAP_MS = TimeUnit.DAYS.toMillis(60);
    static final long MIN_INSTALL_AGE_MS = TimeUnit.DAYS.toMillis(7);

    private ReviewPromptHelper() {}

    /**
     * The whole "should we ask now?" decision, kept free of Android types so
     * it can be unit-tested.
     *
     * @param installAgeMs   time since the app was first installed, or a
     *                       negative value when it could not be determined
     * @param promptCount    how many times we have already asked
     * @param msSinceLastAsk time since the last ask, or a negative value if never
     */
    static boolean shouldAsk(int completions, boolean wentWell, long installAgeMs,
                             int promptCount, long msSinceLastAsk) {
        if (!wentWell || completions < MIN_COMPLETIONS) return false;
        if (installAgeMs < MIN_INSTALL_AGE_MS) return false;      // also false when unknown (<0)
        if (promptCount >= MAX_PROMPTS) return false;
        if (msSinceLastAsk >= 0 && msSinceLastAsk < MIN_GAP_MS) return false;
        return true;
    }

    /** Call once per finished quiz result screen. {@code wentWell} gates the ask. */
    public static void onQuizCompleted(Activity activity, boolean wentWell) {
        if (activity == null) return;
        SharedPreferences prefs = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        int completions = prefs.getInt(KEY_QUIZ_COUNT, 0) + 1;
        prefs.edit().putInt(KEY_QUIZ_COUNT, completions).apply();

        long now = System.currentTimeMillis();
        long last = prefs.getLong(KEY_LAST_PROMPT, 0L);
        int promptCount = prefs.getInt(KEY_PROMPT_COUNT, 0);
        // A device that was asked before the counter existed counts as asked once.
        if (promptCount == 0 && last != 0L) promptCount = 1;

        if (!shouldAsk(completions, wentWell, installAgeMs(activity, now),
                promptCount, last == 0L ? -1L : now - last)) {
            return;
        }

        // Stamp first: if the request below fails or Google declines to show the
        // sheet, we still don't retry on every following quiz.
        prefs.edit()
                .putLong(KEY_LAST_PROMPT, now)
                .putInt(KEY_PROMPT_COUNT, promptCount + 1)
                .apply();

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

    /**
     * Age of the installation from the package manager (survives clearing the
     * app's data and needs no extra storage). -1 when it cannot be read, which
     * makes {@link #shouldAsk} say no rather than guess.
     */
    private static long installAgeMs(Activity activity, long now) {
        try {
            long first = activity.getPackageManager()
                    .getPackageInfo(activity.getPackageName(), 0).firstInstallTime;
            return first > 0 ? Math.max(0L, now - first) : -1L;
        } catch (Exception e) {
            return -1L;
        }
    }
}
