package com.shoppica.com.jobs;

import android.os.Handler;

import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.shoppica.com.utils.NetworkUtils;

import java.util.concurrent.TimeUnit;

/**
 * Created by Carl on 2017-11-27.
 */

public class JobLauncher {

    public static void scheduleFavoriteJob(int postId, String action) {
        NetworkUtils.isNetworkAvailableExtra(mIncomingHandler, action, postId, 3000);
    }


    private static Handler mIncomingHandler = new Handler(msg -> {

        PersistableBundleCompat bundle = new PersistableBundleCompat();
        bundle.putString(FavoriteJobService.ACTION, (String) msg.obj);
        bundle.putInt(FavoriteJobService.POST_ID, msg.arg1);

        if (msg.what == 1) {

            new JobRequest.Builder(FavoriteJobService.TAG)
                    .setUpdateCurrent(false)
                    .setExtras(bundle)
                    .startNow()
                    .build()
                    .schedule();
        } else {
            new JobRequest.Builder(FavoriteJobService.TAG)
                    .setExecutionWindow(TimeUnit.HOURS.toMillis(2), TimeUnit.HOURS.toMillis(4))
                    .setBackoffCriteria(5_000L, JobRequest.BackoffPolicy.EXPONENTIAL)
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .setRequirementsEnforced(true)
                    .setUpdateCurrent(false)
                    .setExtras(bundle)
                    .build()
                    .schedule();

        }

        return true;
    });


    public static void scheduleFacebookLogin() {
        NetworkUtils.isNetworkAvailable(mIncomingLoggedInFacebook, 3000);
    }


    private static Handler mIncomingLoggedInFacebook = new Handler(msg -> {


        if (msg.what == 1) {

            new JobRequest.Builder(LoggedInFacebookJobService.TAG)
                    .setUpdateCurrent(false)
                    .startNow()
                    .build()
                    .schedule();
        } else {
            new JobRequest.Builder(LoggedInFacebookJobService.TAG)
                    .setExecutionWindow(TimeUnit.HOURS.toMillis(2), TimeUnit.HOURS.toMillis(4))
                    .setBackoffCriteria(5_000L, JobRequest.BackoffPolicy.EXPONENTIAL)
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .setRequirementsEnforced(true)
                    .setUpdateCurrent(false)
                    .build()
                    .schedule();

        }

        return true;
    });


    public static void scheduleViewedArticleService(String postId) {

        PersistableBundleCompat bundle = new PersistableBundleCompat();
        bundle.putString(ViewedArticlesJobService.POST_ID,postId);
        new JobRequest.Builder(ViewedArticlesJobService.TAG)
                .setUpdateCurrent(true)
                .setExtras(bundle)
                .startNow()
                .build()
                .schedule();

    }

    public static void schedulePromoteArticle(int postId, int promoted) {

        NetworkUtils.isNetworkAvailableExtra(mIncomingPromoteArticles, postId, promoted, 3000);
    }


    private static Handler mIncomingPromoteArticles = new Handler(msg -> {

        PersistableBundleCompat bundle = new PersistableBundleCompat();
        bundle.putInt(PromoteArticleJobService.POST_ID, msg.arg1);
        bundle.putInt(PromoteArticleJobService.POST_PROMOTED, msg.arg2);

        if (msg.what == 1) {

            new JobRequest.Builder(PromoteArticleJobService.TAG)
                    .setUpdateCurrent(false)
                    .setExtras(bundle)
                    .startNow()
                    .build()
                    .schedule();
        } else {
            new JobRequest.Builder(PromoteArticleJobService.TAG)
                    .setExecutionWindow(TimeUnit.HOURS.toMillis(2), TimeUnit.HOURS.toMillis(4))
                    .setBackoffCriteria(5_000L, JobRequest.BackoffPolicy.EXPONENTIAL)
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .setExtras(bundle)
                    .setRequirementsEnforced(true)
                    .setUpdateCurrent(false)
                    .build()
                    .schedule();

        }

        return true;
    });


    public static void scheduleExtendArticle(int postId, int extended) {

        NetworkUtils.isNetworkAvailableExtra(mIncomingExtendArticles, postId, extended, 3000);
    }


    private static Handler mIncomingExtendArticles = new Handler(msg -> {

        PersistableBundleCompat bundle = new PersistableBundleCompat();
        bundle.putInt(ExtendArticleJobService.POST_ID, msg.arg1);
        bundle.putInt(ExtendArticleJobService.POST_EXTEND, msg.arg2);

        if (msg.what == 1) {

            new JobRequest.Builder(ExtendArticleJobService.TAG)
                    .setUpdateCurrent(false)
                    .setExtras(bundle)
                    .startNow()
                    .build()
                    .schedule();
        } else {
            new JobRequest.Builder(ExtendArticleJobService.TAG)
                    .setExecutionWindow(TimeUnit.HOURS.toMillis(2), TimeUnit.HOURS.toMillis(4))
                    .setBackoffCriteria(5_000L, JobRequest.BackoffPolicy.EXPONENTIAL)
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .setExtras(bundle)
                    .setRequirementsEnforced(true)
                    .setUpdateCurrent(false)
                    .build()
                    .schedule();

        }

        return true;
    });


    public static void scheduleStatusArticle(int postId, int status) {

        NetworkUtils.isNetworkAvailableExtra(mIncomingStatusArticles, postId, status, 3000);
    }


