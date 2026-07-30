package com.abmn.englishhub.Helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.abmn.englishhub.Activity.BattleActivity;
import com.abmn.englishhub.Activity.MainActivity;
import com.abmn.englishhub.Activity.SplashActivity;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

public class FcmService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "learnify_notifications";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // Save token to backend if user is logged in
        String authToken = new UConfig(this).getData(Constant.TOKEN);
        if (authToken != null && !authToken.isEmpty()) {
            saveTokenToBackend(token, authToken);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        String title = remoteMessage.getData().get("title");
        String body  = remoteMessage.getData().get("body");
        String type  = remoteMessage.getData().getOrDefault("type", "");

        Intent intent;
        int notifId = 1000;

        switch (type) {
            case "battle":
                String battleIdStr = remoteMessage.getData().get("battle_id");
                intent = new Intent(this, BattleActivity.class);
                if (battleIdStr != null) {
                    try {
                        intent.putExtra("battle_id", Integer.parseInt(battleIdStr));
                    } catch (NumberFormatException ignored) { /* malformed payload - open BattleActivity with no id */ }
                }
                notifId = 2002;
                break;
            case "item":
                String slug = remoteMessage.getData().get("slug");
                intent = new Intent(this, SplashActivity.class);
                intent.putExtra("type", "item");
                intent.putExtra("slug", slug);
                break;
            default:
                intent = new Intent(this, MainActivity.class);
                break;
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, notifId, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        ensureChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(notifId, builder.build());
    }

    private void ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Learnify Notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Quiz reminders, battle challenges, word of the day");
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .createNotificationChannel(channel);
        }
    }

    // ── Save FCM token to backend ─────────────────────────────────

    public static void saveTokenToBackend(Context context) {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    String authToken = new UConfig(context).getData(Constant.TOKEN);
                    if (authToken != null && !authToken.isEmpty()) {
                        new FcmService().saveTokenToBackend(token, authToken);
                    }
                });
    }

    private void saveTokenToBackend(String fcmToken, String authToken) {
        JSONObject body = new JSONObject();
        try { body.put("token", fcmToken); } catch (Exception ignored) {}
        ApiConfig.postRequest(this, Constant.NOTIF_TOKEN, body, authToken,
                response -> {}, error -> {});
    }
}
