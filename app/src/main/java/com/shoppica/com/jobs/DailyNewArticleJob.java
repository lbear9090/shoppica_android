package com.shoppica.com.jobs;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.loopj.android.http.RequestParams;
import com.shoppica.com.R;
import com.shoppica.com.activities.MainActivity;
import com.shoppica.com.constants.Constants;
import com.shoppica.com.preferences.Prefs;
import com.shoppica.com.server.request;
import com.shoppica.com.utils.AndroidUtilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Objects;

public class DailyNewArticleJob extends Job {

    public static final String TAG = "dailyNewArticleJob";
    private static final long TARGET_HOUR = 2L;
    private static final long TARGET_MINUTE = 15;
    private static final long WINDOW_LENGTH = 60;
    private static final int WAKE_LOCK_AWAIT_TIME_SECONDS = 60;

    // called in <MyApplication extends Application>.onCreate()
    public static void schedule() {
        schedule(true);
    }

    private static void schedule(boolean updateCurrent) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        com.shoppica.com.view.DailyExecutionWindow executionWindow =
                new com.shoppica.com.view.DailyExecutionWindow(hour, minute, TARGET_HOUR, TARGET_MINUTE, WINDOW_LENGTH);

        new JobRequest.Builder(DailyNewArticleJob.TAG)
                .setExecutionWindow(executionWindow.startMs, executionWindow.endMs)
                .setUpdateCurrent(updateCurrent)
                .build()
                .schedule();

    }

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        try {


            if(!Prefs.getBoolean(Constants.KEY_NOTIFY_USER, true)){
                return Result.SUCCESS;
            }

            int time = Prefs.getInt(com.shoppica.com.constants.Constants.KEY_TIME_LEFT_APP, 0);

            if (time == 0)
                return Result.SUCCESS;

            RequestParams requestParams = new RequestParams();
            requestParams.add(Constants.ACTION, Constants.CHECK_FOR_NEW_ATICLES);
            requestParams.add(Constants.APP_KEY, Constants.APP_KEY_VALUE);
            requestParams.add(Constants.TIME, String.valueOf(time));

            request.executeSync(requestParams, new request.getResponseListener() {
                @Override
                public void onSuccess(JSONObject object) {

                    try {
                        int success = object.getInt(Constants.SUCCESS);

                        if (success == 1) {

                            int total = object.getInt(Constants.TOTAL);

                            if(total > 0){

                                String quantityString = getContext().getResources().getQuantityString(R.plurals.new_articles, Math.round(total)
                                        , Math.round(total));

                                Intent intent = new Intent(getContext(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                                showNotification(getContext(), getContext().getString(R.string.notification_new_article_title), quantityString, intent);

                            }


                        }

                    } catch (JSONException e) {
                        Crashlytics.logException(e);
                    }

                }

                @Override
                public void onFailure(String error) {

                }
            });

            return Result.SUCCESS;
        } catch (Exception e) {
            Crashlytics.logException(e);
        } finally {
            schedule(false);
        }
        return Result.FAILURE;

    }


    private void showNotification(Context context, String title, String body, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int m = (int) (AndroidUtilities.getCurrentTime() / 1000L) % Integer.MAX_VALUE;

        String channelId = "channel-" + String.valueOf(m);
        String channelName = "Channel Name-" + String.valueOf(m);

        Log.d(TAG, "notificationId == " + m);
        Log.d(TAG, "channelId == " + channelId);
        Log.d(TAG, "channelName == " + channelName);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            Objects.requireNonNull(notificationManager).createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_small_notification)
                .setContentTitle(title)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body))
                .setContentText(body);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 200,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setAutoCancel(true);
        mBuilder.build().flags |= Notification.FLAG_AUTO_CANCEL;
        mBuilder.setContentIntent(pendingIntent);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);
        long[] DEFAULT_VIBRATE_PATTERN = {0, 250, 250, 250};

        mBuilder.setVibrate(DEFAULT_VIBRATE_PATTERN);


        if (notificationManager != null)
            notificationManager.notify(m, mBuilder.build());

    }

}