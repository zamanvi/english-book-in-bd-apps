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

public class StreakReminderReceiver extends BroadcastReceiver {

    private static final int NOTIF_ID   = 2001;
    private static final String CHANNEL = "abmnmenglish";

    // Messages that rotate daily to keep it fresh
    private static final String[] MESSAGES = {
        "🔥 তোমার streak এখনো বেঁচে আছে! আজকের quiz দিয়ে ধরে রাখো।",
        "⚡ মাত্র ২ মিনিট! আজকের quiz দিয়ে streak বাঁচাও।",
        "💪 গতকাল এত কাছে ছিলে! আজ হাল ছাড়ো না — quiz দাও।",
        "🏆 তোমার leaderboard rank কমে যাচ্ছে! Quiz দিয়ে rank ধরে রাখো।",
        "📚 প্রতিদিন একটু একটু শেখাই আসল শেখা। আজকের quiz বাকি আছে!",
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        // Skip if already played today
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String lastPlayed = prefs.getString(Constant.LAST_PLAYED_DATE, "");
        String today = Constant.todayString();
        if (today.equals(lastPlayed)) return;

        // Pick message by day-of-year so it cycles
        int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        String message = MESSAGES[dayOfYear % MESSAGES.length];

        showNotification(context, message);
        // Reschedule for tomorrow 8pm
        schedule(context);
    }

    // ── Notification ─────────────────────────────────────────────

    private void showNotification(Context context, String message) {
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
            channel.setDescription("Daily streak reminders");
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("ইংরেজি শিখছ তুমি 🔥")
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            // Fallback to inexact — still fires within ~15 min of target
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAt.getTimeInMillis(), pi);
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAt.getTimeInMillis(), pi);
        }
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
