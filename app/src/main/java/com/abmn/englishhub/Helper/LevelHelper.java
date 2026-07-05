package com.abmn.englishhub.Helper;

public class LevelHelper {

    // XP thresholds for each level
    private static final int[] LEVEL_XP = {0, 50, 150, 300, 500, 800, 1200, 1700, 2300, 3000};
    private static final String[] LEVEL_TITLES = {
            "শিক্ষানবিশ",   // 1
            "শিক্ষার্থী",    // 2
            "মধ্যবর্তী",    // 3
            "অ্যাডভান্সড",  // 4
            "এক্সপার্ট",    // 5
            "মাস্টার",      // 6
            "গ্র্যান্ড মাস্টার", // 7
            "লিজেন্ড",     // 8
            "চ্যাম্পিয়ন",  // 9
    };
    private static final String[] LEVEL_EMOJIS = {
            "🌱", "📚", "📈", "⭐", "🎯", "🏅", "💎", "🔥", "🏆"
    };

    public static int getLevel(int xp) {
        for (int i = LEVEL_XP.length - 1; i >= 0; i--) {
            if (xp >= LEVEL_XP[i]) return i + 1;
        }
        return 1;
    }

    public static String getLevelTitle(int xp) {
        int lvl = getLevel(xp);
        int idx = Math.min(lvl - 1, LEVEL_TITLES.length - 1);
        return LEVEL_EMOJIS[idx] + " " + LEVEL_TITLES[idx];
    }

    public static String getLevelLabel(int xp) {
        return "Level " + getLevel(xp);
    }

    /** XP needed to reach the NEXT level (0 if max level) */
    public static int getXpToNextLevel(int xp) {
        int lvl = getLevel(xp);
        if (lvl >= LEVEL_XP.length) return 0;
        return LEVEL_XP[lvl] - xp;
    }

    /** Progress percentage within the current level (0–100) */
    public static int getLevelProgressPercent(int xp) {
        int lvl = getLevel(xp);
        if (lvl >= LEVEL_XP.length) return 100;
        int currentMin = LEVEL_XP[lvl - 1];
        int nextMin    = LEVEL_XP[lvl];
        int range = nextMin - currentMin;
        if (range <= 0) return 100;
        return Math.min(100, (int) (((xp - currentMin) * 100.0f) / range));
    }
}
