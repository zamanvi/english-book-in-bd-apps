package com.abmn.englishhub.Helper;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// Client-local store of words missed during any Quick Quiz round (v1 —
// server sync deliberately deferred, see the design brief's open decisions).
// Backed by its own SharedPreferences file (not "app_prefs") so a future
// server-synced rewrite can just replace this class without touching other
// prefs. Newest-first, deduped by word, capped so it can't grow unbounded
// for a heavy daily player.
public final class MistakeNotebook {

    private static final String PREFS_NAME = "mistake_notebook";
    private static final String KEY_ENTRIES = "entries";
    private static final int MAX_ENTRIES = 100;

    private MistakeNotebook() {}

    public static void add(Context context, String word, String meaning) {
        if (word == null || word.trim().isEmpty() || meaning == null || meaning.trim().isEmpty()) return;
        word = word.trim();
        meaning = meaning.trim();

        List<JSONObject> entries = loadRaw(context);

        // De-dupe: drop any existing entry for this word so it moves back
        // to the top (most-recently-missed) instead of showing twice.
        for (int i = entries.size() - 1; i >= 0; i--) {
            if (word.equalsIgnoreCase(entries.get(i).optString("word"))) {
                entries.remove(i);
            }
        }

        try {
            JSONObject entry = new JSONObject();
            entry.put("word", word);
            entry.put("meaning", meaning);
            entries.add(0, entry);
        } catch (Exception ignored) {
            return;
        }

        while (entries.size() > MAX_ENTRIES) {
            entries.remove(entries.size() - 1);
        }

        saveRaw(context, entries);
    }

    public static List<String[]> getAll(Context context) {
        List<String[]> out = new ArrayList<>();
        for (JSONObject entry : loadRaw(context)) {
            out.add(new String[]{entry.optString("word"), entry.optString("meaning")});
        }
        return out;
    }

    public static void clear(Context context) {
        prefs(context).edit().remove(KEY_ENTRIES).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static List<JSONObject> loadRaw(Context context) {
        List<JSONObject> list = new ArrayList<>();
        String raw = prefs(context).getString(KEY_ENTRIES, null);
        if (raw == null) return list;
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                list.add(arr.getJSONObject(i));
            }
        } catch (Exception ignored) {}
        return list;
    }

    private static void saveRaw(Context context, List<JSONObject> entries) {
        JSONArray arr = new JSONArray();
        for (JSONObject e : entries) arr.put(e);
        prefs(context).edit().putString(KEY_ENTRIES, arr.toString()).apply();
    }
}
