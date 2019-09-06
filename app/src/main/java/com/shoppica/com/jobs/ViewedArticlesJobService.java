package com.shoppica.com.jobs;


import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.Job;
import com.loopj.android.http.RequestParams;
import com.shoppica.com.constants.Constants;
import com.shoppica.com.preferences.Prefs;
import com.shoppica.com.server.request;

import org.json.JSONObject;

public class ViewedArticlesJobService extends Job {

    public static final String TAG = "viewedArticleJobService";

    public static final String POST_ID = "postId";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {

        try {

            String postId = params.getExtras().getString(POST_ID, null);

            RequestParams requestParams = new RequestParams();
            requestParams.add(Constants.ACTION, Constants.VIEWED_ARTICLES);
            requestParams.add(Constants.APP_KEY, Constants.APP_KEY_VALUE);
            requestParams.add(Constants.POST_IDS, postId);
            requestParams.add(Constants.FB_ACCT_ID, Prefs.getString(Constants.KEY_REGISTER_USER_ID, null));

            request.executeSync(requestParams, new request.getResponseListener() {
                @Override
                public void onSuccess(JSONObject object) {


                }

                @Override
                public void onFailure(String error) {
                    Crashlytics.log(error);
                }
            });

        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        return Result.SUCCESS;
    }


}