package com.shoppica.com.jobs;


import android.support.annotation.NonNull;

import com.shoppica.com.constants.Constants;
import com.shoppica.com.db.SqlHelper;
import com.shoppica.com.notifications.NotificationCenter;
import com.shoppica.com.preferences.Prefs;
import com.shoppica.com.server.request;
import com.shoppica.com.utils.AndroidUtilities;
import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.Job;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoggedInFacebookJobService extends Job {

    public static final String TAG = "loggedInFbJobService";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {

        try {
            SqlHelper helper = SqlHelper.getInstance(getContext());

            RequestParams requestParams = new RequestParams();
            requestParams.add(Constants.ACTION, Constants.LOGGED_IN_FACEBOOK);
            requestParams.add(Constants.APP_KEY, Constants.APP_KEY_VALUE);
            requestParams.add(Constants.FB_ACCT_ID, Prefs.getString(Constants.KEY_REGISTER_USER_ID, null));

            request.executeSync(requestParams, new request.getResponseListener() {
                @Override
                public void onSuccess(JSONObject object) {

                    try {
                        int success = object.getInt(Constants.SUCCESS);

                        if (success == 1) {

                            boolean startServiceNeeded = false;

                            JSONArray favorites = object.getJSONArray(Constants.FAVORITES);

                            if (favorites.length() != 0) {

                                for (int i = 0; i < favorites.length(); i++) {

                                    String postId = favorites.getString(i);
                                    helper.insertFavoriteObject(Integer.valueOf(postId));
                                }

                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.addViewArticleObserver);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshFavoritesFragment);
                            }

                            JSONArray articles = object.getJSONArray(Constants.ARTICLES);


                            if (articles.length() != 0) {

                                long currentTime = AndroidUtilities.getCurrentTime();

                                for (int i = 0; i < articles.length(); i++) {

                                    JSONObject articleObject = articles.getJSONObject(i);

                                    String postId = articleObject.getString(Constants.FEED_POST_ID);
                                    String postTimeExpires = articleObject.getString(Constants.FEED_POST_TIME_EXPIRES);
                                    String postPromotedTimeExpires = articleObject.getString(Constants.FEED_POST_PROMOTED_TIME);
                                    String itemTitle = articleObject.getString(Constants.FEED_ITEM_TITLE);
                                    String postPromoted = articleObject.getString(Constants.FEED_POST_PROMOTED);

                                    long expireTimePost = (Long.parseLong(postTimeExpires) * 1000);
                                    long expireTimePostPromoted = (Long.parseLong(postPromotedTimeExpires) * 1000);

                                    long timeLeftPost = expireTimePost - currentTime;

                                    if (timeLeftPost > 0) {
                                        startServiceNeeded = true;
                                        helper.insertPostObject(0, Integer.valueOf(postId), itemTitle, expireTimePost);
                                    }

                                    long timeLeftPostPromoted = expireTimePostPromoted - currentTime;

                                    if (timeLeftPostPromoted > 0 && Integer.valueOf(postPromoted) == 1) {
                                        helper.insertPostObject(1, Integer.valueOf(postId), itemTitle, expireTimePostPromoted);
                                        startServiceNeeded = true;
                                    }

                                }
                            }

                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshOwnPostsFragment);

                            if (startServiceNeeded)
                                DailyNotificationJob.schedule();


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