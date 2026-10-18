package com.abmn.englishhub.Helper;

/**
 * Quick Quiz round numbering shared by the level map, the quiz screens and the
 * result screen.
 *
 * The server numbers the rounds 1 MCQ, 2 Reading, 3 Picture, 4 Listening,
 * 5 Writing. Picture needs word images and none exist yet, so the server has
 * it switched off (GameController::DISABLED_ROUNDS) and the unlock chain skips
 * it. The app hides that card and shows the remaining rounds as 1-4. Keep
 * {@link #HIDDEN} in step with the server; when Picture (or whatever replaces
 * it) is switched on, empty it in the same release.
 */
public final class QuizRounds {

    private static final int[] HIDDEN = {3};

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

    /** Tells the server this client uses the current (5-slot) round numbering. */
    public static String withVersion(String url) {
        return url + (url.contains("?") ? "&" : "?") + "v=2";
    }
}
