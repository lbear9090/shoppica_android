package com.shoppica.com.jobs;


import android.support.annotation.NonNull;

import com.shoppica.com.constants.Constants;
import com.shoppica.com.preferences.Prefs;
import com.shoppica.com.server.request;
import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.Job;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

public class FavoriteJobService extends Job {

    public static final String TAG = "favoriteJobService";

    public static final String ACTION = "action";
    public static final String POST_ID = "postId";


    @NonNull
    @Override
    protected Job.Result onRunJob(@NonNull Params params) {

        try {

            String action = params.getExtras().getString(ACTION, null);
            int postId = params.getExtras().getInt(POST_ID, 0);

            RequestParams requestParams = new RequestParams();
            requestParams.add(Constants.ACTION, action);
            requestParams.add(Constants.APP_KEY, Constants.APP_KEY_VALUE);
            requestParams.add(Constants.FEED_POST_ID, String.valueOf(postId));
            requestParams.add(Constants.FB_ACCT_ID, Prefs.getString(Constants.KEY_REGISTER_USER_ID, null));

            request.executeSync(requestParams, new request.getResponseListener() {
                @Override
                public void onSuccess(JSONObject object) {

                    try {
                        int success = object.getInt(Constants.SUCCESS);

                        if (success == 1) {

                        //    AndroidUtilities.runOnUIThread(() -> Toast.makeText(getContext(), "successful", Toast.LENGTH_SHORT).show());

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