package com.shoppica.com.server;


import com.shoppica.com.R;
import com.shoppica.com.app.KledingApp;
import com.shoppica.com.constants.Constants;
import com.shoppica.com.utils.AndroidUtilities;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Carl on 2017-07-06.
 */

public class request {

    public interface getResponseListener {

        void onSuccess(JSONObject object);

        void onFailure(String error);

    }

    public static void execute(RequestParams params, final getResponseListener listener) {

        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        PersistentCookieStore myCookieStore = new PersistentCookieStore(KledingApp.getInstance());
        asyncHttpClient.setCookieStore(myCookieStore);
        asyncHttpClient.setResponseTimeout(300000);
        asyncHttpClient.setConnectTimeout(300000);
        asyncHttpClient.post(Constants.URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonResponse) {


                AndroidUtilities.runOnUIThread(() -> listener.onSuccess(jsonResponse));


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);

                if(!String.valueOf(statusCode).startsWith("2"))
                    AndroidUtilities.runOnUIThread(() -> listener.onFailure(KledingApp.getInstance().getResources().getString(R.string.state_error_general_desc)));
            }
        });

    }


    public static void executeSync(RequestParams params, final getResponseListener listener) {

        SyncHttpClient asyncHttpClient = new SyncHttpClient();
        PersistentCookieStore myCookieStore = new PersistentCookieStore(KledingApp.getInstance());
        asyncHttpClient.setCookieStore(myCookieStore);
        asyncHttpClient.setResponseTimeout(300000);
        asyncHttpClient.setConnectTimeout(300000);
        asyncHttpClient.post(Constants.URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonResponse) {

                AndroidUtilities.runOnUIThread(() -> listener.onSuccess(jsonResponse));

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);

                if(!String.valueOf(statusCode).startsWith("2"))
                    AndroidUtilities.runOnUIThread(() -> listener.onFailure(KledingApp.getInstance().getResources().getString(R.string.state_error_general_desc)));

            }
        });

    }

}
