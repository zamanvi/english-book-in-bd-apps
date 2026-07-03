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
        UConfig uConfig = new UConfig(activity);
        if (!uConfig.isConnected()){
            uConfig.isConnectedAlert("", "");
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
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(8000, 0, 0));
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
        rawRequest(context, Request.Method.GET, url, null, onSuccess, onError);
    }

    public static void postRequest(Context context, String url, JSONObject body,
                                   SimpleCallback onSuccess, ErrorCallback onError) {
        rawRequest(context, Request.Method.POST, url, body, onSuccess, onError);
    }

    private static void rawRequest(Context context, int method, String url,
                                   JSONObject body,
                                   SimpleCallback onSuccess, ErrorCallback onError) {
        RequestQueue queue = getQueue(context);
        String finalUrl = url.contains("?")
                ? url + "&" + Constant.PUBLIC_KEY_VALUE
                : url + "?" + Constant.PUBLIC_KEY_VALUE;

        final String bodyStr = body != null ? body.toString() : null;

        StringRequest req = new StringRequest(method, finalUrl,
                response -> { if (onSuccess != null) onSuccess.onSuccess(response); },
                error -> { if (onError != null) onError.onError(error); }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("Accept", "application/json");
                h.put("Content-Type", "application/json");
                h.put("x-api-key", "app");
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
}
