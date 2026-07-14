package com.abmn.englishhub.Helper;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.abmn.englishhub.Activity.LoginActivity;
import com.abmn.utility.UConfig;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Shows the "create an account to earn Lipto" nudge card to logged-out
// guests reading Free content - at most once per calendar day, and only
// while the content itself isn't already gated behind a Premium-unlock card.
public class GuestNudgeHelper {

    private static String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    public static boolean shouldShow(Activity activity, String token) {
        if (token != null && !token.isEmpty()) return false;
        UConfig uConfig = new UConfig(activity);
        return !today().equals(uConfig.getData(Constant.GUEST_NUDGE_LAST_SHOWN_DATE));
    }

    public static void show(Activity activity, View cardView, View closeView, View ctaView) {
        cardView.setVisibility(View.VISIBLE);
        new UConfig(activity).setData(Constant.GUEST_NUDGE_LAST_SHOWN_DATE, today());

        closeView.setOnClickListener(v -> cardView.setVisibility(View.GONE));
        ctaView.setOnClickListener(v -> {
            cardView.setVisibility(View.GONE);
            activity.startActivity(new Intent(activity, LoginActivity.class));
        });
    }
}
