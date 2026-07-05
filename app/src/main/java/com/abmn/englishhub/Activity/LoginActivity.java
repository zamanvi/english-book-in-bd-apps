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

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailET, passwordET;
    private TextView errorTV;
    private MaterialButton loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailET    = findViewById(R.id.loginEmailET);
        passwordET = findViewById(R.id.loginPasswordET);
        errorTV    = findViewById(R.id.loginErrorTV);
        loginBtn   = findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(v -> attemptLogin());

        findViewById(R.id.goToRegisterTV).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        findViewById(R.id.skipLoginTV).setOnClickListener(v -> goToMain());
    }

    private void attemptLogin() {
        String email    = emailET.getText() != null ? emailET.getText().toString().trim() : "";
        String password = passwordET.getText() != null ? passwordET.getText().toString() : "";

        if (email.isEmpty() || password.isEmpty()) {
            showError("ইমেইল এবং পাসওয়ার্ড দিন");
            return;
        }

        loginBtn.setEnabled(false);
        loginBtn.setText("লগইন হচ্ছে...");
        errorTV.setVisibility(View.GONE);

        String url = Constant.ROOT_API + "login";
        JSONObject body = new JSONObject();
        try {
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
                    String name  = data.optString("name", "");

                    UConfig uConfig = new UConfig(this);
                    uConfig.setData(Constant.TOKEN, token);
                    uConfig.setData("name", name);

                    goToMain();
                } else {
                    showError(json.optString(Constant.MESSAGE, "ইমেইল বা পাসওয়ার্ড ভুল"));
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
            loginBtn.setEnabled(true);
            loginBtn.setText("লগইন করো");
        });
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }
}
