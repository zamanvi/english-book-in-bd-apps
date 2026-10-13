package com.abmn.englishhub.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * "Unread notices" dot for the toolbar bell.
 *
 * Notices have no per-user read state on the server, so unread = the highest
 * notice id the app has seen so far is lower than the highest id on the
 * server. Everything is local: one gated GET for page 1, ids compared.
 *
 * The very first check only records the current maximum (no dot) so a user who
 * upgrades is not greeted by every historical notice as "new".
 */
public final class NoticeBadge {

    private static final String PREFS = "app_prefs";
    private static final String KEY_MAX = "notice_max_id";
    private static final String KEY_SEEN = "notice_seen_id";
    private static final String KEY_CHECKED_MS = "notice_checked_ms";
    private static final long MIN_GAP_MS = 10 * 60 * 1000L;

    public interface Listener {
        void onChanged();
    }

    private NoticeBadge() {}

    public static boolean hasUnread(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int seen = sp.getInt(KEY_SEEN, -1);
        return seen >= 0 && sp.getInt(KEY_MAX, 0) > seen;
    }

    /** Called when the notice list has been shown: everything up to maxId counts as read. */
    public static void markSeen(Context context, int maxIdInList) {
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int max = Math.max(sp.getInt(KEY_MAX, 0), maxIdInList);
        sp.edit().putInt(KEY_MAX, max).putInt(KEY_SEEN, max).apply();
    }

    /** Gated background check; listener runs on the main thread when the unread state may have changed. */
    public static void refresh(Context context, Listener listener) {
        final Context app = context.getApplicationContext();
        SharedPreferences sp = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        long now = System.currentTimeMillis();
        if (now - sp.getLong(KEY_CHECKED_MS, 0) < MIN_GAP_MS) return;
        sp.edit().putLong(KEY_CHECKED_MS, now).apply();

        ApiConfig.getRequest(app, Constant.ROOT_API + "notices/grammar?page=1", response -> {
            try {
                JSONObject notices = new JSONObject(response).getJSONObject("notices");
                JSONArray data = notices.getJSONArray(Constant.DATA);
                int max = 0;
                for (int i = 0; i < data.length(); i++) {
                    max = Math.max(max, data.getJSONObject(i).optInt("id", 0));
                }
                SharedPreferences.Editor ed = sp.edit().putInt(KEY_MAX, max);
                if (sp.getInt(KEY_SEEN, -1) < 0) ed.putInt(KEY_SEEN, max); // first run: nothing is "new"
                ed.apply();
                if (listener != null) new Handler(Looper.getMainLooper()).post(listener::onChanged);
            } catch (Exception ignored) {
                // Allow the next resume to retry rather than waiting out the gap.
                sp.edit().putLong(KEY_CHECKED_MS, 0).apply();
            }
        }, error -> sp.edit().putLong(KEY_CHECKED_MS, 0).apply());
    }
}
