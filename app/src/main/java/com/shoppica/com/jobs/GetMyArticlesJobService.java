package com.shoppica.com.jobs;


import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.Job;
import com.shoppica.com.constants.Constants;
import com.shoppica.com.db.SqlHelper;
import com.shoppica.com.notifications.NotificationCenter;
import com.shoppica.com.preferences.Prefs;
import com.shoppica.com.server.request;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetMyArticlesJobService extends Job {

    public static final String TAG = "getMyArticlesService";


    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {

        try {

            SqlHelper helper = SqlHelper.getInstance(getContext());

            RequestParams requestParams = new RequestParams();
            requestParams.add(Constants.ACTION, Constants.GET_MY_FAVORITES_IDS);
            requestParams.add(Constants.APP_KEY, Constants.APP_KEY_VALUE);
            requestParams.add(Constants.FB_ACCT_ID, Prefs.getString(Constants.KEY_REGISTER_USER_ID, null));

            request.executeSync(requestParams, new request.getResponseListener() {
                @Override
                public void onSuccess(JSONObject object) {

                    try {
                        int success = object.getInt(Constants.SUCCESS);

                        if (success == 1) {

                            JSONArray userList = object.getJSONArray(Constants.IDS);

                            if (userList.length() == 0)
                                return;

                            for (int i = 0; i < userList.length(); i++) {

                                String postId = userList.getString(i);
                                helper.insertFavoriteObject(Integer.valueOf(postId));
                            }

                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.addViewArticleObserver);

                        }

                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshFavoritesFragment);
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshOwnPostsFragment);
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