package com.abmn.englishhub.Helper;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.abmn.englishhub.Activity.MainActivity;
import com.abmn.englishhub.R;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class StreakReminderReceiver extends BroadcastReceiver {

    private static final int NOTIF_ID   = 2001;
    private static final String CHANNEL = "abmnmenglish";

    // ── Re-engagement tiers, bucketed by days since MainActivity.onResume last
    // stamped LAST_ACTIVE_TIMESTAMP - i.e. days since the user actually opened
    // the app, not days since their last completed quiz. Tone escalates with
    // absence: a light nudge for active users, gently warmer for a week away,
    // and a deliberately emotional appeal past a month so it actually prompts
    // them to tap back in. Title+message rotate together by day-of-year so
    // repeat notifications in the same tier don't feel copy-pasted.

    private static final String[] ACTIVE_TITLES = {
        "চমৎকার চলছে! 🌟", "দারুণ ধারাবাহিকতা! ✨", "তুমি এগিয়ে যাচ্ছ! 🚀",
    };
    private static final String[] ACTIVE_MESSAGES = {
        "🌟 তুমি দারুণ করছ! আজও কিছু নতুন শিখে নাও।",
        "✨ তোমার ধারাবাহিকতা অসাধারণ! চলো, আরেকটু এগিয়ে যাই।",
        "🚀 প্রতিদিনের এই অভ্যাসই তোমাকে এগিয়ে নিয়ে যাচ্ছে। Keep going!",
        "🏆 তুমি ঠিক পথে আছো! আজকের অনুশীলনটা সেরে নাও।",
        "💪 তোমার মতো শিক্ষার্থীরাই সেরা হয়। আজকের লেসনটা শুরু করো।",
    };

    private static final String ONE_DAY_TITLE = "তোমাকে মিস করছি 🥺";
    private static final String[] ONE_DAY_MESSAGES = {
        "😢 গতকাল থেকে তোমাকে দেখিনি। ফিরে এসো, তোমার জন্য অপেক্ষা করছি।",
        "🥺 তোমার শেখার যাত্রাটা থমকে আছে... আজই আবার শুরু করি?",
        "💭 তোমাকে ছাড়া পড়ার ঘরটা একটু ফাঁকা ফাঁকা লাগছে। চলে এসো।",
        "😔 একটা দিন miss হয়ে গেছে, কিন্তু এখনো দেরি হয়নি। ফিরে এসো!",
        "🌧️ তোমার streak-টা তোমার অপেক্ষায় আছে। হারিয়ে যেতে দিও না।",
    };

    private static final String ONE_WEEK_TITLE = "অনেকদিন দেখা নেই 🌸";
    private static final String[] ONE_WEEK_MESSAGES = {
        "🌸 অনেকদিন হয়ে গেল তোমার সাথে দেখা নেই। সময় পেলে একটু ঘুরে যেও।",
        "📖 তোমার বইটা তোমার অপেক্ষায় আছে, ঠিক যেখানে রেখে গিয়েছিলে।",
        "🙂 তোমাকে মিস করছি। ভালো থেকো, আর যখন সময় হয় ফিরে এসো।",
        "🍃 ব্যস্ততা থাকতেই পারে, তবু একটু সময় করে চলে এসো, ভালো লাগবে।",
        "💌 তোমার জন্য নতুন কিছু শেখার অপেক্ষা করছে। সময় হলে দেখে যেও।",
    };

    private static final String ONE_MONTH_TITLE = "তোমাকে খুব মিস করছি... 💔";
    private static final String[] ONE_MONTH_MESSAGES = {
        "😭💔 এক মাস হয়ে গেল তুমি আসোনি... তোমার স্বপ্নটা কি এখানেই থেমে যাবে? 🥺",
        "💔😢 তোমার ইংরেজি শেখার যাত্রাটা কি এখানেই শেষ? আমরা এখনো অপেক্ষা করছি... 🙏",
        "😞💧 এতদিন পর মনে পড়ল তোমার কথা... একবার ফিরে এসো, প্লিজ 🥹",
        "🥹💭 তোমার জায়গাটা এখনো ফাঁকা পড়ে আছে। তুমি ছাড়া সত্যিই ভালো লাগছে না... 😢",
        "😔🕊️ তুমি কি সত্যিই হাল ছেড়ে দিলে? একটা সুযোগ দাও নিজেকে, ফিরে এসো 💔",
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        // Installs from before this field existed have no stamp yet - default
        // to "now" so they land in the ACTIVE tier instead of being wrongly
        // told they've been gone a month on the very first check.
        long lastActive = prefs.getLong(Constant.LAST_ACTIVE_TIMESTAMP, System.currentTimeMillis());
        long daysSince = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - lastActive);

        int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        String title;
        String message;

        if (daysSince >= 30) {
            title   = ONE_MONTH_TITLE;
            message = ONE_MONTH_MESSAGES[dayOfYear % ONE_MONTH_MESSAGES.length];
        } else if (daysSince >= 7) {
            title   = ONE_WEEK_TITLE;
            message = ONE_WEEK_MESSAGES[dayOfYear % ONE_WEEK_MESSAGES.length];
        } else if (daysSince >= 1) {
            title   = ONE_DAY_TITLE;
            message = ONE_DAY_MESSAGES[dayOfYear % ONE_DAY_MESSAGES.length];
        } else {
            title   = ACTIVE_TITLES[dayOfYear % ACTIVE_TITLES.length];
            message = ACTIVE_MESSAGES[dayOfYear % ACTIVE_MESSAGES.length];
        }

        showNotification(context, title, message);
        // Always reschedule for tomorrow regardless of tier - the old
        // "skip if played today" version returned early without calling
        // schedule() again, which meant the very next app-open was the only
        // thing keeping the alarm chain alive. Rescheduling unconditionally
        // here guarantees the 24h cycle continues on its own from now on.
        schedule(context);
    }

    // ── Notification ─────────────────────────────────────────────

    private void showNotification(Context context, String title, String message) {
        Intent tapIntent = new Intent(context, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(
                context, NOTIF_ID, tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL, "ABMN Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Daily re-engagement & streak reminders");
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pi);

        manager.notify(NOTIF_ID, builder.build());
    }

    // ── Scheduling ────────────────────────────────────────────────

    /**
     * Call once from MainActivity to set up daily 8pm reminder.
     * AlarmManager persists the alarm; BootReceiver reschedules after reboot.
     */
    public static void schedule(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = buildPendingIntent(context);

        Calendar fireAt = Calendar.getInstance();
        fireAt.set(Calendar.HOUR_OF_DAY, 20); // 8pm
        fireAt.set(Calendar.MINUTE, 0);
        fireAt.set(Calendar.SECOND, 0);
        fireAt.set(Calendar.MILLISECOND, 0);

        // If 8pm already passed today, schedule for tomorrow
        if (fireAt.getTimeInMillis() <= System.currentTimeMillis()) {
            fireAt.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Inexact is intentional: avoids the SCHEDULE_EXACT_ALARM permission and
        // Play Console's exact-alarm justification review; still fires within ~15 min of target.
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAt.getTimeInMillis(), pi);
    }

    public static void cancel(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(buildPendingIntent(context));
    }

    private static PendingIntent buildPendingIntent(Context context) {
        Intent intent = new Intent(context, StreakReminderReceiver.class);
        return PendingIntent.getBroadcast(
                context, NOTIF_ID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
