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
import com.shoppica.com.R;
import com.shoppica.com.activities.MainActivity;
import com.shoppica.com.db.SqlHelper;
import com.shoppica.com.objects.PostObject;
import com.shoppica.com.utils.AndroidUtilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class DailyNotificationJob extends Job {

    public static final String TAG = "dailyNotificationJob";
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

        new JobRequest.Builder(DailyNotificationJob.TAG)
                .setExecutionWindow(executionWindow.startMs, executionWindow.endMs)
                .setUpdateCurrent(updateCurrent)
                .build()
                .schedule();
    }

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        try {

            SqlHelper helper = SqlHelper.getInstance(getContext());

            List<PostObject> objectsList = helper.getPostObjects();

            if (objectsList.size() == 0)
                schedule();

            List<Integer> removePostIds = new ArrayList<>();

            for (int i = 0; i < objectsList.size(); i++) {

                PostObject postObject = objectsList.get(i);

                if (postObject == null)
                    continue;

                int postId = postObject.getPostId();
                int type = postObject.getType();
                long expireTime = postObject.getDate();
                long currentTime = AndroidUtilities.getCurrentTime();

                if (expireTime < currentTime) {
                    removePostIds.add(postId);
                    String title;
                    String body;
                    if (type == 0) {
                        title = getContext().getString(R.string.notification_title_type_0);
                        body = getContext().getString(R.string.notification_desc_type_0, postObject.getTitle());
                    } else {
                        title = getContext().getString(R.string.notification_title_type_1);
                        body = getContext().getString(R.string.notification_desc_type_1, postObject.getTitle());
                    }

                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.putExtra(MainActivity.POST_ID, postId);
                    intent.putExtra(MainActivity.POST_TYPE, type);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    showNotification(getContext(), title, body, intent,postId);
                }
            }

            for (int i = 0; i < removePostIds.size(); i++) {
                int postId = removePostIds.get(i);
                helper.deletePostObject(postId);
            }

            return Result.SUCCESS;
        }catch (Exception e){
            Crashlytics.logException(e);
        } finally {
            schedule(false);
        }
        return Result.FAILURE;
    }


    private void showNotification(Context context, String title, String body, Intent intent, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "channel-" + String.valueOf(notificationId);
        String channelName = "Channel Name-" + String.valueOf(notificationId);

        Log.d(TAG, "notificationId == " + notificationId);
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
            notificationManager.notify(notificationId, mBuilder.build());

    }

}