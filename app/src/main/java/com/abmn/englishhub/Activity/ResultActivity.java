package com.abmn.englishhub.Activity;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    private TextView resultEmojiTV, resultGradeTV, resultSubtitleTV;
    private TextView correctCountTV, totalCountTV, scorePercentTV;
    private TextView xpEarnedBigTV, streakResultTV, accuracyTV;
    private TextView newRankTV, liptoEarnedTV, liptoIconTV, liptoLabelTV;
    private CardView rankCard, liptoEarnedCard;
    private ProgressBar scoreBar;

    private CardView battleResultCV;
    private TextView battleResultTitleTV, battleResultDetailTV;

    private int correct, total, xpEarned, lessonId, battleId, timeSec;
    private boolean submitFailureNotified = false;

    // -1 = this battle had no lives cap (unlimited attempts, nothing to report)
    private int livesRemaining;

    private ToneGenerator toneGenerator;

    // Round-based Quick Quiz (level-map flow) — round 0 means the legacy
    // single-round quiz, which keeps its existing xp/streak/lipto/battle
    // submit path untouched below.
    private int round, heartsLost;
    private boolean roundPassed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        correct  = getIntent().getIntExtra("correct", 0);
        total    = getIntent().getIntExtra("total", 10);
        xpEarned = getIntent().getIntExtra("xp", 0);
        lessonId = getIntent().getIntExtra("lesson_id", 1);
        battleId = getIntent().getIntExtra("battle_id", 0);
        timeSec  = getIntent().getIntExtra("time_sec", 0);
        round      = getIntent().getIntExtra("round", 0);
        heartsLost = getIntent().getIntExtra("hearts_lost", 0);
        // Was hardcoded to "< 3", silently assuming every round always uses
        // exactly 3 lives. QuizActivity/WritingActivity now send the real
        // cap they actually played with (max_lives) - default 3 only
        // covers an old client on the same account replaying a cached
        // Intent shape that predates this extra.
        int maxLivesForRound = getIntent().getIntExtra("max_lives", 3);
        roundPassed = heartsLost < maxLivesForRound;
        livesRemaining = getIntent().getIntExtra("lives_remaining", -1);

        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 80);
        } catch (RuntimeException e) {
            toneGenerator = null;
        }

        bindViews();
        populateStatic();
        animateScore();
        // Guard against double-crediting XP/Lipto/streak (or a duplicate
        // round-submit call unlocking stars twice) if this Activity gets
        // recreated after onCreate already fired once - a screen rotation
        // or the system killing/restoring a backgrounded app both replay
        // onCreate() with the same result Intent extras still attached.
        // savedInstanceState is null only on the very first creation.
        if (savedInstanceState == null) {
            if (round > 0) {
                submitRoundResult();
            } else {
                submitXpAndStreak();
            }
        }
        setupButtons();
    }

    private void bindViews() {
        resultEmojiTV    = findViewById(R.id.resultEmojiTV);
        resultGradeTV    = findViewById(R.id.resultGradeTV);
        resultSubtitleTV = findViewById(R.id.resultSubtitleTV);
        correctCountTV   = findViewById(R.id.correctCountTV);
        totalCountTV     = findViewById(R.id.totalCountTV);
        scorePercentTV   = findViewById(R.id.scorePercentTV);
        xpEarnedBigTV    = findViewById(R.id.xpEarnedBigTV);
        streakResultTV   = findViewById(R.id.streakResultTV);
        accuracyTV       = findViewById(R.id.accuracyTV);
        newRankTV        = findViewById(R.id.newRankTV);
        rankCard         = findViewById(R.id.rankCard);
        scoreBar         = findViewById(R.id.scoreBar);
        liptoEarnedTV    = findViewById(R.id.liptoEarnedTV);
        liptoEarnedCard  = findViewById(R.id.liptoEarnedCard);
        liptoIconTV      = findViewById(R.id.liptoIconTV);
        liptoLabelTV     = findViewById(R.id.liptoLabelTV);
        battleResultCV       = findViewById(R.id.battleResultCV);
        battleResultTitleTV  = findViewById(R.id.battleResultTitleTV);
        battleResultDetailTV = findViewById(R.id.battleResultDetailTV);
    }

    private void populateStatic() {
        int pct = total > 0 ? (correct * 100) / total : 0;

        totalCountTV.setText(String.valueOf(total));
        correctCountTV.setText(String.valueOf(correct));
        scorePercentTV.setText(pct + "%");
        accuracyTV.setText(pct + "%");
        xpEarnedBigTV.setText("+" + xpEarned);

        // Mark today as played — suppresses the 8pm streak reminder
        String today = Constant.todayString();
        getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .edit().putString(Constant.LAST_PLAYED_DATE, today).apply();

        if (round > 0) {
            // Round-based flow: pass/fail comes from hearts, not score — see
            // the design brief's "hearts subsumes score threshold" rule.
            int stars = heartsLost == 0 ? 3 : heartsLost == 1 ? 2 : 1;
            if (roundPassed) {
                resultEmojiTV.setText(repeat("⭐", stars));
                resultGradeTV.setText("রাউন্ড " + round + " ক্লিয়ার! 🏆");
                resultSubtitleTV.setText(stars == 3 ? "পারফেক্ট! কোনো হার্ট হারাওনি!" : "দারুণ! পরের রাউন্ড খুলে গেছে।");
            } else {
                resultEmojiTV.setText("💔");
                resultGradeTV.setText("আরেকটু চেষ্টা করা লাগবে");
                resultSubtitleTV.setText("আরেকবার ট্রাই করো, তুমি পারবে!");
            }
        } else if (pct >= 90) {
            resultEmojiTV.setText("🏆");
            resultGradeTV.setText("অসাধারণ!");
            resultSubtitleTV.setText("তুমি আজ দারুণ করেছ! একদম perfect! 🔥");
        } else if (pct >= 70) {
            resultEmojiTV.setText("⭐");
            resultGradeTV.setText("বাহ, ভালো হয়েছে!");
            resultSubtitleTV.setText("প্রায় নিখুঁত! চালিয়ে যাও, তুমি পারবে!");
        } else if (pct >= 50) {
            resultEmojiTV.setText("👍");
            resultGradeTV.setText("চেষ্টা ভালো ছিল!");
            resultSubtitleTV.setText("আরও অনুশীলন করো — তুমি আরও ভালো করবে!");
        } else {
            resultEmojiTV.setText("💪");
            resultGradeTV.setText("থামো না!");
            resultSubtitleTV.setText("প্রতিটি চেষ্টায় তুমি শিখছ। হাল ছাড়ো না!");
        }

        // Streak from prefs (updated after API call)
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        int streak = prefs.getInt(Constant.STREAK_DAYS, 0);
        streakResultTV.setText("🔥 " + streak);
    }

    // ── Animations ───────────────────────────────────────────────

    private void animateScore() {
        int pct = total > 0 ? (correct * 100) / total : 0;

        // Score bar fill
        ValueAnimator barAnim = ValueAnimator.ofInt(0, pct);
        barAnim.setDuration(1000);
        barAnim.setStartDelay(300);
        barAnim.setInterpolator(new DecelerateInterpolator());
        barAnim.addUpdateListener(a -> scoreBar.setProgress((int) a.getAnimatedValue()));
        barAnim.start();

        // XP counter count-up
        ValueAnimator xpAnim = ValueAnimator.ofInt(0, xpEarned);
        xpAnim.setDuration(800);
        xpAnim.setStartDelay(500);
        xpAnim.setInterpolator(new DecelerateInterpolator());
        xpAnim.addUpdateListener(a -> xpEarnedBigTV.setText("+" + a.getAnimatedValue()));
        xpAnim.start();
    }

    private static String repeat(String s, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) sb.append(s);
        return sb.toString();
    }

    // 🎁 mystery-box "unwrap" moment — pop-in with overshoot rather than a
    // plain fade, so a surprise reward actually feels like a surprise.
    private void revealMysteryBox() {
        liptoEarnedCard.setVisibility(View.VISIBLE);
        liptoEarnedCard.setAlpha(0f);
        liptoEarnedCard.setScaleX(0.6f);
        liptoEarnedCard.setScaleY(0.6f);
        liptoEarnedCard.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(420)
                .setInterpolator(new android.view.animation.OvershootInterpolator(2.2f))
                .start();
    }

    // ── API calls ────────────────────────────────────────────────

    // Round-based flow's single atomic call — replaces submitXp() +
    // updateStreak() + submitLipto() with one request that updates XP,
    // Lipto (incl. mystery box), streak, stars, and next-round unlock
    // together server-side (see GameController::submitRound).
    private void submitRoundResult() {
        UConfig uConfig = new UConfig(this);
        String token = uConfig.getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) return;

        JSONObject body = new JSONObject();
        try {
            body.put("lesson_id", lessonId);
            body.put("round", round);
            body.put("score", correct);
            body.put("total", total);
            body.put("hearts_lost", heartsLost);
        } catch (Exception ignored) {}

        ApiConfig.postRequest(this, Constant.GAME_ROUND_SUBMIT, body, token, response -> {
            try {
                JSONObject json = new JSONObject(response);
                if (!json.optString(Constant.STATUS, "").equals(Constant.SUCCESS)) return;
                JSONObject data = json.optJSONObject(Constant.DATA);
                if (data == null) return;

                int totalXp   = data.optInt("total_xp", 0);
                int streak    = data.optInt("streak_days", 0);
                int liptoWon  = data.optInt("lipto_earned", 0);
                JSONObject box = data.optJSONObject("mystery_box");

                getSharedPreferences("app_prefs", MODE_PRIVATE)
                        .edit()
                        .putInt(Constant.TOTAL_XP, totalXp)
                        .putInt(Constant.STREAK_DAYS, streak)
                        .apply();

                runOnUiThread(() -> {
                    streakResultTV.setText("🔥 " + streak);
                    if (box != null && liptoWon > 0) {
                        String tier = box.optString("tier", "common");
                        String tierLabel = "epic".equals(tier) ? "জ্যাকপট! এক্সক্লুসিভ মিস্ট্রি বক্স"
                                : "rare".equals(tier) ? "বিরল মিস্ট্রি বক্স"
                                : "মিস্ট্রি বক্স থেকে";
                        liptoIconTV.setText("epic".equals(tier) ? "🌟" : "rare".equals(tier) ? "💎" : "🪙");
                        liptoLabelTV.setText(tierLabel);
                        liptoEarnedTV.setText("+" + liptoWon);
                        revealMysteryBox();
                    }
                });
            } catch (Exception ignored) {}
        }, error -> notifySubmitFailure());
    }

    private void submitXpAndStreak() {
        submitXp();
        updateStreak();
        if (xpEarned > 0) submitLipto();
        if (battleId > 0) submitBattleResult();
    }

    // XP/streak/Lipto/battle submissions can fail independently and silently
    // (network drop right after a quiz) with no retry - at minimum let the
    // user know their result may not have saved, once per screen visit.
    private void notifySubmitFailure() {
        if (submitFailureNotified) return;
        submitFailureNotified = true;
        runOnUiThread(() -> android.widget.Toast.makeText(this,
                "নেটওয়ার্ক সমস্যা — ফলাফল সেভ নাও হতে পারে",
                android.widget.Toast.LENGTH_LONG).show());
    }

    private void submitXp() {
        UConfig uConfig = new UConfig(this);
        String token = uConfig.getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) return;

        JSONObject body = new JSONObject();
        try {
            body.put("score", correct);
            body.put("total", total);
            body.put("lesson_id", lessonId);
        } catch (Exception ignored) {}

        ApiConfig.postRequest(this, Constant.GAME_XP, body, token, response -> {
            try {
                JSONObject json = new JSONObject(response);
                if (json.optString(Constant.STATUS, "").equals(Constant.SUCCESS)) {
                    JSONObject data = json.optJSONObject(Constant.DATA);
                    if (data == null) return;
                    int newRank   = data.optInt("rank", 0);
                    int totalXp   = data.optInt("total_xp", 0);
                    getSharedPreferences("app_prefs", MODE_PRIVATE)
                            .edit()
                            .putInt(Constant.USER_RANK, newRank)
                            .putInt(Constant.TOTAL_XP, totalXp)
                            .apply();
                    if (newRank > 0) {
                        runOnUiThread(() -> {
                            newRankTV.setText("#" + newRank);
                            rankCard.setVisibility(View.VISIBLE);
                        });
                    }
                }
            } catch (Exception ignored) {}
        }, error -> notifySubmitFailure());
    }

    private void updateStreak() {
        UConfig uConfig = new UConfig(this);
        String token = uConfig.getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) return;

        ApiConfig.postRequest(this, Constant.GAME_STREAK_UPDATE, new JSONObject(), token, response -> {
            try {
                JSONObject json = new JSONObject(response);
                if (json.optString(Constant.STATUS, "").equals(Constant.SUCCESS)) {
                    JSONObject data = json.optJSONObject(Constant.DATA);
                    if (data == null) return;
                    int streak = data.optInt("streak_days", 0);
                    // Save to prefs and update UI
                    getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                            .edit().putInt(Constant.STREAK_DAYS, streak).apply();
                    runOnUiThread(() -> streakResultTV.setText("🔥 " + streak));
                }
            } catch (Exception ignored) {}
        }, error -> notifySubmitFailure());
    }

    private void submitLipto() {
        UConfig uConfig = new UConfig(this);
        String token = uConfig.getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) return;

        JSONObject body = new JSONObject();
        try {
            body.put("amount", xpEarned);
            body.put("source", "quiz");
            body.put("description", "Quiz lesson #" + lessonId + " — " + correct + "/" + total + " correct");
        } catch (Exception ignored) {}

        ApiConfig.postRequest(this, Constant.GAME_LIPTO_EARN, body, token, response -> {
            try {
                JSONObject json = new JSONObject(response);
                if (json.optString(Constant.STATUS, "").equals(Constant.SUCCESS)) {
                    int newBalance = json.optInt("balance", 0);
                    SharedPreferences liptoPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                    int maxBalance = json.optInt("max_balance", newBalance);
                    liptoPrefs.edit()
                            .putInt(Constant.LIPTO_BALANCE, newBalance)
                            .putInt(Constant.LIPTO_MAX_BALANCE, Math.max(maxBalance, liptoPrefs.getInt(Constant.LIPTO_MAX_BALANCE, 0)))
                            .apply();
                    runOnUiThread(() -> {
                        liptoEarnedTV.setText("+" + xpEarned);
                        liptoEarnedCard.setVisibility(View.VISIBLE);
                    });
                }
            } catch (Exception ignored) {}
        }, error -> notifySubmitFailure());
    }

    private void submitBattleResult() {
        UConfig uConfig = new UConfig(this);
        String token = uConfig.getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) return;

        JSONObject body = new JSONObject();
        try {
            body.put("score", correct);
            body.put("total", total);
            body.put("time_sec", timeSec);
            if (livesRemaining >= 0) body.put("lives_remaining", livesRemaining);
        } catch (Exception ignored) {}

        String url = Constant.BATTLE_BASE + battleId + "/submit";
        ApiConfig.postRequest(this, url, body, token, response -> {
            try {
                JSONObject r = new JSONObject(response);
                if (Constant.SUCCESS.equals(r.optString(Constant.STATUS))) {
                    JSONObject battle = r.optJSONObject("battle");
                    if (battle != null) runOnUiThread(() -> renderBattleResult(battle));
                }
            } catch (Exception ignored) {}
        }, error -> notifySubmitFailure());
    }

    // Was previously silent - a player got no immediate win/loss/draw
    // feedback here, only much later in BattleActivity's history list.
    // Reuses the same win/loss/draw color palette as BattleAdapter so the
    // two screens read as one consistent system.
    private void renderBattleResult(JSONObject battle) {
        String status = battle.optString("status", "");
        String result = battle.optString("result", "");
        JSONArray participants = battle.optJSONArray("participants");

        battleResultCV.setVisibility(View.VISIBLE);

        if (!"completed".equals(status)) {
            battleResultTitleTV.setText("⏳ অন্যরা এখনও খেলেনি");
            battleResultTitleTV.setTextColor(Color.parseColor("#7A84B8"));
            battleResultDetailTV.setText("সবাই খেলা শেষ করলে ফলাফল জানতে পারবে");
            return;
        }

        int titleColor;
        int bannerColor;
        switch (result) {
            case "win":
                battleResultTitleTV.setText("🎉 তুমি জিতেছ!");
                titleColor = Color.parseColor("#4ade80");
                bannerColor = Color.parseColor("#2234D399"); // teal_dim
                playTone(ToneGenerator.TONE_PROP_ACK);
                break;
            case "loss":
                battleResultTitleTV.setText("😔 এবার হেরেছ");
                titleColor = Color.parseColor("#f87171");
                bannerColor = Color.parseColor("#22F43F5E"); // red_wrong_dim
                playTone(ToneGenerator.TONE_PROP_NACK);
                break;
            default:
                battleResultTitleTV.setText("🤝 ড্র হয়েছে!");
                titleColor = Color.parseColor("#facc15");
                bannerColor = Color.parseColor("#22FBBF24"); // gold_dim
                playTone(ToneGenerator.TONE_PROP_BEEP);
        }
        battleResultTitleTV.setTextColor(titleColor);
        battleResultCV.setCardBackgroundColor(bannerColor);

        if (participants != null && participants.length() > 2) {
            // Multi-player - show a ranked "name: score" list instead of a
            // single "my - their" line.
            java.util.List<JSONObject> sorted = new ArrayList<>();
            for (int i = 0; i < participants.length(); i++) {
                JSONObject p = participants.optJSONObject(i);
                if (p != null) sorted.add(p);
            }
            sorted.sort((a, b) -> b.optInt("score", 0) - a.optInt("score", 0));

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sorted.size(); i++) {
                JSONObject p = sorted.get(i);
                if (i > 0) sb.append("\n");
                String name = p.optBoolean("is_me", false) ? "তুমি" : p.optString("name", "বন্ধু");
                sb.append(i + 1).append(". ").append(name).append(" — ").append(p.optInt("score", 0));
                if (p.optInt("lives_remaining", -1) == 0) sb.append(" 💔 আউট");
            }
            battleResultDetailTV.setText(sb.toString());
        } else {
            int myScore = battle.optInt("my_score", correct);
            int theirScore = battle.optInt("their_score", -1);
            battleResultDetailTV.setText(myScore + " - " + (theirScore >= 0 ? theirScore : "?"));
        }
    }

    // ── Buttons ──────────────────────────────────────────────────

    private void setupButtons() {
        findViewById(R.id.playAgainBtn).setOnClickListener(v -> {
            if (round > 0) {
                // Passed → the level map is the next natural stop (it shows
                // the newly-unlocked round); failed → retry this same round,
                // per the "only this round resets" rule, not the whole game.
                if (roundPassed) {
                    startActivity(new Intent(this, LevelMapActivity.class)
                            .putExtra("lesson_id", lessonId));
                } else if (round == 5) {
                    // Round 5 (Writing) has its own dedicated typed-answer
                    // Activity - QuizActivity can't render its question
                    // shape (no options/correct_index, just prompt/expected_answer).
                    startActivity(new Intent(this, WritingActivity.class)
                            .putExtra("lesson_id", lessonId));
                } else {
                    startActivity(new Intent(this, QuizActivity.class)
                            .putExtra("lesson_id", lessonId)
                            .putExtra("round", round));
                }
            } else {
                startActivity(new Intent(this, QuizActivity.class)
                        .putExtra("lesson_id", lessonId));
            }
            finish();
        });

        findViewById(R.id.homeBtn).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            finish();
        });

        findViewById(R.id.shareResultBtn).setOnClickListener(v -> shareResult());
    }

    private void shareResult() {
        int pct = total > 0 ? (correct * 100) / total : 0;
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        int streak = prefs.getInt(Constant.STREAK_DAYS, 0);

        String grade = pct >= 90 ? "অসাধারণ! 🏆"
                : pct >= 70 ? "বাহ, ভালো! ⭐"
                : pct >= 50 ? "ভালো চেষ্টা! 👍"
                : "হাল ছাড়িনি! 💪";

        String text = "আমি English Grammar Book-এ Quiz খেললাম!\n\n"
                + "✅ " + correct + "/" + total + " সঠিক (" + pct + "%)\n"
                + "⚡ +" + xpEarned + " XP অর্জন\n"
                + "🔥 " + streak + " দিনের Streak\n"
                + grade + "\n\n"
                + "তুমিও শেখা শুরু করো 👇\n"
                + "https://play.google.com/store/apps/details?id=" + getPackageName()
                + "\n#EnglishGrammarBook #LearnEnglish";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(shareIntent, "শেয়ার করো"));
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        finish();
        super.onBackPressed();
    }

    // Same mute toggle QuizActivity's sound effects respect (LevelMapActivity's
    // speaker icon), so battle-result cues don't play when the user muted quiz sfx.
    private void playTone(int tone) {
        boolean soundEnabled = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean(Constant.SOUND_ENABLED, true);
        if (!soundEnabled || toneGenerator == null) return;
        try {
            toneGenerator.startTone(tone, 200);
        } catch (Exception ignored) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }
}
