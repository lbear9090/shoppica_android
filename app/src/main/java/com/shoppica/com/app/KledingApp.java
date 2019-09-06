package com.shoppica.com.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.facebook.FacebookSdk;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.appevents.AppEventsLogger;
import com.shoppica.com.R;
import com.shoppica.com.jobs.DailyNewArticleJob;
import com.shoppica.com.jobs.DailyNotificationJob;
import com.shoppica.com.jobs.jobCreator;
import com.shoppica.com.preferences.Prefs;
import com.shoppica.com.utils.AndroidUtilities;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;
import pl.aprilapps.easyphotopicker.EasyImage;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class KledingApp extends Application implements Application.ActivityLifecycleCallbacks {

    private static KledingApp sApplication;
    public volatile static Handler applicationHandler;

    public static final List<Locale> SUPPORTED_LOCALES =
            Arrays.asList(
                    new Locale("de", "DE"),
                    new Locale("en", "AU"),
                    new Locale("en", "CA"),
                    new Locale("en", "GB"),
                    new Locale("en", "IE"),
                    new Locale("en", "IN"),
                    new Locale("en", "NZ"),
                    new Locale("en", "SG"),
                    new Locale("en", "US"),
                    new Locale("es", "ES"),
                    new Locale("fr", "FR"),
                    new Locale("it", "IT"),
                    new Locale("ja", "JP"),
                    new Locale("nl", "BE"),
                    new Locale("nl", "NL"),
                    new Locale("sv", "SE"),
                    new Locale("zh", "HK"));

    @Override
    public void onCreate() {
        super.onCreate();

        AudienceNetworkAds.initialize(this);

        sApplication = this;
        applicationHandler = new Handler(getMainLooper());
        com.evernote.android.job.JobManager.create(this).addJobCreator(new jobCreator());
        Fabric.with(this, new Crashlytics(), new Answers());

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        EasyImage.configuration(this)
                .setImagesFolderName(getString(R.string.app_name))
                .setCopyTakenPhotosToPublicGalleryAppFolder(true)
                .setCopyPickedImagesToPublicGalleryAppFolder(true)
                .setAllowMultiplePickInGallery(true);


        initializeFonts();

        initializePreferences();


        if(Prefs.getBoolean(com.shoppica.com.constants.Constants.KEY_NOTIFY_USER, true))
            DailyNewArticleJob.schedule();


        DailyNotificationJob.schedule();


        String language = Prefs.getString(com.shoppica.com.constants.Constants.KEY_REGISTER_COUNTRY_CODE, null);

        if (TextUtils.isEmpty(language))
            return;

        for (int i = 0; i < KledingApp.SUPPORTED_LOCALES.size(); i++) {

            Locale country = KledingApp.SUPPORTED_LOCALES.get(i);

            if (country.getCountry().equals(language)) {
                AndroidUtilities.restartInLocale(country);
                break;
            }

        }

        registerActivityLifecycleCallbacks(this);
    }

    public static KledingApp getInstance(Context context) {
        return (KledingApp) context.getApplicationContext();
    }

    public static Application getApplication() {
        return sApplication;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.get(getApplicationContext()).clearMemory();
    }


    public static Context getInstance() {
        return (getApplication().getApplicationContext());
    }

    private void initializeFonts() {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/MONTSERRAT-MEDIUM.TTF")
                .setFontAttrId(R.attr.fontPath)
                .build());
    }

    private void initializePreferences() {

        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {
        Prefs.putInt(com.shoppica.com.constants.Constants.KEY_TIME_LEFT_APP, Math.round(AndroidUtilities.getCurrentTime() / 1000));

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
