package com.abmn.englishhub.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public class ThemePrefs {

    public static final int PALETTE_DARK = 0;
    public static final int PALETTE_NAVY = 1;
    public static final int PALETTE_FOREST = 2;

    private static final String PREFS = "app_prefs";
    private static final String KEY_HOME_PALETTE = "home_palette";

    private ThemePrefs() {}

    public static class Palette {
        public final int background;
        public final int card;

        public Palette(int background, int card) {
            this.background = background;
            this.card = card;
        }
    }

    public static int getHomePalette(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_HOME_PALETTE, PALETTE_DARK);
    }

    public static void setHomePalette(Context context, int palette) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_HOME_PALETTE, palette)
                .apply();
    }

    // Backgrounds stay in the same dark luminance range as the existing
    // bg_deep/bg_card pair so the app's light-on-dark text/accent colors
    // (text_primary, indigo, teal, gold, etc.) keep working unchanged.
    public static Palette getPalette(int index) {
        switch (index) {
            case PALETTE_NAVY:
                return new Palette(Color.parseColor("#061428"), Color.parseColor("#0B2540"));
            case PALETTE_FOREST:
                return new Palette(Color.parseColor("#071A12"), Color.parseColor("#0C2A1C"));
            case PALETTE_DARK:
            default:
                return new Palette(Color.parseColor("#07081A"), Color.parseColor("#0C0E26"));
        }
    }

    public static String[] paletteNames() {
        return new String[]{"ডার্ক (ডিফল্ট)", "নেভি", "ফরেস্ট"};
    }
}
