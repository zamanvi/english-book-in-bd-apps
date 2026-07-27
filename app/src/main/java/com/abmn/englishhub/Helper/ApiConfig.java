package com.abmn.englishhub.Helper;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;

import com.abmn.utility.UConfig;
import com.abmn.utility.UI.ProgressDisplay;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ApiConfig {

    // Singleton queue — avoids creating a new RequestQueue on every API call
    private static RequestQueue requestQueue;

    private static RequestQueue getQueue(Context context) {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public static void RequestToVolley(VolleyCallback result, int method, Activity activity, String url, Map<String, String> params, boolean isProgress) {
        RequestToVolley(result, method, activity, url, params, isProgress, null);
    }

    // cacheKey non-null opts a call into offline caching: served from disk when
    // there's no connection or the request fails, and refreshed on every
    // successful response. Existing callers are unaffected (see overload above).
    public static void RequestToVolley(VolleyCallback result, int method, Activity activity, String url, Map<String, String> params, boolean isProgress, String cacheKey) {
        UConfig uConfig = new UConfig(activity);
        if (!uConfig.isConnected()){
            if (cacheKey != null) {
                String cached = OfflineCache.load(activity, cacheKey);
                if (cached != null) {
                    result.onResponse(true, cached, "");
                    return;
                }
            }
            uConfig.isConnectedAlert("ইন্টারনেট সংযোগ নেই", "এই ফিচারটি ব্যবহার করতে ইন্টারনেট সংযোগ প্রয়োজন");
        }
        ProgressDisplay progressDisplay = new ProgressDisplay(activity);
        if (isProgress)
            progressDisplay.showProgress();
        else
            progressDisplay.hideProgress();

        RequestQueue queue = getQueue(activity);
        String m_url = url;
        if (m_url.contains("?")){
            m_url = m_url + "&" + Constant.PUBLIC_KEY_VALUE;
        }else {
            m_url = m_url + "?" + Constant.PUBLIC_KEY_VALUE;
        }

        String finalUrl = m_url;
        StringRequest stringRequest = new StringRequest(method, finalUrl,
                response -> {
                    progressDisplay.hideProgress();
                    try {
                        JSONObject success = new JSONObject(response).getJSONObject(Constant.SUCCESS);
                        if (success.getBoolean(Constant.STATUS)){
                            JSONObject data = success.getJSONObject(Constant.DATA);
                            if (cacheKey != null) {
                                OfflineCache.save(activity, cacheKey, String.valueOf(data));
                            }
                            result.onResponse(true, String.valueOf(data), "");
                        }else {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.has(Constant.ERROR)){
                                JSONObject error = jsonObject.getJSONObject(Constant.ERROR);
                                String message = error.getString(Constant.MESSAGE);
                                int code = error.getInt(Constant.CODE);
                                result.onResponse(false, "", message);
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("ApiConfig", "response parse error", e);
                        result.onResponse(false, "", "parse_error");
                    }
                },
                error -> {
                    progressDisplay.hideProgress();
                    if (cacheKey != null) {
                        String cached = OfflineCache.load(activity, cacheKey);
                        if (cached != null) {
                            result.onResponse(true, cached, "");
                            return;
                        }
                    }
                    NetworkResponse networkResponse = error.networkResponse;
                    if (networkResponse != null && networkResponse.data != null) {
                        String errorResponse = new String(networkResponse.data);
                        result.onResponse(false, errorResponse, "");
                    } else {
                        // Network timeout / no connection — must still fire callback
                        result.onResponse(false, "", "");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params1 = new HashMap<>();
                params1.put(Constant.ACCEPT, Constant.APPLICATION_JSON);
                params1.put("x-api-key", "app");
                params1.put("Content-Type", Constant.APPLICATION_JSON);
                params1.put(Constant.AUTHORIZATION, Constant.BEARER + uConfig.getData(Constant.TOKEN));
                return params1;
            }
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                return params;
            }

            @Override
            public Response<String> parseNetworkResponse(NetworkResponse response) {
                return super.parseNetworkResponse(response);
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(8000, 1, 1.0f));
        queue.add(stringRequest);
    }

    // ── Simple GET — for game public endpoints (x-api-key: app) ──

    public interface SimpleCallback {
        void onSuccess(String response);
    }

    public interface ErrorCallback {
        void onError(Exception error);
    }

    public static void getRequest(Context context, String url,
                                  SimpleCallback onSuccess, ErrorCallback onError) {
        rawRequest(context, Request.Method.GET, url, null, null, onSuccess, onError);
    }

    public static void getRequest(Context context, String url, String token,
                                  SimpleCallback onSuccess, ErrorCallback onError) {
        rawRequest(context, Request.Method.GET, url, null, token, onSuccess, onError);
    }

    public static void postRequest(Context context, String url, JSONObject body,
                                   SimpleCallback onSuccess, ErrorCallback onError) {
        rawRequest(context, Request.Method.POST, url, body, null, onSuccess, onError);
    }

    public static void postRequest(Context context, String url, JSONObject body, String token,
                                   SimpleCallback onSuccess, ErrorCallback onError) {
        rawRequest(context, Request.Method.POST, url, body, token, onSuccess, onError);
    }

    private static void rawRequest(Context context, int method, String url,
                                   JSONObject body, String authToken,
                                   SimpleCallback onSuccess, ErrorCallback onError) {
        RequestQueue queue = getQueue(context);
        String finalUrl = url.contains("?")
                ? url + "&" + Constant.PUBLIC_KEY_VALUE
                : url + "?" + Constant.PUBLIC_KEY_VALUE;

        final String bodyStr = body != null ? body.toString() : null;
        final String token   = authToken;

        StringRequest req = new StringRequest(method, finalUrl,
                response -> { if (onSuccess != null) onSuccess.onSuccess(response); },
                error -> {
                    if (onError == null) return;
                    // Volley routes any non-2xx response (validation errors, 409 conflicts,
                    // etc.) here instead of onSuccess, even though the backend usually sends
                    // a well-formed JSON body with a real message. Surface that message
                    // instead of a bare VolleyError whose getMessage() is null.
                    String message = null;
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            JSONObject errBody = new JSONObject(new String(error.networkResponse.data));
                            if (errBody.has("message")) {
                                message = errBody.getString("message");
                            } else if (errBody.has(Constant.ERROR)) {
                                message = errBody.getJSONObject(Constant.ERROR).optString(Constant.MESSAGE, null);
                            }
                        } catch (Exception ignored) {}
                    }
                    onError.onError(message != null ? new Exception(message, error) : error);
                }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("Accept", "application/json");
                h.put("Content-Type", "application/json");
                h.put("x-api-key", "app");
                if (token != null && !token.isEmpty()) {
                    h.put(Constant.AUTHORIZATION, Constant.BEARER + token);
                }
                return h;
            }

            @Override
            public byte[] getBody() {
                return bodyStr != null ? bodyStr.getBytes() : null;
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(15000, 1, 1.0f));
        queue.add(req);
    }

    // ── Multipart file upload — Volley has no built-in multipart support,
    // so this one goes straight over HttpURLConnection on a background thread. ──
    public static void uploadFile(String url, String token, java.io.File file, String fieldName,
                                   SimpleCallback onSuccess, ErrorCallback onError) {
        new Thread(() -> {
            String boundary = "Boundary-" + System.currentTimeMillis();
            java.net.HttpURLConnection conn = null;
            try {
                conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(20000);
                conn.setReadTimeout(20000);
                conn.setRequestProperty("x-api-key", "app");
                conn.setRequestProperty(Constant.AUTHORIZATION, Constant.BEARER + token);
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                try (java.io.DataOutputStream out = new java.io.DataOutputStream(conn.getOutputStream())) {
                    out.writeBytes("--" + boundary + "\r\n");
                    out.writeBytes("Content-Disposition: form-data; name=\"" + fieldName
                            + "\"; filename=\"" + file.getName() + "\"\r\n");
                    out.writeBytes("Content-Type: image/jpeg\r\n\r\n");
                    try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = fis.read(buffer)) != -1) out.write(buffer, 0, len);
                    }
                    out.writeBytes("\r\n--" + boundary + "--\r\n");
                }

                int code = conn.getResponseCode();
                java.io.InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
                String response = readFully(is);

                postToMain(() -> {
                    if (code >= 200 && code < 300) {
                        if (onSuccess != null) onSuccess.onSuccess(response);
                    } else if (onError != null) {
                        onError.onError(new Exception(response));
                    }
                });
            } catch (Exception e) {
                postToMain(() -> { if (onError != null) onError.onError(e); });
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private static String readFully(java.io.InputStream is) throws java.io.IOException {
        if (is == null) return "";
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = is.read(buffer)) != -1) bos.write(buffer, 0, len);
        return bos.toString("UTF-8");
    }

    private static void postToMain(Runnable r) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(r);
    }
}
