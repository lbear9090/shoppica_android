package com.shoppica.com.jobs;


import android.support.annotation.NonNull;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.Job;
import com.shoppica.com.constants.Constants;
import com.shoppica.com.preferences.Prefs;
import com.shoppica.com.server.request;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

public class StatusArticleJobService extends Job {

    public static final String TAG = "statusArticleJobService";

    public static final String POST_STATUS = "postStatus";
    public static final String POST_ID = "postId";


    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {

        try {

            int mPostStatus = params.getExtras().getInt(POST_STATUS, 0);
            int mPostId = params.getExtras().getInt(POST_ID, 0);

            RequestParams requestParams = new RequestParams();
            requestParams.add(Constants.ACTION, Constants.UPDATE_ARTICLE_STATUS);
            requestParams.add(Constants.APP_KEY, Constants.APP_KEY_VALUE);
            requestParams.add(Constants.FB_ACCT_ID, Prefs.getString(Constants.KEY_REGISTER_USER_ID, null));
            requestParams.add(Constants.POST_STATUS, String.valueOf(mPostStatus));
            requestParams.add(Constants.FEED_POST_ID, String.valueOf(mPostId));

            request.executeSync(requestParams, new request.getResponseListener() {
                @Override
                public void onSuccess(JSONObject object) {

                    try {
                        int success = object.getInt(Constants.SUCCESS);

                        if (success == 1) {
                            Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
                        }else if(object.has(Constants.ERROR)){
                            Toast.makeText(getContext(), "Error: Update Status - " + object.getString(Constants.ERROR), Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        Crashlytics.logException(e);
                    }

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