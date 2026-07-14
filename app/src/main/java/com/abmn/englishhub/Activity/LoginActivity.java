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

    private TextInputEditText nameET, passwordET;
    private TextView errorTV;
    private MaterialButton loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        nameET     = findViewById(R.id.loginNameET);
        passwordET = findViewById(R.id.loginPasswordET);
        errorTV    = findViewById(R.id.loginErrorTV);
        loginBtn   = findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(v -> attemptLogin());

        findViewById(R.id.goToRegisterTV).setOnClickListener(v ->
            startActivity(new Intent(this, RegisterActivity.class)));

        findViewById(R.id.forgotPasswordTV).setOnClickListener(v ->
            startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void attemptLogin() {
        String name     = nameET.getText() != null ? nameET.getText().toString().trim() : "";
        String password = passwordET.getText() != null ? passwordET.getText().toString() : "";

        if (name.isEmpty() || password.isEmpty()) {
            showError("নাম/Email এবং পাসওয়ার্ড দিন");
            return;
        }

        loginBtn.setEnabled(false);
        loginBtn.setText("লগইন হচ্ছে...");
        errorTV.setVisibility(View.GONE);

        String url = Constant.ROOT_API + "login";
        JSONObject body = new JSONObject();
        try {
            body.put("name", name);
            body.put("password", password);
        } catch (Exception ignored) {}

        ApiConfig.postRequest(this, url, body, response -> {
            try {
                JSONObject json = new JSONObject(response);
                JSONObject success = json.optJSONObject(Constant.SUCCESS);
                if (success != null && success.optBoolean(Constant.STATUS, false)) {
                    JSONObject data = success.optJSONObject(Constant.DATA);
                    if (data == null) { showError("Server error"); return; }

                    // Backend wraps user fields inside data.user
                    JSONObject userObj = data.optJSONObject("user");
                    String token = data.optString("token", "");
                    String userName = userObj != null ? userObj.optString("name", name) : name;
                    int userId = userObj != null ? userObj.optInt("id", 0) : 0;

                    UConfig uConfig = new UConfig(this);
                    uConfig.setData(Constant.TOKEN, token);
                    uConfig.setData("name", userName);

                    if (userId > 0) {
                        getSharedPreferences("app_prefs", MODE_PRIVATE)
                                .edit().putInt("user_id", userId).apply();
                    }

                    com.abmn.englishhub.Helper.FcmService.saveTokenToBackend(this);

                    goToMain();
                } else {
                    JSONObject errorObj = json.optJSONObject(Constant.ERROR);
                    String msg = errorObj != null ? errorObj.optString(Constant.MESSAGE, "নাম বা পাসওয়ার্ড ভুল") : "নাম বা পাসওয়ার্ড ভুল";
                    showError(msg);
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
