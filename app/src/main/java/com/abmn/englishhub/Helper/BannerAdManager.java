package com.abmn.englishhub.Helper;

import android.app.Activity;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class BannerAdManager {
    private static final String TAG = "BannerAdManager";

    public static void loadBannerAd(Activity activity, FrameLayout adContainer, String adUnitId) {
        if (adContainer == null || adUnitId == null || adUnitId.isEmpty()) {
            Log.w(TAG, "Invalid container or ad unit ID");
            return;
        }

        AdView adView = new AdView(activity);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(adUnitId);

        adContainer.removeAllViews();
        adContainer.addView(adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.d(TAG, "Banner ad loaded.");
            }

            @Override
            public void onAdFailedToLoad(@NonNull com.google.android.gms.ads.LoadAdError adError) {
                Log.e(TAG, "Banner ad failed: " + adError.getMessage());
            }
        });
    }
}
