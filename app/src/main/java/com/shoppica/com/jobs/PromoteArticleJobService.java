package com.shoppica.com.jobs;


import android.support.annotation.NonNull;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.Job;
import com.shoppica.com.constants.Constants;
import com.shoppica.com.notifications.NotificationCenter;
import com.shoppica.com.preferences.Prefs;
import com.shoppica.com.server.request;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

public class PromoteArticleJobService extends Job {

    public static final String TAG = "promoteArticleJobService";


    public static final String POST_ID = "postId";
    public static final String POST_PROMOTED = "postPromoted";



    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {

        try {


            int mPostId = params.getExtras().getInt(POST_ID, 0);
            int mPostPromoted = params.getExtras().getInt(POST_PROMOTED, 0);

            RequestParams requestParams = new RequestParams();
            requestParams.add(Constants.ACTION, Constants.PROMOTE_ARTICLE);
            requestParams.add(Constants.APP_KEY, Constants.APP_KEY_VALUE);
            requestParams.add(Constants.FB_ACCT_ID, Prefs.getString(Constants.KEY_REGISTER_USER_ID, null));
            requestParams.add(Constants.POST_PROMOTED, String.valueOf(mPostPromoted));
            requestParams.add(Constants.FEED_POST_ID, String.valueOf(mPostId));

            request.executeSync(requestParams, new request.getResponseListener() {
                @Override
                public void onSuccess(JSONObject object) {

                    try {
                        int success = object.getInt(Constants.SUCCESS);

                        if (success == 1) {
                            Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshOwnPostsFragment);
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