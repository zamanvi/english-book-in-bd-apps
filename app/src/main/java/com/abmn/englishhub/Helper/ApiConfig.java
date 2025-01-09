package com.abmn.englishhub.Helper;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.abmn.utility.UConfig;
import com.abmn.utility.UI.ProgressDisplay;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ApiConfig {

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

        RequestQueue queue = Volley.newRequestQueue(activity);
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
                            Log.d("abmn_message-success", success.getString(Constant.MESSAGE));
                            JSONObject data = success.getJSONObject(Constant.DATA);
                            result.onResponse(true, String.valueOf(data), "");
                        }else {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.has(Constant.ERROR)){
                                JSONObject error = jsonObject.getJSONObject(Constant.ERROR);
                                String message = error.getString(Constant.MESSAGE);
                                int code = error.getInt(Constant.CODE);
                                Log.d("abmn_message-error", message + ", code: " + code);
                                result.onResponse(false, "", message);
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    progressDisplay.hideProgress();
                    NetworkResponse networkResponse = error.networkResponse;
                    if (networkResponse != null && networkResponse.data != null) {
                        String errorResponse = new String(networkResponse.data);
                        result.onResponse(false, errorResponse, "");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params1 = new HashMap<>();
                params1.put(Constant.ACCEPT, Constant.APPLICATION_JSON);
                params1.put(Constant.AUTHORIZATION, Constant.BEARER + uConfig.getData(Constant.TOKEN));
//                Log.d("srHeaders", params1 + ", " + params + ", " + finalUrl);
                return params1;
            }
            @NonNull
            @Override
            protected Map<String, String> getParams() {
//                Log.d("srParams", params + ", " + finalUrl + ", " + getHeaders());
                return params;
            }

            @Override
            public Response<String> parseNetworkResponse(NetworkResponse response) {
                return super.parseNetworkResponse(response);
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, 0, 0));
        queue.add(stringRequest);
    }
}
