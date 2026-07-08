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

    private TextInputEditText nameET, emailET, passwordET, confirmPasswordET;
    private TextView errorTV;
    private MaterialButton registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameET              = findViewById(R.id.registerNameET);
        emailET             = findViewById(R.id.registerEmailET);
        passwordET          = findViewById(R.id.registerPasswordET);
        confirmPasswordET   = findViewById(R.id.registerConfirmPasswordET);
        errorTV             = findViewById(R.id.registerErrorTV);
        registerBtn         = findViewById(R.id.registerBtn);

        registerBtn.setOnClickListener(v -> attemptRegister());
        findViewById(R.id.goToLoginTV).setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name            = nameET.getText()            != null ? nameET.getText().toString().trim()  : "";
        String email           = emailET.getText()           != null ? emailET.getText().toString().trim()  : "";
        String password        = passwordET.getText()        != null ? passwordET.getText().toString()     : "";
        String confirmPassword = confirmPasswordET.getText() != null ? confirmPasswordET.getText().toString() : "";

        if (name.isEmpty()) {
            showError("তোমার নাম লিখো");
            return;
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("সঠিক email address দাও");
            return;
        }
        if (password.length() < 6) {
            showError("পাসওয়ার্ড কমপক্ষে ৬ অক্ষর হতে হবে");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("পাসওয়ার্ড দুটো মিলছে না");
            return;
        }

        registerBtn.setEnabled(false);
        registerBtn.setText("তৈরি হচ্ছে...");
        errorTV.setVisibility(View.GONE);

        String url = Constant.ROOT_API + "register";
        JSONObject body = new JSONObject();
        try {
            body.put("name", name);
            body.put("email", email);
            body.put("password", password);
            body.put("password_confirmation", confirmPassword);
        } catch (Exception ignored) {}

        ApiConfig.postRequest(this, url, body, response -> {
            try {
                JSONObject json = new JSONObject(response);
                JSONObject success = json.optJSONObject(Constant.SUCCESS);
                if (success != null && success.optBoolean(Constant.STATUS, false)) {
                    JSONObject data = success.optJSONObject(Constant.DATA);
                    if (data == null) { showError("Server error"); return; }

                    String token = data.optString("token", "");
                    UConfig uConfig = new UConfig(this);
                    uConfig.setData(Constant.TOKEN, token);
                    uConfig.setData("name", name);

                    startActivity(new Intent(this, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                    finish();
                } else {
                    JSONObject errorObj = json.optJSONObject(Constant.ERROR);
                    String msg = errorObj != null ? errorObj.optString(Constant.MESSAGE, "একাউন্ট তৈরি ব্যর্থ হয়েছে") : "একাউন্ট তৈরি ব্যর্থ হয়েছে";
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
            registerBtn.setEnabled(true);
            registerBtn.setText("একাউন্ট তৈরি করো");
        });
    }
}
