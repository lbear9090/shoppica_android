package com.shoppica.com.interfaces;

import android.support.v4.app.FragmentActivity;

public class BaseBackPressedListener implements OnBackPressedListener {
    private final FragmentActivity activity;

    public BaseBackPressedListener(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void doBack() {
        activity.finish();
    }
}