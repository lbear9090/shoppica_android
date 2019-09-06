package com.shoppica.com.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.shoppica.com.app.KledingApp;
import com.shoppica.com.db.SqlHelper;

import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by kcampagna on 6/21/14.
 */
abstract public class FullBaseActivity extends AppCompatActivity {

    public final String TAG = FullBaseActivity.class.getSimpleName();

    public KledingApp app;

    public float density;

    public SqlHelper helper;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        density = getResources().getDisplayMetrics().density;
        app = KledingApp.getInstance(getApplicationContext());
        helper = SqlHelper.getInstance(getApplicationContext());

        super.onCreate(savedInstanceState);

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
    }


    @Override
    protected void onStart() {
        Log.v(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Log.v(TAG, "onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.v(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.v(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy");
        super.onDestroy();
    }



    public void showDialogFragmentSupport(android.support.v4.app.DialogFragment fragment, String title) {
        try {
            getSupportFragmentManager().beginTransaction().add(fragment, title).commit();
        } catch (IllegalStateException ex) {
            Log.e(TAG, "Unable to show dialog fragment", ex);
        }
    }

    /**
     * Dismisses the dialog fragment with the given title
     *
     * @param title The title of the Dialog Fragment
     */

    public void dismissDialogFragmentSupport(String title) {
        android.support.v4.app.Fragment fragment = getSupportFragmentManager().findFragmentByTag(title);

        if (fragment != null && fragment instanceof android.support.v4.app.DialogFragment) {
            ((android.support.v4.app.DialogFragment) fragment).dismissAllowingStateLoss();
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarColor(int color) {
        if (isApiLevel(Build.VERSION_CODES.LOLLIPOP)) {
            getWindow().setStatusBarColor(color);
        }
    }

    /**
     * Sets the color of the status bar, only for SDK 21+ devices
     *
     * @param color
     */
    public void setStatusBarColorResource(@ColorRes int color) {
        if (isApiLevel(Build.VERSION_CODES.LOLLIPOP)) {
            setStatusBarColor(ContextCompat.getColor(getApplicationContext(), color));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }



    public static  void hideKeyboard(View view) {

        // hide keyboard
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static  void showKeyboard(View view) {

        // show keyboard
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, 0);
        }

    }

    protected boolean isApiLevel(int apiLevel) {
        return Build.VERSION.SDK_INT >= apiLevel;
    }


}
