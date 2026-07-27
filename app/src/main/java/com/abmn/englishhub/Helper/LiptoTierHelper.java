package com.abmn.englishhub.Helper;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.abmn.englishhub.R;

// Purely presentational - buckets a user's existing lipto_balance into a
// tier for the profile badge/progress ring. No new backend data needed.
public class LiptoTierHelper {

    public static final int SILVER_THRESHOLD = 500;
    public static final int GOLD_THRESHOLD   = 1000;

    private LiptoTierHelper() {}

    public static String getTierName(int lipto) {
        if (lipto >= GOLD_THRESHOLD) return "Gold";
        if (lipto >= SILVER_THRESHOLD) return "Silver";
        return "Bronze";
    }

    public static String getTierEmoji(int lipto) {
        if (lipto >= GOLD_THRESHOLD) return "🥇";
        if (lipto >= SILVER_THRESHOLD) return "🥈";
        return "🥉";
    }

    public static int getTierColor(Context context, int lipto) {
        if (lipto >= GOLD_THRESHOLD) return ContextCompat.getColor(context, R.color.gold);
        if (lipto >= SILVER_THRESHOLD) return ContextCompat.getColor(context, R.color.silver);
        return ContextCompat.getColor(context, R.color.bronze);
    }

    public static int getTierDimColor(Context context, int lipto) {
        if (lipto >= GOLD_THRESHOLD) return ContextCompat.getColor(context, R.color.gold_dim);
        if (lipto >= SILVER_THRESHOLD) return ContextCompat.getColor(context, R.color.silver_dim);
        return ContextCompat.getColor(context, R.color.bronze_dim);
    }

    // -1 means already at the max tier (Gold)
    public static int getNextThreshold(int lipto) {
        if (lipto < SILVER_THRESHOLD) return SILVER_THRESHOLD;
        if (lipto < GOLD_THRESHOLD) return GOLD_THRESHOLD;
        return -1;
    }

    public static String getNextTierName(int lipto) {
        if (lipto < SILVER_THRESHOLD) return "Silver";
        if (lipto < GOLD_THRESHOLD) return "Gold";
        return "";
    }

    private static int getPrevThreshold(int lipto) {
        if (lipto < SILVER_THRESHOLD) return 0;
        if (lipto < GOLD_THRESHOLD) return SILVER_THRESHOLD;
        return GOLD_THRESHOLD;
    }

    // Percent of the way through the CURRENT tier's range towards the next one.
    public static int getProgressPercent(int lipto) {
        int next = getNextThreshold(lipto);
        if (next == -1) return 100;
        int prev = getPrevThreshold(lipto);
        int span = next - prev;
        if (span <= 0) return 100;
        int into = lipto - prev;
        return Math.max(0, Math.min(100, (into * 100) / span));
    }
}
