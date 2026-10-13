package com.abmn.englishhub.Helper;

/**
 * Rate gate for the "refresh streak / XP / rank / Lipto from the server" calls.
 *
 * Home and Profile both refresh on every onResume, which fires each time the
 * user comes back from a lesson or quiz. Without a gate that is two GETs per
 * back-press on a server that runs a handful of PHP workers. Both screens
 * read the same prefs the fetch writes, so one fetch per window serves both.
 */
public final class StatsRefreshGate {

    private static final long MIN_GAP_MS = 25_000L;
    private static long lastFetchMs = 0;

    private StatsRefreshGate() {}

    /** True (and arms the gate) when enough time has passed since the last fetch. */
    public static synchronized boolean tryAcquire() {
        long now = System.currentTimeMillis();
        if (now - lastFetchMs < MIN_GAP_MS) return false;
        lastFetchMs = now;
        return true;
    }

    /** Forget the last fetch - call after a login/logout so the next screen refetches. */
    public static synchronized void reset() {
        lastFetchMs = 0;
    }
}
