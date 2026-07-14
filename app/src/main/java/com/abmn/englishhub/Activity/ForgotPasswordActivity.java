package com.abmn.englishhub.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

public class ForgotPasswordActivity extends AppCompatActivity {

    // Step 1: email input
    private View step1Layout, step2Layout, step3Layout;
    private TextInputEditText emailET;
    private MaterialButton sendOtpBtn;

    // Step 2: OTP verify
    private TextInputEditText otpET;
    private MaterialButton verifyOtpBtn;

    // Step 3: new password
    private TextInputEditText newPasswordET, confirmPasswordET;
    private MaterialButton resetBtn;

    private TextView errorTV;
    private String userEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        step1Layout   = findViewById(R.id.step1Layout);
        step2Layout   = findViewById(R.id.step2Layout);
        step3Layout   = findViewById(R.id.step3Layout);

        emailET       = findViewById(R.id.forgotEmailET);
        sendOtpBtn    = findViewById(R.id.sendOtpBtn);

        otpET         = findViewById(R.id.forgotOtpET);
        verifyOtpBtn  = findViewById(R.id.verifyOtpBtn);

        newPasswordET     = findViewById(R.id.newPasswordET);
        confirmPasswordET = findViewById(R.id.confirmNewPasswordET);
        resetBtn          = findViewById(R.id.resetPasswordBtn);

        errorTV = findViewById(R.id.forgotErrorTV);

        sendOtpBtn.setOnClickListener(v -> attemptSendOtp());
        verifyOtpBtn.setOnClickListener(v -> attemptVerifyOtp());
        resetBtn.setOnClickListener(v -> attemptReset());
        findViewById(R.id.forgotBackTV).setOnClickListener(v -> finish());
    }

    private void attemptSendOtp() {
        String email = emailET.getText() != null ? emailET.getText().toString().trim() : "";

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("সঠিক email address দাও");
            return;
        }

        userEmail = email;
        sendOtpBtn.setEnabled(false);
        sendOtpBtn.setText("পাঠানো হচ্ছে...");
        errorTV.setVisibility(View.GONE);

        String url = Constant.ROOT_API + "forget-password";
        JSONObject body = new JSONObject();
        try { body.put("email", email); } catch (Exception ignored) {}

        ApiConfig.postRequest(this, url, body, response -> {
            try {
                JSONObject json = new JSONObject(response);
                JSONObject success = json.optJSONObject(Constant.SUCCESS);
                if (success != null && success.optBoolean(Constant.STATUS, false)) {
                    runOnUiThread(() -> {
                        step1Layout.setVisibility(View.GONE);
                        step2Layout.setVisibility(View.VISIBLE);
                        errorTV.setVisibility(View.GONE);
                    });
                } else {
                    JSONObject errorObj = json.optJSONObject(Constant.ERROR);
                    showError(errorObj != null ? errorObj.optString(Constant.MESSAGE, "Email পাঠাতে ব্যর্থ হয়েছে") : "Email পাঠাতে ব্যর্থ হয়েছে");
                }
            } catch (Exception e) {
                showError("সংযোগ ব্যর্থ হয়েছে");
            }
        }, error -> showError("সংযোগ ব্যর্থ হয়েছে"));
    }

    private void attemptVerifyOtp() {
        String otp = otpET.getText() != null ? otpET.getText().toString().trim() : "";

        if (otp.isEmpty()) { showError("OTP লিখো"); return; }

        verifyOtpBtn.setEnabled(false);
        verifyOtpBtn.setText("যাচাই হচ্ছে...");
        errorTV.setVisibility(View.GONE);

        String url = Constant.ROOT_API + "verify-otp";
        JSONObject body = new JSONObject();
        try {
            body.put("email", userEmail);
            body.put("otp", otp);
        } catch (Exception ignored) {}

        ApiConfig.postRequest(this, url, body, response -> {
            try {
                JSONObject json = new JSONObject(response);
                JSONObject success = json.optJSONObject(Constant.SUCCESS);
                if (success != null && success.optBoolean(Constant.STATUS, false)) {
                    runOnUiThread(() -> {
                        step2Layout.setVisibility(View.GONE);
                        step3Layout.setVisibility(View.VISIBLE);
                        // OTP already confirmed correct here - carried forward
                        // only so confirm_password_verify() can consume it.
                        resetBtn.setTag(otp);
                        errorTV.setVisibility(View.GONE);
                        verifyOtpBtn.setEnabled(true);
                        verifyOtpBtn.setText("যাচাই করো");
                    });
                } else {
                    JSONObject errorObj = json.optJSONObject(Constant.ERROR);
                    showError(errorObj != null ? errorObj.optString(Constant.MESSAGE, "OTP ভুল বা মেয়াদ শেষ") : "OTP ভুল বা মেয়াদ শেষ");
                }
            } catch (Exception e) {
                showError("সংযোগ ব্যর্থ হয়েছে");
            }
        }, error -> showError("সংযোগ ব্যর্থ হয়েছে"));
    }

    private void attemptReset() {
        String password = newPasswordET.getText() != null ? newPasswordET.getText().toString() : "";
        String confirm  = confirmPasswordET.getText() != null ? confirmPasswordET.getText().toString() : "";
        String otp      = resetBtn.getTag() != null ? resetBtn.getTag().toString() : "";

        if (password.length() < 6) { showError("পাসওয়ার্ড কমপক্ষে ৬ অক্ষর হতে হবে"); return; }
        if (!password.equals(confirm)) { showError("পাসওয়ার্ড দুটো মিলছে না"); return; }

        resetBtn.setEnabled(false);
        resetBtn.setText("পরিবর্তন হচ্ছে...");
        errorTV.setVisibility(View.GONE);

        String url = Constant.ROOT_API + "confirm-password";
        JSONObject body = new JSONObject();
        try {
            body.put("email", userEmail);
            body.put("otp", otp);
            body.put("password", password);
        } catch (Exception ignored) {}

        ApiConfig.postRequest(this, url, body, response -> {
            try {
                JSONObject json = new JSONObject(response);
                JSONObject success = json.optJSONObject(Constant.SUCCESS);
                if (success != null && success.optBoolean(Constant.STATUS, false)) {
                    runOnUiThread(() -> {
                        errorTV.setTextColor(getResources().getColor(R.color.green, null));
                        errorTV.setText("পাসওয়ার্ড পরিবর্তন হয়েছে! এখন লগইন করো।");
                        errorTV.setVisibility(View.VISIBLE);
                        resetBtn.setEnabled(false);
                        resetBtn.postDelayed(this::finish, 2000);
                    });
                } else {
                    JSONObject errorObj = json.optJSONObject(Constant.ERROR);
                    showError(errorObj != null ? errorObj.optString(Constant.MESSAGE, "OTP ভুল বা মেয়াদ শেষ") : "OTP ভুল বা মেয়াদ শেষ");
                }
            } catch (Exception e) {
                showError("সংযোগ ব্যর্থ হয়েছে");
            }
        }, error -> showError("সংযোগ ব্যর্থ হয়েছে"));
    }

    private void showError(String msg) {
        runOnUiThread(() -> {
            errorTV.setTextColor(getResources().getColor(R.color.coral, null));
            errorTV.setText(msg);
            errorTV.setVisibility(View.VISIBLE);
            sendOtpBtn.setEnabled(true);
            sendOtpBtn.setText("OTP পাঠাও");
            verifyOtpBtn.setEnabled(true);
            verifyOtpBtn.setText("যাচাই করো");
            resetBtn.setEnabled(true);
            resetBtn.setText("পাসওয়ার্ড পরিবর্তন করো");
        });
    }
}
