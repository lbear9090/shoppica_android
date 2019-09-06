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

public class ReportTradeIssueArticleJobService extends Job {

    public static final String TAG = "reportTradeIssueService";

    public static final String REPORT_MSG = "reportMsg";


    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {

        try {

            String mMessage = params.getExtras().getString(REPORT_MSG, null);

            RequestParams requestParams = new RequestParams();
            requestParams.add(Constants.ACTION, Constants.REPORT_TRADE_ISSUE);
            requestParams.add(Constants.MESSAGE, mMessage);
            requestParams.add(Constants.APP_KEY, Constants.APP_KEY_VALUE);


            String userId = Prefs.getString(Constants.KEY_REGISTER_USER_ID, null);
            String displayName = Prefs.getString(Constants.KEY_REGISTER_DISPLAY_NAME, null);

            if (!TextUtils.isEmpty(userId))
                requestParams.add(Constants.FB_ACCT_ID,userId);

            if (!TextUtils.isEmpty(displayName))
                requestParams.add(Constants.FB_NAME,displayName);

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