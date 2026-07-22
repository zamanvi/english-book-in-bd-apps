package com.abmn.englishhub.Helper;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Disk cache for API response JSON, keyed by a caller-supplied key. Lets
 * content already fetched once while online be reopened with no connection -
 * ApiConfig.RequestToVolley writes here on every successful call that passes
 * a cacheKey, and reads from here when the network call fails or there's no
 * connection at all.
 */
public class OfflineCache {
    private static final String DIR_NAME = "offline_content";

    private static File file(Context context, String key) {
        File dir = new File(context.getApplicationContext().getFilesDir(), DIR_NAME);
        if (!dir.exists()) dir.mkdirs();
        String safeName = key.replaceAll("[^a-zA-Z0-9_\\-]", "_") + ".json";
        return new File(dir, safeName);
    }

    public static void save(Context context, String key, String json) {
        if (json == null || json.isEmpty()) return;
        try (FileOutputStream fos = new FileOutputStream(file(context, key))) {
            fos.write(json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {
        }
    }

    public static String load(Context context, String key) {
        File f = file(context, key);
        if (!f.exists()) return null;
        try (FileInputStream fis = new FileInputStream(f)) {
            byte[] bytes = new byte[(int) f.length()];
            int read = fis.read(bytes);
            if (read <= 0) return null;
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }
}
