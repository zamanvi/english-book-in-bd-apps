package com.abmn.englishhub.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CreateChallengeActivity extends AppCompatActivity {

    private AutoCompleteTextView lessonDropdown, questionCountDropdown;
    private TextInputLayout customCountLayout;
    private TextInputEditText customCountET, livesET, friendCodeET;
    private MaterialButton addFriendBtn, createBtn, shareCodeBtn, startPlayingBtn;
    private TextView addFriendErrorTV, createErrorTV, inviteCodeTV, allowCodeJoinHintTV, maxPlayersTV, modeLiveBadgeTV;
    private LinearLayout invitedFriendsLL;
    private androidx.cardview.widget.CardView successCV;
    private SwitchCompat allowCodeJoinSwitch;

    private final List<String> lessonNames = new ArrayList<>();
    private final List<Integer> lessonIds = new ArrayList<>();
    private int selectedLessonId = 0;
    private int selectedQuestionCount = 10;

    // friendCode -> {id, name}
    private final Map<Integer, String> invitees = new LinkedHashMap<>();

    private ToneGenerator toneGenerator;

    private String createdInviteCode = null;
    private int createdBattleId = 0;
    private int createdLessonId = 0;
    private int createdQuestionCount = 10;
    private int createdLives = 0;
    private String createdWordIds = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String token = new UConfig(this).getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 80);
        } catch (RuntimeException e) {
            toneGenerator = null;
        }

        setContentView(R.layout.activity_create_challenge);

        lessonDropdown        = findViewById(R.id.lessonDropdown);
        questionCountDropdown = findViewById(R.id.questionCountDropdown);
        customCountLayout      = findViewById(R.id.customCountLayout);
        customCountET          = findViewById(R.id.customCountET);
        livesET                = findViewById(R.id.livesET);
        friendCodeET           = findViewById(R.id.friendCodeET);
        addFriendBtn           = findViewById(R.id.addFriendBtn);
        createBtn              = findViewById(R.id.createBtn);
        shareCodeBtn           = findViewById(R.id.shareCodeBtn);
        startPlayingBtn        = findViewById(R.id.startPlayingBtn);
        addFriendErrorTV       = findViewById(R.id.addFriendErrorTV);
        createErrorTV          = findViewById(R.id.createErrorTV);
        inviteCodeTV           = findViewById(R.id.inviteCodeTV);
        invitedFriendsLL       = findViewById(R.id.invitedFriendsLL);
        successCV              = findViewById(R.id.successCV);
        allowCodeJoinSwitch    = findViewById(R.id.allowCodeJoinSwitch);
        allowCodeJoinHintTV    = findViewById(R.id.allowCodeJoinHintTV);
        maxPlayersTV           = findViewById(R.id.maxPlayersTV);
        modeLiveBadgeTV        = findViewById(R.id.modeLiveBadgeTV);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
        modeLiveBadgeTV.setOnClickListener(v -> Toast.makeText(this,
                "🚀 লাইভ মোড শীঘ্রই আসছে! এখন শুধু 'একে একে খেলবে' মোডে খেলা যাচ্ছে",
                Toast.LENGTH_LONG).show());

        setupQuestionCountDropdown();
        loadLessons();
        lessonDropdown.setOnClickListener(v -> {
            if (lessonNames.isEmpty()) loadLessons();
        });

        addFriendBtn.setOnClickListener(v -> addFriend());
        createBtn.setOnClickListener(v -> createChallenge());
        shareCodeBtn.setOnClickListener(v -> shareCode());
        startPlayingBtn.setOnClickListener(v -> startPlaying());

        allowCodeJoinSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateMaxPlayersHint());
        updateAllowCodeJoinGuard();
    }

    private void setupQuestionCountDropdown() {
        List<String> options = new ArrayList<>();
        options.add("১০ টি প্রশ্ন");
        options.add("২০ টি প্রশ্ন");
        options.add("৫০ টি প্রশ্ন");
        options.add("✏️ কাস্টম সংখ্যা");
        int[] values = {10, 20, 50, -1};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_dropdown_text, options);
        questionCountDropdown.setAdapter(adapter);
        questionCountDropdown.setText(options.get(0), false);
        selectedQuestionCount = 10;

        questionCountDropdown.setOnItemClickListener((parent, view, position, id) -> {
            int val = values[position];
            if (val == -1) {
                customCountLayout.setVisibility(View.VISIBLE);
            } else {
                customCountLayout.setVisibility(View.GONE);
                selectedQuestionCount = val;
            }
        });
    }

    private void loadLessons() {
        String url = Constant.ROOT_API2 + "lessons";
        ApiConfig.RequestToVolley((result, response, error) -> {
            if (!result) {
                runOnUiThread(() -> {
                    lessonDropdown.setText("লোড ব্যর্থ — আবার চেষ্টা করতে ট্যাপ করো", false);
                    Toast.makeText(this, "লেসন লোড করা যায়নি, ইন্টারনেট চেক করো", Toast.LENGTH_SHORT).show();
                });
                return;
            }
            try {
                JSONArray arr = new JSONObject(response).optJSONArray("lessons");
                if (arr == null) return;
                lessonNames.clear(); lessonIds.clear();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject l = arr.getJSONObject(i);
                    lessonNames.add(l.optString("title", "Lesson " + l.optInt("id")));
                    lessonIds.add(l.optInt("id"));
                }
                runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            R.layout.item_dropdown_text, lessonNames);
                    lessonDropdown.setAdapter(adapter);
                    lessonDropdown.setOnItemClickListener((parent, view, position, id) ->
                            selectedLessonId = lessonIds.get(position));
                    if (!lessonNames.isEmpty()) lessonDropdown.setText("", false);
                });
            } catch (Exception ignored) {}
        }, com.android.volley.Request.Method.GET, this, url, new java.util.HashMap<>(), false);
    }

    private void addFriend() {
        String code = friendCodeET.getText() != null ? friendCodeET.getText().toString().trim() : "";
        if (code.isEmpty()) { showAddFriendError("বন্ধুর Friend Code দাও"); return; }

        String token = new UConfig(this).getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) { showAddFriendError("লগইন করো আগে"); return; }

        if (!new UConfig(this).isConnected()) {
            new UConfig(this).isConnectedAlert("ইন্টারনেট সংযোগ নেই", "এই ফিচারটি ব্যবহার করতে ইন্টারনেট সংযোগ প্রয়োজন");
            return;
        }

        showAddFriendError("");
        addFriendBtn.setEnabled(false);
        String url = Constant.GAME_LIPTO_FIND_FRIEND + "?code=" + code;
        ApiConfig.getRequest(this, url, token, response -> {
            runOnUiThread(() -> addFriendBtn.setEnabled(true));
            try {
                JSONObject r = new JSONObject(response);
                if (!Constant.SUCCESS.equals(r.optString(Constant.STATUS))) {
                    showAddFriendError(r.optString("message", "খুঁজে পাওয়া যায়নি"));
                    return;
                }
                JSONObject user = r.optJSONObject("user");
                if (user == null) { showAddFriendError("খুঁজে পাওয়া যায়নি"); return; }

                boolean isSelf = user.optBoolean("is_self", false);
                if (isSelf) { showAddFriendError("নিজেকে ইনভাইট করা যাবে না"); return; }

                int id = user.optInt("id", 0);
                String name = user.optString("name", "বন্ধু");

                runOnUiThread(() -> {
                    if (invitees.containsKey(id)) {
                        showAddFriendError(name + " আগেই যোগ করা হয়েছে");
                        return;
                    }
                    invitees.put(id, name);
                    renderInvitees();
                    friendCodeET.setText("");
                });
            } catch (Exception e) {
                showAddFriendError("সমস্যা হয়েছে");
            }
        }, error -> {
            runOnUiThread(() -> addFriendBtn.setEnabled(true));
            showAddFriendError("খুঁজে পাওয়া যায়নি");
        });
    }

    private void renderInvitees() {
        invitedFriendsLL.removeAllViews();
        for (Map.Entry<Integer, String> entry : invitees.entrySet()) {
            int id = entry.getKey();
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, 12, 0, 12);

            TextView nameTV = new TextView(this);
            nameTV.setText("👤 " + entry.getValue());
            nameTV.setTextColor(Color.parseColor("#F0EEFF"));
            nameTV.setTextSize(14);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            nameTV.setLayoutParams(lp);

            TextView removeTV = new TextView(this);
            removeTV.setText("✕");
            removeTV.setTextColor(Color.parseColor("#F43F5E"));
            removeTV.setTextSize(16);
            removeTV.setPadding(16, 0, 8, 0);
            removeTV.setOnClickListener(v -> {
                invitees.remove(id);
                renderInvitees();
            });

            row.addView(nameTV);
            row.addView(removeTV);
            invitedFriendsLL.addView(row);
        }
        updateAllowCodeJoinGuard();
    }

    // A code is only ever generated when it will actually be joinable
    // (server mirrors this: allow_code_join OFF + no invitees would
    // otherwise produce a code locked to max_participants=1, i.e. a
    // permanent dead end). So once the invitee list is empty, force the
    // switch on and lock it - the user simply can't create that broken
    // combination from this screen.
    private void updateAllowCodeJoinGuard() {
        boolean noFriends = invitees.isEmpty();
        if (noFriends) {
            allowCodeJoinSwitch.setChecked(true);
        }
        allowCodeJoinSwitch.setEnabled(!noFriends);
        allowCodeJoinHintTV.setVisibility(noFriends ? View.VISIBLE : View.GONE);
        updateMaxPlayersHint();
    }

    private void updateMaxPlayersHint() {
        boolean codeJoinOpen = invitees.isEmpty() || allowCodeJoinSwitch.isChecked();
        maxPlayersTV.setVisibility(View.VISIBLE);
        if (codeJoinOpen) {
            maxPlayersTV.setText("👥 কোড দিয়ে যেকেউ যোগ দিতে পারবে (আনলিমিটেড)");
        } else {
            int max = invitees.size() + 1;
            maxPlayersTV.setText("👥 সর্বোচ্চ " + max + " জন খেলতে পারবে (তুমি + " + invitees.size() + " বন্ধু)");
        }
    }

    private void createChallenge() {
        if (selectedLessonId == 0) { showCreateError("একটা Lesson বেছে নাও"); return; }

        int questionCount = selectedQuestionCount;
        if (customCountLayout.getVisibility() == View.VISIBLE) {
            String customStr = customCountET.getText() != null ? customCountET.getText().toString().trim() : "";
            if (customStr.isEmpty()) { showCreateError("কতগুলো প্রশ্ন লিখো"); return; }
            try {
                questionCount = Integer.parseInt(customStr);
            } catch (Exception e) { showCreateError("সঠিক সংখ্যা লিখো"); return; }
            if (questionCount < 1 || questionCount > 50) { showCreateError("১ থেকে ৫০ এর মধ্যে দাও"); return; }
        }

        Integer lives = null;
        String livesStr = livesET.getText() != null ? livesET.getText().toString().trim() : "";
        if (!livesStr.isEmpty()) {
            try {
                lives = Integer.parseInt(livesStr);
            } catch (Exception e) { showCreateError("Lives-এ সঠিক সংখ্যা দাও"); return; }
            if (lives < 1 || lives > 50) { showCreateError("Lives ১ থেকে ৫০ এর মধ্যে দাও"); return; }
        }

        String token = new UConfig(this).getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) { showCreateError("লগইন করো আগে"); return; }

        if (!new UConfig(this).isConnected()) {
            new UConfig(this).isConnectedAlert("ইন্টারনেট সংযোগ নেই", "এই ফিচারটি ব্যবহার করতে ইন্টারনেট সংযোগ প্রয়োজন");
            return;
        }

        showCreateError("");
        createBtn.setEnabled(false);
        createBtn.setText("⏳ তৈরি হচ্ছে...");

        JSONObject body = new JSONObject();
        try {
            body.put("lesson_id", selectedLessonId);
            body.put("question_count", questionCount);
            if (lives != null) body.put("lives", lives);
            body.put("mode", "async");
            body.put("allow_code_join", allowCodeJoinSwitch.isChecked());
            JSONArray inviteeArr = new JSONArray();
            for (Integer id : invitees.keySet()) inviteeArr.put(id);
            body.put("invitee_ids", inviteeArr);
        } catch (Exception ignored) {}

        final int finalQuestionCount = questionCount;
        final Integer finalLives = lives;

        ApiConfig.postRequest(this, Constant.BATTLE_CREATE, body, token, response -> {
            try {
                JSONObject r = new JSONObject(response);
                if (!Constant.SUCCESS.equals(r.optString(Constant.STATUS))) {
                    runOnUiThread(() -> resetCreateBtn());
                    showCreateError(r.optString("message", "সমস্যা হয়েছে"));
                    return;
                }
                createdBattleId = r.optInt("battle_id", 0);
                createdInviteCode = r.optString("invite_code", null);
                createdLessonId = selectedLessonId;
                createdQuestionCount = finalQuestionCount;
                createdLives = finalLives != null ? finalLives : 0;
                JSONArray wordIdsArr = r.optJSONArray("question_word_ids");
                createdWordIds = wordIdsArr != null ? joinIntArray(wordIdsArr) : "";
                playTone(ToneGenerator.TONE_PROP_ACK);

                boolean hasCode = createdInviteCode != null && !createdInviteCode.isEmpty();

                runOnUiThread(() -> {
                    createBtn.setEnabled(false);
                    createBtn.setText("✅ তৈরি হয়েছে");

                    if (hasCode) {
                        // A shareable code exists - stay on screen so the
                        // user can copy/share it before jumping into the
                        // (timed) quiz themselves.
                        successCV.setVisibility(View.VISIBLE);
                        inviteCodeTV.setText(createdInviteCode);
                        inviteCodeTV.setVisibility(View.VISIBLE);
                        shareCodeBtn.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "🎉 চ্যালেঞ্জ রেডি! কোড শেয়ার করো বা এখনই খেলা শুরু করো", Toast.LENGTH_LONG).show();
                    } else {
                        // Invite-only challenge with no shareable code -
                        // nothing to show/share, so skip the extra tap and
                        // go straight into the quiz.
                        Toast.makeText(this, "🎉 চ্যালেঞ্জ তৈরি হয়েছে! বন্ধুদের নোটিফিকেশন পাঠানো হয়েছে, খেলা শুরু হচ্ছে...", Toast.LENGTH_SHORT).show();
                        startPlaying();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(this::resetCreateBtn);
                showCreateError("সমস্যা হয়েছে");
            }
        }, error -> {
            runOnUiThread(this::resetCreateBtn);
            String message = error.getMessage();
            showCreateError(message != null ? message : "নেটওয়ার্ক সমস্যা");
        });
    }

    private void resetCreateBtn() {
        createBtn.setEnabled(true);
        createBtn.setText("🚀 চ্যালেঞ্জ তৈরি করো");
    }

    private void shareCode() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,
                "আমার সাথে একটা Battle খেলো! ⚔️\n" +
                "কোড: " + createdInviteCode + "\n" +
                "ডাউনলোড করো: https://play.google.com/store/apps/details?id=com.abmn.englishhub");
        startActivity(Intent.createChooser(intent, "শেয়ার করো"));
    }

    private void startPlaying() {
        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra("lesson_id", createdLessonId);
        intent.putExtra("battle_id", createdBattleId);
        intent.putExtra("question_count", createdQuestionCount);
        intent.putExtra("lives", createdLives);
        if (!createdWordIds.isEmpty()) intent.putExtra("word_ids", createdWordIds);
        startActivity(intent);
        finish();
    }

    private String joinIntArray(JSONArray arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length(); i++) {
            if (sb.length() > 0) sb.append(",");
            sb.append(arr.optInt(i));
        }
        return sb.toString();
    }

    private void showAddFriendError(String msg) {
        runOnUiThread(() -> {
            if (msg.isEmpty()) {
                addFriendErrorTV.setVisibility(View.GONE);
            } else {
                addFriendErrorTV.setText(msg);
                addFriendErrorTV.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showCreateError(String msg) {
        runOnUiThread(() -> {
            if (msg.isEmpty()) {
                createErrorTV.setVisibility(View.GONE);
            } else {
                createErrorTV.setText(msg);
                createErrorTV.setVisibility(View.VISIBLE);
            }
        });
    }

    // Same mute toggle QuizActivity's sound effects respect.
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
