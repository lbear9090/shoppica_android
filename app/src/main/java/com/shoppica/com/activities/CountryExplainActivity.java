package com.shoppica.com.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;

import com.shoppica.com.R;
import com.shoppica.com.app.KledingApp;
import com.shoppica.com.constants.Constants;
import com.shoppica.com.country.CountryCodePicker;
import com.shoppica.com.notifications.NotificationCenter;
import com.shoppica.com.preferences.Prefs;
import com.shoppica.com.utils.AndroidUtilities;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class CountryExplainActivity extends FullBaseActivity {


    public static final String TAG = CountryExplainActivity.class.getSimpleName();

    @BindView(R.id.ccp)
    CountryCodePicker mCountryPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_explain);

        mCountryPicker.setOnCountryChangeListener(() -> {
            Prefs.putString(Constants.KEY_REGISTER_COUNTRY_CODE, mCountryPicker.getSelectedCountryNameCode());
            Prefs.putInt(Constants.KEY_REGISTER_PHONE_CODE, mCountryPicker.getSelectedCountryCodeAsInt());
            for (int i = 0; i < KledingApp.SUPPORTED_LOCALES.size(); i++) {

                Locale country = KledingApp.SUPPORTED_LOCALES.get(i);

                Log.d(TAG,"Locale == " + country.getCountry());
                Log.d(TAG,"mCountryPicker == " + mCountryPicker.getSelectedCountryNameCode().toUpperCase());

                if (country.getCountry().equals(mCountryPicker.getSelectedCountryNameCode().toUpperCase())) {
                    AndroidUtilities.restartInLocale(country);
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.recreateActivity);
                    break;
                }

            }
        });

    }


    @OnClick(R.id.btnContinue)
    void onContinueClicked() {


        if (TextUtils.isEmpty(mCountryPicker.getSelectedCountryNameCode())) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.snackbar_error_country_pick), Snackbar.LENGTH_SHORT).show();
            return;
        }

        Prefs.putString(Constants.KEY_REGISTER_COUNTRY_CODE, mCountryPicker.getSelectedCountryNameCode());
        Prefs.putInt(Constants.KEY_REGISTER_PHONE_CODE, mCountryPicker.getSelectedCountryCodeAsInt());

        for (int i = 0; i < KledingApp.SUPPORTED_LOCALES.size(); i++) {

            Locale country = KledingApp.SUPPORTED_LOCALES.get(i);

            Log.d(TAG,"Locale == " + country.getCountry());
            Log.d(TAG,"mCountryPicker == " + mCountryPicker.getSelectedCountryNameCode().toUpperCase());

            if (country.getCountry().equals(mCountryPicker.getSelectedCountryNameCode().toUpperCase())) {
                AndroidUtilities.restartInLocale(country);
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.recreateActivity);
                break;
            }

        }


        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}
