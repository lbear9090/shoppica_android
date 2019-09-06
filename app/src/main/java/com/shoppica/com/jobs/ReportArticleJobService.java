package com.shoppica.com.jobs;


import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import com.shoppica.com.R;
import com.shoppica.com.constants.Constants;
import com.shoppica.com.preferences.Prefs;
import com.shoppica.com.server.request;
import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.Job;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

public class ReportArticleJobService extends Job {

    public static final String TAG = "reportArticleJobService";

    public static final String REPORT_MSG = "reportMsg";
    public static final String REPORT_TYPE = "reportType";
    public static final String POST_ID = "postId";


    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {

        try {

            String mMessage = params.getExtras().getString(REPORT_MSG, null);
            int mPostId = params.getExtras().getInt(POST_ID, 0);
            int mReportType = params.getExtras().getInt(REPORT_TYPE, 0);

            RequestParams requestParams = new RequestParams();
            requestParams.add(Constants.ACTION, Constants.REPORT_ARTICLE);

            if (!TextUtils.isEmpty(mMessage))
                requestParams.add(Constants.REPORT_REASON, mMessage);

            requestParams.add(Constants.REPORT_TYPE, String.valueOf(mReportType));
            requestParams.add(Constants.APP_KEY, Constants.APP_KEY_VALUE);
            requestParams.add(Constants.FEED_POST_ID, String.valueOf(mPostId));
            requestParams.add(Constants.FB_ACCT_ID, Prefs.getString(Constants.KEY_REGISTER_USER_ID, null));

            request.executeSync(requestParams, new request.getResponseListener() {
                @Override
                public void onSuccess(JSONObject object) {

                    try {
                        int success = object.getInt(Constants.SUCCESS);

                        if (success == 1)
                            Toast.makeText(getContext(), R.string.toast_notice_sent, Toast.LENGTH_SHORT).show();


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