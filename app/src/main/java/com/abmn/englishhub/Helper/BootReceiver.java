package com.abmn.englishhub.Helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Reschedules the daily streak reminder after device reboot.
 * AlarmManager alarms are cleared on reboot — this restores them.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            StreakReminderReceiver.schedule(context);
        }
    }
}