    private static Handler mIncomingStatusArticles = new Handler(msg -> {

        PersistableBundleCompat bundle = new PersistableBundleCompat();
        bundle.putInt(StatusArticleJobService.POST_ID, msg.arg1);
        bundle.putInt(StatusArticleJobService.POST_STATUS, msg.arg2);

        if (msg.what == 1) {

            new JobRequest.Builder(StatusArticleJobService.TAG)
                    .setUpdateCurrent(false)
                    .setExtras(bundle)
                    .startNow()
                    .build()
                    .schedule();
        } else {
            new JobRequest.Builder(StatusArticleJobService.TAG)
                    .setExecutionWindow(TimeUnit.HOURS.toMillis(2), TimeUnit.HOURS.toMillis(4))
                    .setBackoffCriteria(5_000L, JobRequest.BackoffPolicy.EXPONENTIAL)
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .setExtras(bundle)
                    .setRequirementsEnforced(true)
                    .setUpdateCurrent(false)
                    .build()
                    .schedule();

        }

        return true;
    });


    public static void scheduleDeleteArticle(int postId) {

        NetworkUtils.isNetworkAvailableExtra(mIncomingDeleteArticle, postId, 3000);
    }


    private static Handler mIncomingDeleteArticle = new Handler(msg -> {

        PersistableBundleCompat bundle = new PersistableBundleCompat();
        bundle.putInt(StatusArticleJobService.POST_ID, (Integer) msg.obj);

        if (msg.what == 1) {

            new JobRequest.Builder(DeleteArticleJobService.TAG)
                    .setUpdateCurrent(false)
                    .setExtras(bundle)
                    .startNow()
                    .build()
                    .schedule();
        } else {
            new JobRequest.Builder(DeleteArticleJobService.TAG)
                    .setExecutionWindow(TimeUnit.HOURS.toMillis(2), TimeUnit.HOURS.toMillis(4))
                    .setBackoffCriteria(5_000L, JobRequest.BackoffPolicy.EXPONENTIAL)
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .setExtras(bundle)
                    .setRequirementsEnforced(true)
                    .setUpdateCurrent(false)
                    .build()
                    .schedule();

        }

        return true;
    });

    public static void scheduleReportUser(int postId, int type, String message) {
        NetworkUtils.isNetworkAvailableExtra(mIncomingReportUser, postId, type, message, 3000);
    }


    private static Handler mIncomingReportUser = new Handler(msg -> {
        PersistableBundleCompat bundle = new PersistableBundleCompat();
        bundle.putInt(ReportArticleJobService.POST_ID, msg.arg1);
        bundle.putInt(ReportArticleJobService.REPORT_TYPE, msg.arg2);
        bundle.putString(ReportArticleJobService.REPORT_MSG, (String) msg.obj);


        if (msg.what == 1) {
            new JobRequest.Builder(ReportArticleJobService.TAG)
                    .setUpdateCurrent(false)
                    .setExtras(bundle)
                    .startNow()
                    .build()
                    .schedule();
        } else {
            new JobRequest.Builder(ReportArticleJobService.TAG)
                    .setExecutionWindow(TimeUnit.HOURS.toMillis(2), TimeUnit.HOURS.toMillis(4))
                    .setBackoffCriteria(5_000L, JobRequest.BackoffPolicy.EXPONENTIAL)
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .setRequirementsEnforced(true)
                    .setExtras(bundle)
                    .setUpdateCurrent(false)
                    .build()
                    .schedule();

        }

        return true;
    });

    public static void scheduleFeedBack(String message) {
        NetworkUtils.isNetworkAvailableExtra(mIncomingFeedBack, message, 3000);
    }


    private static Handler mIncomingFeedBack = new Handler(msg -> {
        PersistableBundleCompat bundle = new PersistableBundleCompat();
        bundle.putString(FeedBackJobService.FEED_BACK_MSG, (String) msg.obj);


        if (msg.what == 1) {
            new JobRequest.Builder(FeedBackJobService.TAG)
                    .setUpdateCurrent(false)
                    .setExtras(bundle)
                    .startNow()
                    .build()
                    .schedule();
        } else {
            new JobRequest.Builder(FeedBackJobService.TAG)
                    .setExecutionWindow(TimeUnit.HOURS.toMillis(2), TimeUnit.HOURS.toMillis(4))
                    .setBackoffCriteria(5_000L, JobRequest.BackoffPolicy.EXPONENTIAL)
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .setRequirementsEnforced(true)
                    .setExtras(bundle)
                    .setUpdateCurrent(false)
                    .build()
                    .schedule();

        }

        return true;
    });


    public static void schedulePromotionJob(int hourStart, int hourEnd){
        new JobRequest.Builder(HourNotificationJob.TAG)
                .setExecutionWindow(TimeUnit.HOURS.toMillis(hourStart), TimeUnit.HOURS.toMillis(hourEnd))
                .setUpdateCurrent(false)
                .build()
                .schedule();
    }

    public static void scheduleTradeIssue(String message) {
        NetworkUtils.isNetworkAvailableExtra(mIncomingTradeIssue, message, 3000);
    }


    private static Handler mIncomingTradeIssue = new Handler(msg -> {
        PersistableBundleCompat bundle = new PersistableBundleCompat();
        bundle.putString(ReportTradeIssueArticleJobService.REPORT_MSG, (String) msg.obj);


        if (msg.what == 1) {
            new JobRequest.Builder(ReportTradeIssueArticleJobService.TAG)
                    .setUpdateCurrent(false)
                    .setExtras(bundle)
                    .startNow()
                    .build()
                    .schedule();
        } else {
            new JobRequest.Builder(ReportTradeIssueArticleJobService.TAG)
                    .setExecutionWindow(TimeUnit.HOURS.toMillis(2), TimeUnit.HOURS.toMillis(4))
                    .setBackoffCriteria(5_000L, JobRequest.BackoffPolicy.EXPONENTIAL)
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .setRequirementsEnforced(true)
                    .setExtras(bundle)
                    .setUpdateCurrent(false)
                    .build()
                    .schedule();

        }

        return true;
    });


}
