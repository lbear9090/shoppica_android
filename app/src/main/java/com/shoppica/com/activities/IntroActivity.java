package com.shoppica.com.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.WindowManager;

import com.github.paolorotolo.appintro.AppIntro;
import com.shoppica.com.R;
import com.shoppica.com.fragments.slide1Fragment;
import com.shoppica.com.fragments.slide2Fragment;
import com.shoppica.com.fragments.slide3Fragment;
import com.shoppica.com.fragments.slide4Fragment;

public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        super.onCreate(savedInstanceState);

        // Note here that we DO NOT use setContentView();

        // Add your slide fragments here.
        // AppIntro will automatically generate the dots indicator and buttons.

        addSlide(new slide1Fragment());
        addSlide(new slide2Fragment());
        addSlide(new slide3Fragment());
        addSlide(new slide4Fragment());

        // Override bar/separator color.
        setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
        setSeparatorColor(ContextCompat.getColor(getApplicationContext(),R.color.transparent));

        // Hide Skip/Done button.
        showSkipButton(true);
        setSkipText(getString(R.string.btn_action_skip));
        setProgressButtonEnabled(true);

        // Turn vibration on and set intensity.
        // NOTE: you will probably need to ask VIBRATE permission in Manifest.
        setVibrate(true);
        setVibrateIntensity(30);

      //  setIndicatorColor(ContextCompat.getColor(getApplicationContext(), R.color.white),ContextCompat.getColor(getApplicationContext(),R.color.white_85));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (findViewById(android.R.id.content) != null) {
                findViewById(android.R.id.content).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.into_anim_slide_from_left, R.anim.into_anim_slide_to_right);
    }
}