package com.shoppica.com.jobs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;


public class jobCreator implements JobCreator {

    @Override
    @Nullable
    public Job create(@NonNull String tag) {
        switch (tag) {
            case FavoriteJobService.TAG:
                return new FavoriteJobService();
            case LoggedInFacebookJobService.TAG:
                return new LoggedInFacebookJobService();
            case ViewedArticlesJobService.TAG:
                return new ViewedArticlesJobService();
            case PromoteArticleJobService.TAG:
                return new PromoteArticleJobService();
            case ExtendArticleJobService.TAG:
                return new ExtendArticleJobService();
            case StatusArticleJobService.TAG:
                return new StatusArticleJobService();
            case DeleteArticleJobService.TAG:
                return new DeleteArticleJobService();
            case ReportArticleJobService.TAG:
                return new ReportArticleJobService();
            case FeedBackJobService.TAG:
                return new FeedBackJobService();
            case DailyNotificationJob.TAG:
                return new DailyNotificationJob();
            case HourNotificationJob.TAG:
                return new HourNotificationJob();
            case GetMyArticlesJobService.TAG:
                return new GetMyArticlesJobService();
            case ReportTradeIssueArticleJobService.TAG:
                return new ReportTradeIssueArticleJobService();
                case DailyNewArticleJob.TAG:
                return new DailyNewArticleJob();
            default:
                return null;
        }
    }
}