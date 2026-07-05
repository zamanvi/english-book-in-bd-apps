package com.abmn.englishhub.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText nameET, phoneET, emailET, passwordET;
    private TextView errorTV;
    private MaterialButton registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameET      = findViewById(R.id.registerNameET);
        phoneET     = findViewById(R.id.registerPhoneET);
        emailET     = findViewById(R.id.registerEmailET);
        passwordET  = findViewById(R.id.registerPasswordET);
        errorTV     = findViewById(R.id.registerErrorTV);
        registerBtn = findViewById(R.id.registerBtn);

        registerBtn.setOnClickListener(v -> attemptRegister());
        findViewById(R.id.goToLoginTV).setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name     = nameET.getText()     != null ? nameET.getText().toString().trim()     : "";
        String phone    = phoneET.getText()    != null ? phoneET.getText().toString().trim()    : "";
        String email    = emailET.getText()    != null ? emailET.getText().toString().trim()    : "";
        String password = passwordET.getText() != null ? passwordET.getText().toString()         : "";

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("নাম, ইমেইল এবং পাসওয়ার্ড দিন");
            return;
        }
        if (password.length() < 8) {
            showError("পাসওয়ার্ড কমপক্ষে ৮ অক্ষর হতে হবে");
            return;
        }

        registerBtn.setEnabled(false);
        registerBtn.setText("রেজিস্ট্রেশন হচ্ছে...");
        errorTV.setVisibility(View.GONE);

        String url = Constant.ROOT_API + "register?" + Constant.PUBLIC_KEY_VALUE;
        JSONObject body = new JSONObject();
        try {
            body.put("name", name);
            body.put("phone", phone.isEmpty() ? "01000000000" : phone);
            body.put("email", email);
            body.put("password", password);
        } catch (Exception ignored) {}

        ApiConfig.postRequest(this, url, body, response -> {
            try {
                JSONObject json = new JSONObject(response);
                if (json.optString(Constant.STATUS, "").equals(Constant.SUCCESS)) {
                    JSONObject data = json.optJSONObject(Constant.DATA);
                    if (data == null) { showError("Server error"); return; }

                    String token = data.optString("token", "");
                    UConfig uConfig = new UConfig(this);
                    uConfig.setData(Constant.TOKEN, token);
                    uConfig.setData("name", name);

                    startActivity(new Intent(this, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                    finish();
                } else {
                    showError(json.optString(Constant.MESSAGE, "রেজিস্ট্রেশন ব্যর্থ হয়েছে"));
                }
            } catch (Exception e) {
                showError("সংযোগ ব্যর্থ হয়েছে");
            }
        }, error -> showError("সংযোগ ব্যর্থ হয়েছে"));
    }

    private void showError(String msg) {
        runOnUiThread(() -> {
            errorTV.setText(msg);
            errorTV.setVisibility(View.VISIBLE);
            registerBtn.setEnabled(true);
            registerBtn.setText("রেজিস্ট্রেশন করো");
        });
    }
}
