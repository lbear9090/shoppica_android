package com.shoppica.com.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.shoppica.com.R;
import com.shoppica.com.constants.Constants;
import com.shoppica.com.preferences.Prefs;

import butterknife.OnClick;

public class HomeExplainActivity extends FullBaseActivity {

    public static final String TAG = HomeExplainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_explain);

        if (Prefs.getBoolean(Constants.KEY_HOME_EXPLAIN_SEEN, false)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

    }


    @OnClick(R.id.btnContinue)
    void onContinueClicked() {
        Intent intent = new Intent(getApplicationContext(),CountryExplainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.into_anim_slide_from_right, R.anim.into_anim_slide_to_left);
        Prefs.putBoolean(Constants.KEY_HOME_EXPLAIN_SEEN, true);
        finish();
    }


}
