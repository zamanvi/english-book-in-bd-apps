package com.abmn.englishhub.Helper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.abmn.englishhub.Activity.LevelMapActivity;
import com.abmn.englishhub.Activity.VocabularyChapterActivity;
import com.abmn.utility.UConfig;

/**
 * One "start a quiz" behaviour for every shortcut (Home's today's quiz and
 * Practice's quick quiz).
 *
 * Someone who has played before goes straight back into the next round of
 * their last lesson; a first-time player lands on the chapter list in picker
 * mode. Both used to take several taps through a category screen, a chapter
 * list, a lesson list and the level map before the first question.
 */
public final class QuizLauncher {

    private QuizLauncher() {}

    public static void startQuickQuiz(Context context) {
        SharedPreferences sp = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        int lessonId = sp.getInt(Constant.LAST_QUIZ_LESSON_ID, 0);
        String token = new UConfig(context).getData(Constant.TOKEN);
        boolean loggedIn = token != null && !token.isEmpty();

        if (lessonId > 0 && loggedIn) {
            // LevelMapActivity still re-checks premium lock / round state and
            // only auto-starts a round that is actually unlocked.
            context.startActivity(new Intent(context, LevelMapActivity.class)
                    .putExtra("lesson_id", lessonId)
                    .putExtra("lesson_title", sp.getString(Constant.LAST_QUIZ_LESSON_TITLE, ""))
                    .putExtra("auto_start", true));
        } else {
            context.startActivity(new Intent(context, VocabularyChapterActivity.class)
                    .putExtra(Constant.FROM_TYPE, "vocabulary")
                    .putExtra(Constant.FROM_TITLE, "\u0995\u09c1\u0987\u099c\u09c7\u09b0 \u099c\u09a8\u09cd\u09af \u09ac\u09c7\u099b\u09c7 \u09a8\u09be\u0993")
                    .putExtra(Constant.QUIZ_PICKER_MODE, true));
        }
    }
}
