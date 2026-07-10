package com.abmn.englishhub.Helper;

public class Constant {
    //Base Url
    public static final String ROOT_DOMAIN = "https://red-rose-academy-backend-laravel-production.up.railway.app/";
    public static final String API = "api/v2/app/";
    public static final String API2 = "api/";
    public static final String ROOT_API = ROOT_DOMAIN + API;
    public static final String ROOT_API2 = ROOT_DOMAIN + API2 + "grammer/";
    public static final String ROOT_API_GAME      = ROOT_DOMAIN + API2 + "game/";
    public static final String ROOT_API_GAME_AUTH = ROOT_DOMAIN + API2 + "app/game/";

    // Game API endpoints
    public static final String GAME_DAILY_WORD    = ROOT_API_GAME      + "daily-word";
    public static final String GAME_QUIZ          = ROOT_API_GAME      + "quiz/";
    public static final String GAME_LEADERBOARD   = ROOT_API_GAME      + "leaderboard";
    public static final String GAME_XP            = ROOT_API_GAME_AUTH + "xp";
    public static final String GAME_STREAK        = ROOT_API_GAME_AUTH + "streak";
    public static final String GAME_STREAK_UPDATE = ROOT_API_GAME_AUTH + "streak/update";
    public static final String GAME_LIPTO_BALANCE  = ROOT_API_GAME_AUTH + "lipto/balance";
    public static final String GAME_LIPTO_EARN     = ROOT_API_GAME_AUTH + "lipto/earn";
    public static final String GAME_LIPTO_SPEND    = ROOT_API_GAME_AUTH + "lipto/spend";

    // Group / Room endpoints
    public static final String GROUP_CREATE        = ROOT_API_GAME_AUTH + "group/create";
    public static final String GROUP_JOIN          = ROOT_API_GAME_AUTH + "group/join";
    public static final String GROUP_MY            = ROOT_API_GAME_AUTH + "group/my";
    public static final String GROUP_LEADERBOARD   = ROOT_API_GAME_AUTH + "group/";  // + code + "/leaderboard"

    // Notification endpoints
    public static final String NOTIF_TOKEN = ROOT_API_GAME_AUTH + "notification/token";

    // Battle endpoints
    public static final String BATTLE_CHALLENGE    = ROOT_API_GAME_AUTH + "battle/challenge";
    public static final String BATTLE_PENDING      = ROOT_API_GAME_AUTH + "battle/pending";
    public static final String BATTLE_HISTORY      = ROOT_API_GAME_AUTH + "battle/history";
    public static final String BATTLE_BASE         = ROOT_API_GAME_AUTH + "battle/";  // + id or id/submit

    // Grammar API endpoints
    public static final String GRAMMAR_CHAPTERS = ROOT_API2 + "chapters";

    //API path
    public static final String BOOK_API = ROOT_API + "book/index";
    public static final String CHAPTER_API2 = ROOT_API + "book/chapter/index2";
    public static final String ITEM_API = ROOT_API + "book/item/index";
    public static final String ITEM_SHOW_API = ROOT_API + "book/item/show/";
    public static final String CHAPTERS = "chapters";
    public static final String LESSONS = "lessons";
    public static final String WORDS = "words";
    public static final String ACCEPT = "Accept";
    public static final String APPLICATION_JSON = "application/json";
    public static final String AUTHORIZATION = "authorization";
    public static final String BEARER = "Bearer ";
    public static final String TOKEN = "_token";
    public static final String PUBLIC_KEY_VALUE = "public_key=eyJpdiI6InB3bGtRS2NXT1pheEdDSi8zTjhpTWc9PSIsInZhbHVlIjoiRHBxdVMzaXVYRVFJb2dMNExSblUzeDVOQW5zV1I4WnIzZ1U5cmF0NjhHaG9iMU5WbXZYYnJ3Z0R4RXpPa3FZbEp5amRxL3dYN3d0VmhPRThadUZCamc9PSIsIm1hYyI6IjZjNDgyZjIwOGUxMzU0NjY5Zjg5ZmQ0OTJkZDEyN2RmMGM2NWMyNjNhMTRhZmZjYjRjMDc3ZjU0ZjY4ODJjNTciLCJ0YWciOiIifQ";
    public static final String CODE = "code";
    public static final String FROM = "abmn_from";
    public static final String FROM_TITLE = "abmn_from_title";
    public static final String FROM_TYPE = "abmn_from_type";
    public static final String SUCCESS = "success";
    public static final String ERROR = "error";
    public static final String STATUS = "status";
    public static final String MESSAGE = "message";
    public static final String DATA = "data";
    public static final String IS_SET_VOICE_SPEED = "is_set_voice_speed";
    public static final String VOICE_SPEED = "voice_speed";
    public static final String IS_TEST_ADS = "is_test_ads";

    // Game / user prefs
    public static final String STREAK_DAYS        = "streak_days";
    public static final String TOTAL_XP           = "total_xp";
    public static final String LIPTO_BALANCE      = "lipto_balance";
    public static final String USER_RANK          = "user_rank";
    public static final String USER_NAME          = "user_name";
    public static final String LAST_PLAYED_DATE   = "last_played_date";
    public static final String LAST_CHAPTER_SLUG  = "last_chapter_slug";
    public static final String LAST_CHAPTER_TITLE = "last_chapter_title";

    // Shared date format — used by ResultActivity + StreakReminderReceiver
    public static String todayString() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        return c.get(java.util.Calendar.YEAR) + "-"
                + (c.get(java.util.Calendar.MONTH) + 1) + "-"
                + c.get(java.util.Calendar.DAY_OF_MONTH);
    }
}
