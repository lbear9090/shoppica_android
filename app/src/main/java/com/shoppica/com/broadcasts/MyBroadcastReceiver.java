package com.shoppica.com.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.shoppica.com.jobs.DailyNewArticleJob;
import com.shoppica.com.jobs.DailyNotificationJob;
import com.shoppica.com.preferences.Prefs;

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if(Prefs.getBoolean(com.shoppica.com.constants.Constants.KEY_NOTIFY_USER, true))
            DailyNewArticleJob.schedule();

        DailyNotificationJob.schedule();

    }
}