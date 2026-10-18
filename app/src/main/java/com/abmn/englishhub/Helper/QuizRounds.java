package com.abmn.englishhub.Helper;

/**
 * Quick Quiz round numbering shared by the level map, the quiz screens and the
 * result screen.
 *
 * The server numbers the rounds 1 MCQ, 2 Reading, 3 Match pairs, 4 Listening,
 * 5 Writing. Clients that say "?v=3" (this one) can play all five; older
 * clients are told round 3 is "soon" and the unlock chain skips it for them.
 * {@link #HIDDEN} lists server rounds this build does not show (none now);
 * the displayed numbers shift down for every hidden round before them.
 */
public final class QuizRounds {

    private static final int[] HIDDEN = {};

    private QuizRounds() {}

    public static boolean isHidden(int serverRound) {
        for (int h : HIDDEN) {
            if (h == serverRound) return true;
        }
        return false;
    }

    /** The number the player sees: rounds after a hidden one move up. */
    public static int displayNumber(int serverRound) {
        int shown = serverRound;
        for (int h : HIDDEN) {
            if (h < serverRound) shown--;
        }
        return shown;
    }

    /** Tells the server this client uses the 5-slot numbering and can play Match pairs. */
    public static String withVersion(String url) {
        return url + (url.contains("?") ? "&" : "?") + "v=3";
    }
}
