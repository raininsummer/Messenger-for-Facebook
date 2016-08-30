package me.rain.liteforfacebook;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PollReceiver extends BroadcastReceiver {
    static void scheduleAlarms(Context ctxt, boolean cancel) {
        Intent startIntent = new Intent(ctxt, NotificationService.class);
        // Start the alarm
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctxt);
        if (preferences.getBoolean(SettingsActivity.KEY_PREF_NOTIFICATIONS_ENABLED, false) && !cancel) {
            ctxt.startService(startIntent);
        }
    }

    @Override
    public void onReceive(Context ctxt, Intent i) {
        scheduleAlarms(ctxt, false);
    }
}