package com.shoppica.com.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.StateSet;
import android.view.Display;
import android.view.WindowManager;

import com.shoppica.com.R;
import com.shoppica.com.app.KledingApp;
import com.shoppica.com.constants.Constants;
import com.shoppica.com.preferences.Prefs;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class AndroidUtilities {

    private static final String TAG = AndroidUtilities.class.getSimpleName();

    public static volatile DispatchQueue globalQueue = new DispatchQueue("globalQueue");
    private static DisplayMetrics displayMetrics = new DisplayMetrics();
    public static float density = 1;
    private static boolean usingHardwareInput;

    public static Point displaySize = new Point();

    static {
        checkDisplaySize(KledingApp.getInstance(), null);
    }
    public static String getShareUrl(){

        String url = "https://" + Constants.SERVER_URL;

        switch (Prefs.getString(Constants.KEY_REGISTER_COUNTRY_CODE,null).toLowerCase()) {

            case Constants.BE:
            case Constants.NL:
                url = "https://www.kledingkoopjes.net/";
                break;
        }

        return url;
    }


    private static void checkDisplaySize(Context context, Configuration newConfiguration) {
        try {
            density = context.getResources().getDisplayMetrics().density;
            Configuration configuration = newConfiguration;
            if (configuration == null) {
                configuration = context.getResources().getConfiguration();
            }
            usingHardwareInput = configuration.keyboard != Configuration.KEYBOARD_NOKEYS && configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO;
            WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (manager != null) {
                Display display = manager.getDefaultDisplay();
                if (display != null) {
                    display.getMetrics(displayMetrics);
                    display.getSize(displaySize);
                }
            }
            if (configuration.screenWidthDp != Configuration.SCREEN_WIDTH_DP_UNDEFINED) {
                int newSize = (int) Math.ceil(configuration.screenWidthDp * density);
                if (Math.abs(displaySize.x - newSize) > 3) {
                    displaySize.x = newSize;
                }
            }
            if (configuration.screenHeightDp != Configuration.SCREEN_HEIGHT_DP_UNDEFINED) {
                int newSize = (int) Math.ceil(configuration.screenHeightDp * density);
                if (Math.abs(displaySize.y - newSize) > 3) {
                    displaySize.y = newSize;
                }
            }

            Log.e(TAG, "display size = " + displaySize.x + " " + displaySize.y + " " + displayMetrics.xdpi + "x" + displayMetrics.ydpi);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        if (delay == 0) {
            KledingApp.applicationHandler.post(runnable);
        } else {
            KledingApp.applicationHandler.postDelayed(runnable, delay);
        }
    }



    public static Drawable createSimpleSelectorCircleDrawable(int size, int defaultColor, int pressedColor) {
        OvalShape ovalShape = new OvalShape();
        ovalShape.resize(size, size);
        ShapeDrawable defaultDrawable = new ShapeDrawable(ovalShape);
        defaultDrawable.getPaint().setColor(defaultColor);
        ShapeDrawable pressedDrawable = new ShapeDrawable(ovalShape);
        if (Build.VERSION.SDK_INT >= 21) {
            pressedDrawable.getPaint().setColor(0xffffffff);
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{StateSet.WILD_CARD},
                    new int[]{pressedColor}
            );
            return new RippleDrawable(colorStateList, defaultDrawable, pressedDrawable);
        } else {
            pressedDrawable.getPaint().setColor(pressedColor);
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
            stateListDrawable.addState(new int[]{android.R.attr.state_focused}, pressedDrawable);
            stateListDrawable.addState(StateSet.WILD_CARD, defaultDrawable);
            return stateListDrawable;
        }
    }

    public static int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = AndroidUtilities.getCurrentTime();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return KledingApp.getInstance().getString(R.string.spannable_time_ago_minute);
        } else if (diff < 2 * MINUTE_MILLIS) {
            return KledingApp.getInstance().getString(R.string.spannable_time_ago_minute);
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " " + KledingApp.getInstance().getString(R.string.spannable_time_ago_minutes);
        } else if (diff < 90 * MINUTE_MILLIS) {
            return KledingApp.getInstance().getString(R.string.spannable_time_ago_hour);
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " " +  KledingApp.getInstance().getString(R.string.spannable_time_ago_hours);
        } else if (diff < 48 * HOUR_MILLIS) {
            return KledingApp.getInstance().getString(R.string.spannable_time_ago_yesterday);
        } else {
            return diff / DAY_MILLIS + " " + KledingApp.getInstance().getString(R.string.spannable_time_ago_days);
        }
    }


    public static String getTimeAgoMember(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = AndroidUtilities.getCurrentTime();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return KledingApp.getInstance().getString(R.string.spannable_active_minute_ago);
        } else if (diff < 2 * MINUTE_MILLIS) {
            return KledingApp.getInstance().getString(R.string.spannable_active_minute_ago);
        } else if (diff < 50 * MINUTE_MILLIS) {
            return KledingApp.getInstance().getString(R.string.spannable_active_minutes_ago,(diff / MINUTE_MILLIS));
        } else if (diff < 90 * MINUTE_MILLIS) {
            return KledingApp.getInstance().getString(R.string.spannable_active_hour_ago);
        } else if (diff < 24 * HOUR_MILLIS) {
            return  KledingApp.getInstance().getString(R.string.spannable_active_hours_ago,(diff / HOUR_MILLIS));
        } else if (diff < 48 * HOUR_MILLIS) {
            return KledingApp.getInstance().getString(R.string.spannable_active_day_ago);
        } else {
            return KledingApp.getInstance().getString(R.string.spannable_active_days_ago,diff / DAY_MILLIS);
        }
    }

    public static String getDurationBreakdown(long millis) {
        if (millis < 0) {
            return "";
        }

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
                    sb.append(months == 1 ? KledingApp.getInstance().getString(R.string.spannable_time_month) : KledingApp.getInstance().getString(R.string.spannable_time_months));
                    if (days != 0) {
                        sb.append(" ");
                        sb.append(KledingApp.getInstance().getString(R.string.spannable_time_and));
                        sb.append(" ");
                    }
                } else {

                    double answer2 = months / 12;
                    years = (int) Math.floor(answer2);
                    months = months % 12;
                    sb.append(years);
                    sb.append(" ");
                    sb.append(KledingApp.getInstance().getString(R.string.spannable_time_year));
                    if (months != 0) {
                        sb.append(" ");
                        sb.append(KledingApp.getInstance().getString(R.string.spannable_time_and));
                        sb.append(" ");
                        sb.append(months);
                        sb.append(" ");
                        sb.append(months == 1 ? KledingApp.getInstance().getString(R.string.spannable_time_month) : KledingApp.getInstance().getString(R.string.spannable_time_months));

                    }
                }

            }

            if (years != 0 && months != 0)
                return sb.toString();

            sb.append(days);
            sb.append(" ");
            sb.append(days == 1 ? KledingApp.getInstance().getString(R.string.spannable_time_day) : KledingApp.getInstance().getString(R.string.spannable_time_days));
        }

        if (months != 0 && days != 0)
            return sb.toString();


        if (hours != 0) {
            if (days != 0) {
                sb.append(" ");
                sb.append(KledingApp.getInstance().getString(R.string.spannable_time_and));
                sb.append(" ");
            }

            sb.append(hours);
            sb.append(" ");
            sb.append(hours == 1 ? KledingApp.getInstance().getString(R.string.spannable_time_hour) : KledingApp.getInstance().getString(R.string.spannable_time_hours));
        }

        if (years == 0 && months == 0 && days == 0 && hours != 0 && millis != 0) {
            sb.append(" ");
            sb.append(KledingApp.getInstance().getString(R.string.spannable_time_and));
            sb.append(" ");
            sb.append(minutes);
            sb.append(" ");
            sb.append(minutes == 1 ? KledingApp.getInstance().getString(R.string.spannable_time_minute) : KledingApp.getInstance().getString(R.string.spannable_time_minutes));
        }

        if (years == 0 && months == 0 && days == 0 && hours == 0) {
            sb.append(minutes);
            sb.append(" ");
            sb.append(minutes == 1 ? KledingApp.getInstance().getString(R.string.spannable_time_minute) : KledingApp.getInstance().getString(R.string.spannable_time_minutes));

            return sb.toString();
        }

        return sb.toString();
    }
    public static boolean isPackageInstalled(String packagename, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    private final static double AVERAGE_RADIUS_OF_EARTH_KM = 6371;
    public static int calculateDistanceInKilometer(double userLat, double userLng,
                                                   double venueLat, double venueLng) {

        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(venueLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (int) (Math.round(AVERAGE_RADIUS_OF_EARTH_KM * c));
    }

    public static void restartInLocale(Locale locale) {
        Locale.setDefault(locale);
        Resources resources = KledingApp.getInstance().getResources();
        Configuration config = resources.getConfiguration();//new Configuration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            config.setLocale(locale);
        }else{
            config.locale = locale;
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            KledingApp.getInstance().createConfigurationContext(config);
        }else{
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }
    }

    public static long getCurrentTime() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Europe/Amsterdam"));
        return c.getTimeInMillis();
    }


}
