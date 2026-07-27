package com.abmn.englishhub.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

public class LiptoTransferActivity extends AppCompatActivity {

    private TextInputEditText friendCodeET, transferAmountET;
    private MaterialButton findFriendBtn, sendBtn;
    private TextView findErrorTV, sendErrorTV, foundFriendNameTV;
    private CardView foundFriendCV;
    private View sendSectionLL;

    private int foundFriendId = 0;
    private boolean foundIsSelf = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String token = new UConfig(this).getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_lipto_transfer);

        friendCodeET      = findViewById(R.id.friendCodeET);
        transferAmountET  = findViewById(R.id.transferAmountET);
        findFriendBtn     = findViewById(R.id.findFriendBtn);
        sendBtn           = findViewById(R.id.sendBtn);
        findErrorTV       = findViewById(R.id.findErrorTV);
        sendErrorTV       = findViewById(R.id.sendErrorTV);
        foundFriendNameTV = findViewById(R.id.foundFriendNameTV);
        foundFriendCV     = findViewById(R.id.foundFriendCV);
        sendSectionLL     = findViewById(R.id.sendSectionLL);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
        findFriendBtn.setOnClickListener(v -> findFriend());
        sendBtn.setOnClickListener(v -> sendLipto());
    }

    private void findFriend() {
        String code = friendCodeET.getText() != null ? friendCodeET.getText().toString().trim() : "";
        if (code.isEmpty()) { showFindError("বন্ধুর Friend Code দাও"); return; }

        String token = new UConfig(this).getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) { showFindError("লগইন করো আগে"); return; }

        foundFriendCV.setVisibility(View.GONE);
        sendSectionLL.setVisibility(View.GONE);
        showFindError("");

        String url = Constant.GAME_LIPTO_FIND_FRIEND + "?code=" + code;
        ApiConfig.getRequest(this, url, token, response -> {
            try {
                JSONObject r = new JSONObject(response);
                if (!Constant.SUCCESS.equals(r.optString(Constant.STATUS))) {
                    showFindError(r.optString("message", "খুঁজে পাওয়া যায়নি"));
                    return;
                }
                JSONObject user = r.optJSONObject("user");
                if (user == null) { showFindError("খুঁজে পাওয়া যায়নি"); return; }

                foundFriendId = user.optInt("id", 0);
                foundIsSelf = user.optBoolean("is_self", false);
                String name = user.optString("name", "বন্ধু");

                runOnUiThread(() -> {
                    if (foundIsSelf) {
                        showFindError("নিজেকে Lipto পাঠানো যাবে না");
                        foundFriendCV.setVisibility(View.GONE);
                        sendSectionLL.setVisibility(View.GONE);
                        return;
                    }
                    foundFriendNameTV.setText(name + " কে Lipto পাঠাবে");
                    foundFriendCV.setVisibility(View.VISIBLE);
                    sendSectionLL.setVisibility(View.VISIBLE);
                });
            } catch (Exception e) {
                showFindError("সমস্যা হয়েছে");
            }
        }, error -> showFindError("খুঁজে পাওয়া যায়নি"));
    }

    private void sendLipto() {
        if (foundFriendId == 0 || foundIsSelf) { showSendError("আগে একজন বন্ধু খুঁজে বের করো"); return; }

        String amountStr = transferAmountET.getText() != null ? transferAmountET.getText().toString().trim() : "";
        if (amountStr.isEmpty()) { showSendError("পরিমাণ লিখো"); return; }

        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (Exception e) { showSendError("সঠিক পরিমাণ লিখো"); return; }

        if (amount <= 0) { showSendError("সঠিক পরিমাণ লিখো"); return; }

        String token = new UConfig(this).getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) { showSendError("লগইন করো আগে"); return; }

        JSONObject body = new JSONObject();
        try {
            body.put("to_user_id", foundFriendId);
            body.put("amount", amount);
        } catch (Exception ignored) {}

        ApiConfig.postRequest(this, Constant.GAME_LIPTO_TRANSFER, body, token, response -> {
            try {
                JSONObject r = new JSONObject(response);
                if (Constant.SUCCESS.equals(r.optString(Constant.STATUS))) {
                    runOnUiThread(() -> {
                        android.widget.Toast.makeText(this, "🎁 Lipto পাঠানো হয়েছে!", android.widget.Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                } else {
                    showSendError(r.optString("message", "সমস্যা হয়েছে"));
                }
            } catch (Exception e) {
                showSendError("সমস্যা হয়েছে");
            }
        }, error -> {
            String message = error.getMessage();
            showSendError(message != null ? message : "নেটওয়ার্ক সমস্যা");
        });
    }

    private void showFindError(String msg) {
        runOnUiThread(() -> {
            if (msg.isEmpty()) {
                findErrorTV.setVisibility(View.GONE);
            } else {
                findErrorTV.setText(msg);
                findErrorTV.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showSendError(String msg) {
        runOnUiThread(() -> {
            if (msg.isEmpty()) {
                sendErrorTV.setVisibility(View.GONE);
            } else {
                sendErrorTV.setText(msg);
                sendErrorTV.setVisibility(View.VISIBLE);
            }
        });
    }
}
