package com.abmn.englishhub.Helper;
import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class InterstitialAdManager {

    private final Activity activity;
    private final String from;
    private InterstitialAd interstitialAd;
    private boolean isAdLoading = false;

    public InterstitialAdManager(Activity activity, String from) {
        this.activity = activity;
        this.from = from;
    }

    public void loadInterstitialAd() {
        if (isAdLoading || interstitialAd != null) return;

        isAdLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(activity, from, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd ad) {
                interstitialAd = ad;
                isAdLoading = false;

                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        interstitialAd = null;
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        interstitialAd = null;
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        interstitialAd = null;
                    }
                });
                show();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.d("InterstitialAd", "Failed to load: " + loadAdError.getMessage());
                isAdLoading = false;
            }
        });
    }

    private void show() {
        if (interstitialAd != null) {
            interstitialAd.show(activity);
        } else {
            loadInterstitialAd(); // Load if not ready
        }
    }
}