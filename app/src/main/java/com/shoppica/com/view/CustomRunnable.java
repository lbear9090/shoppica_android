package com.shoppica.com.view;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.shoppica.com.R;

import java.util.concurrent.TimeUnit;

public class CustomRunnable implements Runnable {

    public long millisUntilFinished;
    public TextView mTitle;
    public TextView mSubTitle;
    public Context mContext;
    private Handler handler;
    public boolean isArticle;

    public CustomRunnable(Handler handler, TextView mTitle, TextView mSubTitle) {
        this.handler = handler;
        this.mTitle = mTitle;
        this.mSubTitle = mSubTitle;
    }

    @Override
    public void run() {

        mSubTitle.setText(getDurationBreakdown(millisUntilFinished));

        millisUntilFinished -= 1000;

        handler.postDelayed(this, 1000);
    }

    private String getDurationBreakdown(long millis) {
        if (millis < 0) {
            mSubTitle.setVisibility(View.GONE);

            if (isArticle)
                mTitle.setText(mContext.getString(R.string.btn_action_extended));
            // mTitle.setText(mContext.getString(isArticle ? R.string.btn_extend: R.string.btn_promote));
            return isArticle ? "Artikel Verlopen" : "Artikel Uitlichten";
        }

        //  mTitle.setText(mContext.getString(isArticle ? R.string.btn_extend_active: R.string.btn_promote_active));
        mSubTitle.setVisibility(View.VISIBLE);

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        int months = 0;
        int years = 0;
        StringBuilder sb = new StringBuilder();
        if (days != 0) {

            if (days > 30) {
                double answer = days / 30;
                months = (int) Math.floor(answer);
                days = days % 30;

                if (months <= 12) {
                    sb.append(months);
                    sb.append(" ");
                    sb.append(months == 1 ?  mContext.getString(R.string.spannable_time_month) : mContext.getString(R.string.spannable_time_months));
                    if (days != 0) {
                        sb.append(" ");
                        sb.append(mContext.getString(R.string.spannable_time_and));
                        sb.append(" ");
                    }
                } else {

                    double answer2 = months / 12;
                    years = (int) Math.floor(answer2);
                    months = months % 12;
                    sb.append(years);
                    sb.append(" ");
                    sb.append(mContext.getString(R.string.spannable_time_year));
                    if (months != 0) {
                        sb.append(" ");
                        sb.append(mContext.getString(R.string.spannable_time_and));
                        sb.append(" ");
                        sb.append(months);
                        sb.append(" ");
                        sb.append(months == 1 ?  mContext.getString(R.string.spannable_time_month) : mContext.getString(R.string.spannable_time_months));

                    }
                }

            }

            if (years != 0 && months != 0)
                return sb.toString();

            sb.append(days);
            sb.append(" ");
            sb.append(days == 1 ?  mContext.getString(R.string.spannable_time_day) : mContext.getString(R.string.spannable_time_days));
        }

        if (months != 0 && days != 0)
            return sb.toString();


        if (hours != 0) {
            if (days != 0){
                sb.append(" ");
                sb.append(mContext.getString(R.string.spannable_time_and));
                sb.append(" ");
            }


            sb.append(hours);
            sb.append(" ");
            sb.append(mContext.getString(R.string.spannable_time_hour));
        }

        if (years == 0 && months == 0 && days == 0 && hours != 0 && millis != 0) {

            sb.append(" ");
            sb.append(mContext.getString(R.string.spannable_time_and));
            sb.append(" ");
            sb.append(minutes);
            sb.append(" ");
            sb.append(minutes == 1 ? mContext.getString(R.string.spannable_time_minute) : mContext.getString(R.string.spannable_time_minutes));
        }

        if (years == 0 && months == 0 && days == 0 && hours == 0) {
            sb.append(minutes);
            sb.append(" ");
            sb.append(" min");

            sb.append(" ");
            sb.append(mContext.getString(R.string.spannable_time_and));
            sb.append(" ");
            sb.append(seconds);
            sb.append(" ");
            sb.append("sec");

            return sb.toString();
        }


        return sb.toString();
    }
}