package com.abmn.englishhub.Helper;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Asks "leave this quiz?" before a half-played round is thrown away.
 *
 * The X button and the system back gesture used to call finish() straight
 * away, so one stray tap silently dropped the round (hearts, answers, XP) with
 * no way back. Shared by QuizActivity and WritingActivity.
 */
public final class QuizExitGuard {

    /** Tells the guard whether leaving now would actually lose anything. */
    public interface ProgressProbe {
        boolean hasProgress();
    }

    private final AppCompatActivity activity;
    private final ProgressProbe probe;
    private AlertDialog dialog;

    private QuizExitGuard(AppCompatActivity activity, ProgressProbe probe) {
        this.activity = activity;
        this.probe = probe;
    }

    /** Wires the system back gesture and returns the guard for the X button. */
    public static QuizExitGuard attach(AppCompatActivity activity, ProgressProbe probe) {
        QuizExitGuard guard = new QuizExitGuard(activity, probe);
        activity.getOnBackPressedDispatcher().addCallback(activity, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                guard.requestExit();
            }
        });
        return guard;
    }

    /** Finishes immediately when nothing is at stake, otherwise confirms first. */
    public void requestExit() {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        if (!probe.hasProgress()) {
            activity.finish();
            return;
        }
        if (dialog != null && dialog.isShowing()) return;
        dialog = new AlertDialog.Builder(activity, com.abmn.englishhub.R.style.Theme_App_Dialog)
                .setTitle("কুইজ থেকে বের হবে?")
                .setMessage("এখন বের হলে এই রাউন্ডের অগ্রগতি হারিয়ে যাবে।")
                .setPositiveButton("বের হও", (d, w) -> activity.finish())
                .setNegativeButton("খেলা চালিয়ে যাও", null)
                .create();
        dialog.show();
    }

    /** Call from onDestroy so a showing dialog never leaks the Activity. */
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
        dialog = null;
    }
}
