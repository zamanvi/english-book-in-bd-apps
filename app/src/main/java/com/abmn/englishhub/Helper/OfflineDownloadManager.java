package com.abmn.englishhub.Helper;

import android.app.Activity;
import android.util.Log;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Walks the same book → chapters → items → item-details chain that
 * ChapterActivity / ItemActivity / ItemDetailsActivity walk during normal
 * browsing, firing each call through ApiConfig with the exact same cache
 * keys those screens use. ApiConfig.RequestToVolley already writes every
 * successful response to OfflineCache, so this class only needs to trigger
 * the calls - it doesn't touch the cache directly.
 */
public class OfflineDownloadManager {

    private static final String BOOK_SLUG = "english-hub";
    private static final String[] CHAPTER_TYPES = {"grammar", "daily_vocabulary", "writing_reading"};

    public interface Listener {
        void onProgress(int done, int total);
        void onComplete(int itemCount);
        void onError(String message);
    }

    private static volatile boolean running = false;

    public static boolean isRunning() {
        return running;
    }

    public static void downloadAll(Activity activity, Listener listener) {
        if (running) return;
        running = true;

        ApiConfig.RequestToVolley((result, response, error) -> {
            if (!result) {
                running = false;
                listener.onError("বই তথ্য লোড করা যায়নি। ইন্টারনেট চেক করো।");
                return;
            }
            fetchAllChapters(activity, listener);
        }, Request.Method.GET, activity, Constant.BOOK_API, new HashMap<>(), false, "book_english_hub");
    }

    private static void fetchAllChapters(Activity activity, Listener listener) {
        Set<String> chapterSlugs = new LinkedHashSet<>();
        int[] typesRemaining = {CHAPTER_TYPES.length};

        for (String type : CHAPTER_TYPES) {
            String url = Constant.CHAPTER_API2 + "?book_slug=" + BOOK_SLUG + "&type=" + type;
            String cacheKey = "chapters_" + type;
            ApiConfig.RequestToVolley((result, response, error) -> {
                if (result && response != null && !response.isEmpty()) {
                    try {
                        JSONObject chapters = new JSONObject(response).getJSONObject("chapters");
                        JSONArray arr = chapters.getJSONArray(Constant.DATA);
                        for (int i = 0; i < arr.length(); i++) {
                            chapterSlugs.add(arr.getJSONObject(i).getString("slug"));
                        }
                    } catch (Exception e) {
                        Log.e("OfflineDownload", "chapters parse error", e);
                    }
                }
                typesRemaining[0]--;
                if (typesRemaining[0] == 0) {
                    if (chapterSlugs.isEmpty()) {
                        running = false;
                        listener.onError("কোনো chapter পাওয়া যায়নি");
                        return;
                    }
                    fetchAllItems(activity, chapterSlugs, listener);
                }
            }, Request.Method.GET, activity, url, new HashMap<>(), false, cacheKey);
        }
    }

    private static void fetchAllItems(Activity activity, Set<String> chapterSlugs, Listener listener) {
        Set<String> itemSlugs = new LinkedHashSet<>();
        int[] chaptersRemaining = {chapterSlugs.size()};

        for (String chapterSlug : chapterSlugs) {
            String url = Constant.ITEM_API + "?chapter_slug=" + chapterSlug;
            String cacheKey = "items_" + chapterSlug;
            ApiConfig.RequestToVolley((result, response, error) -> {
                if (result && response != null && !response.isEmpty()) {
                    try {
                        JSONObject items = new JSONObject(response).getJSONObject("items");
                        JSONArray arr = items.getJSONArray(Constant.DATA);
                        for (int i = 0; i < arr.length(); i++) {
                            itemSlugs.add(arr.getJSONObject(i).getString("slug"));
                        }
                    } catch (Exception e) {
                        Log.e("OfflineDownload", "items parse error", e);
                    }
                }
                chaptersRemaining[0]--;
                if (chaptersRemaining[0] == 0) {
                    if (itemSlugs.isEmpty()) {
                        running = false;
                        listener.onError("কোনো lesson পাওয়া যায়নি");
                        return;
                    }
                    downloadItemDetails(activity, itemSlugs, listener);
                }
            }, Request.Method.GET, activity, url, new HashMap<>(), false, cacheKey);
        }
    }

    private static void downloadItemDetails(Activity activity, Set<String> itemSlugs, Listener listener) {
        int total = itemSlugs.size();
        int[] done = {0};

        for (String slug : itemSlugs) {
            String url = Constant.ITEM_SHOW_API + slug;
            String cacheKey = "item_details_" + slug;
            ApiConfig.RequestToVolley((result, response, error) -> {
                done[0]++;
                listener.onProgress(done[0], total);
                if (done[0] == total) {
                    running = false;
                    listener.onComplete(total);
                }
            }, Request.Method.GET, activity, url, new HashMap<>(), false, cacheKey);
        }
    }
}
