package com.shoppica.com.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.shoppica.com.BuildConfig;
import com.shoppica.com.R;
import com.shoppica.com.app.KledingApp;
import com.shoppica.com.constants.Constants;
import com.shoppica.com.country.CountryCodePicker;
import com.shoppica.com.db.SqlHelper;
import com.shoppica.com.dialogs.FeedBackDialog;
import com.shoppica.com.dialogs.SafeTradeDialog;
import com.shoppica.com.facebook.FacebookHelper;
import com.shoppica.com.facebook.FacebookListener;
import com.shoppica.com.jobs.JobLauncher;
import com.shoppica.com.notifications.NotificationCenter;
import com.shoppica.com.preferences.Prefs;
import com.shoppica.com.utils.AndroidUtilities;
import com.shoppica.com.utils.NetworkUtils;
import com.shoppica.com.utils.ViewUtil;
import com.shoppica.com.view.ActivityRecreationHelper;
import com.shoppica.com.view.SingleShotLocationProvider;
import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.pnikosis.materialishprogress.ProgressWheel;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

import static com.shoppica.com.constants.Constants.KEY_REGISTER_BIRTHDAY;
import static com.shoppica.com.constants.Constants.KEY_REGISTER_DISPLAY_NAME;
import static com.shoppica.com.constants.Constants.KEY_REGISTER_EMAIL;
import static com.shoppica.com.constants.Constants.KEY_REGISTER_GENDER;
import static com.shoppica.com.constants.Constants.KEY_REGISTER_PROFILE_LINK;
import static com.shoppica.com.constants.Constants.KEY_REGISTER_PROFILE_URL;
import static com.shoppica.com.constants.Constants.KEY_REGISTER_SOCIAL_AUTH_TOKEN;
import static com.shoppica.com.constants.Constants.KEY_REGISTER_SOCIAL_ID;
import static com.shoppica.com.constants.Constants.KEY_REGISTER_USER_ID;

@RuntimePermissions
public class SettingsActivity extends BaseActivity implements FacebookListener, FeedBackDialog.OnCompleteListener, SafeTradeDialog.OnCompleteListener {


    private static final String MALE = "male";
    private static final String FIELDS = "fields";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String LINK = "link";
    private static final String GENDER = "gender";
    private static final String BIRTHDAY = "birthday";
    private static final String EMAIL = "email";
    private static final String PICTURE = "picture";
    private static final String DATA = "data";
    private static final String URL = "url";
    private static final String TYPE1 = "MM/dd/yyyy";
    private static final String TYPE2 = "yyyy-MM-dd";

    @BindView(R.id.back)
    ImageButton mBack;

    @BindView(R.id.facebookButtonTitle)
    TextView mFacebookSync;

    @BindView(R.id.contactWhatsAppTitle)
    TextView mWhatsApp;

    @BindView(R.id.contactInstagramTitle)
    TextView mInstagram;

    @BindView(R.id.contactEmailTitle)
    TextView mEmail;

    @BindView(R.id.currentLocationTitle)
    TextView mLocation;

    @BindView(R.id.ownLocationTitle)
    TextView mLocationSelf;

    @BindView(R.id.versionSettings)
    TextView mVersion;

    @BindView(R.id.locationProgress)
    ProgressWheel mLocationProgress;

    @BindView(R.id.ccp)
    CountryCodePicker mCountryPicker;

    @BindView(R.id.switchNotify)
    SwitchCompat mSwitchNotify;

    @BindView(R.id.notificationSubTitle2)
    TextView mNotificationText;

    String mInstagramText, mWhatsAppText, mEmailText, mInputLocation, mFetchedLocation;

    private float mFetchLocationLatitude, mFetchLocationLongitude;

    private FacebookHelper mFacebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initDrawable();

        init();

    }


    private void init() {

        mFacebook = new FacebookHelper(this);

        boolean loggedIn = AccessToken.getCurrentAccessToken() != null;

        if (loggedIn)
            mFacebookSync.setText(getString(R.string.settings_facebook_button_title_on));
        else
            mFacebookSync.setText(getString(R.string.settings_facebook_button_title_off));


        mWhatsAppText = Prefs.getString(Constants.KEY_POST_WHATSAPP, null);

        if (!TextUtils.isEmpty(mWhatsAppText)) {
            mWhatsApp.setText(mWhatsAppText);
            mWhatsApp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_whatsapp, 0, R.drawable.ic_edit, 0);
        }

        mInstagramText = Prefs.getString(Constants.KEY_POST_INSTAGRAM, null);

        if (!TextUtils.isEmpty(mInstagramText)) {
            mInstagram.setText(mInstagramText);
            mInstagram.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_instagram, 0, R.drawable.ic_edit, 0);
        }


        mEmailText = Prefs.getString(Constants.KEY_POST_EMAIL, null);

        if (!TextUtils.isEmpty(mEmailText)) {
            mEmail.setText(mEmailText);
            mEmail.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_email, 0, R.drawable.ic_edit, 0);
        }

        mFetchedLocation = Prefs.getString(Constants.KEY_POST_CURRENT_LOCATION, null);

        if (!TextUtils.isEmpty(mFetchedLocation)) {
            mLocation.setText(mFetchedLocation);
            mLocation.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit, 0);
        }

        mInputLocation = Prefs.getString(Constants.KEY_POST_OWN_LOCATION, null);

        if (!TextUtils.isEmpty(mInputLocation)) {
            mLocationSelf.setText(mInputLocation);
            mLocationSelf.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit, 0);
            Prefs.remove(Constants.KEY_POST_CURRENT_LOCATION);
            mFetchedLocation = "";
            mLocation.setText("");
            mLocation.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (findViewById(android.R.id.content) != null) {
                findViewById(android.R.id.content).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        String versionName = BuildConfig.VERSION_NAME;
        mVersion.setText(getString(R.string.settings_version, versionName));


        String currentCountry = Prefs.getString(Constants.KEY_REGISTER_COUNTRY_CODE, null);
        if (!TextUtils.isEmpty(currentCountry))
            mCountryPicker.setCountryForNameCode(currentCountry);

        mCountryPicker.setOnCountryChangeListener(() -> {
            Prefs.putString(Constants.KEY_REGISTER_COUNTRY_CODE, mCountryPicker.getSelectedCountryNameCode());
            Prefs.putInt(Constants.KEY_REGISTER_PHONE_CODE, mCountryPicker.getSelectedCountryCodeAsInt());


            for (int i = 0; i < KledingApp.SUPPORTED_LOCALES.size(); i++) {

                Locale country = KledingApp.SUPPORTED_LOCALES.get(i);

                Log.d(TAG, "Locale == " + country.getCountry());
                Log.d(TAG, "mCountryPicker == " + mCountryPicker.getSelectedCountryNameCode().toUpperCase());

                if (country.getCountry().equals(mCountryPicker.getSelectedCountryNameCode().toUpperCase())) {
                    AndroidUtilities.restartInLocale(country);
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.recreateActivity);
                    ActivityRecreationHelper.recreate(this, true);
                    break;
                }

            }


        });

        mSwitchNotify.setChecked(Prefs.getBoolean(Constants.KEY_NOTIFY_USER, true));
        mNotificationText.setText(Prefs.getBoolean(Constants.KEY_NOTIFY_USER, true) ? getString(R.string.settings_notifications_switch_on) : getString(R.string.settings_notifications_switch_off));
        mSwitchNotify.setOnCheckedChangeListener((compoundButton, b) -> {

            Prefs.putBoolean(Constants.KEY_NOTIFY_USER, compoundButton.isChecked());
            mNotificationText.setText(compoundButton.isChecked() ? getString(R.string.settings_notifications_switch_on) : getString(R.string.settings_notifications_switch_off));

        });
    }


    private void initDrawable() {

        Drawable backDrawable = AndroidUtilities.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(48), ContextCompat.getColor(getApplicationContext(),
                R.color.transparent), ContextCompat.getColor(getApplicationContext(), R.color.colorFilterPressed));

        mBack.setBackground(backDrawable);

    }

    @OnClick(R.id.back)
    void onBackClicked() {
        finish();
    }

    @OnClick(R.id.facebookButton)
    void onFacebookClicked() {

        boolean loggedIn = AccessToken.getCurrentAccessToken() != null;

        if (loggedIn) {
            onError(getString(R.string.settings_logout), getString(R.string.settings_logout_desc), true);
        } else {
            if (!NetworkUtils.isConnected()) {
                onErrorFb(getString(R.string.alert_error_network_title), getString(R.string.alert_error_internet_facebook_desc), getString(R.string.btn_action_retry), true);
                return;
            }
            mFacebook.performSignIn(this);

        }


    }

    @OnClick(R.id.contactWhatsAppView)
    void onWhatsAppClicked() {

        if (!isAppInstalled(Constants.PACKAGE_NAME_WHATSAPP)) {
            onError(getString(R.string.view_article_not_installed_error_whatsapp_title), getString(R.string.view_article_not_installed_error_whatsapp_desc2), Constants.PACKAGE_NAME_WHATSAPP);
            return;
        }

        showEditDialog(getString(R.string.alert_add_whatsapp), getString(R.string.alert_add_whatsapp_sub), getString(R.string.alert_add_whatsapp_hint), mWhatsAppText, 0);
    }

    @OnClick(R.id.contactInstagramView)
    void onInstagramClicked() {

        showEditDialog(getString(R.string.alert_add_instagram), getString(R.string.alert_add_instagram_sub), getString(R.string.alert_add_instagram_hint), mInstagramText, 1);


    }

    @OnClick(R.id.contactEmailView)
    void onEmailClicked() {

        showEditDialog(getString(R.string.alert_add_email), getString(R.string.alert_add_email_sub), getString(R.string.alert_add_email_hint), mEmailText, 2);
    }


    @OnClick(R.id.currentLocationView)
    void onGetCurrentLocation() {


        SettingsActivityPermissionsDispatcher.continueLocationPermissionWithCheck(this);

    }


    @OnClick(R.id.ownLocationView)
    void onOwnLocationCLicked() {

        showEditDialog(getString(R.string.alert_add_own_location), getString(R.string.alert_add_own_location_sub), getString(R.string.alert_add_own_location_sub_hint), mInputLocation, 3);
    }

    @OnClick(R.id.sendFeedBackView)
    void onSendFeedBackClicked() {

        FeedBackDialog dialog = new FeedBackDialog();
        showDialogFragmentSupport(dialog, FeedBackDialog.TAG);
    }

    @OnClick(R.id.safeExplainView)
    void onSafeExplainView() {

        SafeTradeDialog dialog = new SafeTradeDialog();
        showDialogFragmentSupport(dialog, SafeTradeDialog.TAG);
    }

    @OnClick(R.id.followUsBtnView)
    void onFollowClicked() {

        Uri uri = Uri.parse("http://instagram.com/_u/" + getString(R.string.spannable_follow_username));
        Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

        likeIng.setPackage("com.instagram.android");

        try {
            startActivity(likeIng);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://instagram.com/" + getString(R.string.spannable_follow_username))));
        }

    }

    private void openPlayStore(String appPackageName) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    private boolean isAppInstalled(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }


    private void showEditDialog(String title, String subTitle, String hint, String currentText, int type) {

        LayoutInflater li = LayoutInflater.from(this);

        int layout;

        if (type == 0) {
            layout = R.layout.item_edit_dialog_number_code;
        } else
            layout = R.layout.item_edit_dialog;

        @SuppressLint("InflateParams") View promptsView = li.inflate(layout, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.DialogTheme);
        alertDialogBuilder.setView(promptsView);

        TextView titleTextView = ViewUtil.findById(promptsView, R.id.title);
        TextView subTitleTextView = ViewUtil.findById(promptsView, R.id.subTitle);
        CountryCodePicker countryCodePicker = null;

        titleTextView.setText(title);
        subTitleTextView.setText(subTitle);

        final EditText userInput = promptsView.findViewById(R.id.editText);
        userInput.setHint(hint);


        if (type == 0) {
            userInput.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
            int maxLength = 18;
            userInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});

            countryCodePicker = ViewUtil.findById(promptsView, R.id.ccp);
            int phoneCode = Prefs.getInt(Constants.KEY_REGISTER_PHONE_CODE, 0);

            String phoneNumber = Prefs.getString(Constants.KEY_REGISTER_PHONE_NUMBER, null);

            if (!TextUtils.isEmpty(phoneNumber))
                currentText = phoneNumber;

            if (phoneCode != 0)
                countryCodePicker.setCountryForPhoneCode(phoneCode);

        }

        if (!TextUtils.isEmpty(currentText)) {
            userInput.setText(currentText);
            userInput.setSelection(currentText.length());
        }


        showKeyboard(userInput);

        // set dialog message
        CountryCodePicker finalCountryCodePicker = countryCodePicker;
        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton(R.string.btn_action_ok,
                        (dialog, id) -> {


                            if (type == 0) {
                                if (!TextUtils.isEmpty(userInput.getText().toString())) {

                                    String countryCode = Prefs.getString(Constants.KEY_REGISTER_COUNTRY_CODE, null);

                                    if (userInput.getText().length() < 6) {
                                        onError(getString(R.string.alert_whatsapp_error_title), getString(R.string.alert_whatsapp_error_desc));
                                        return;
                                    }

                                    mWhatsAppText = userInput.getText().toString();

                                    switch (countryCode.toLowerCase()) {
                                        case Constants.BE:
                                        case Constants.NL:
                                            if (mWhatsAppText.startsWith("0"))
                                                mWhatsAppText = mWhatsAppText.substring(1);
                                            break;
                                    }

                                    int phoneCode = finalCountryCodePicker.getSelectedCountryCodeAsInt();

                                    Prefs.putInt(Constants.KEY_REGISTER_PHONE_CODE, phoneCode);
                                    Prefs.putString(Constants.KEY_REGISTER_PHONE_NUMBER, mWhatsAppText);

                                    mWhatsAppText = "+" + String.valueOf(phoneCode) + mWhatsAppText;
                                    mWhatsApp.setText(mWhatsAppText);
                                    mWhatsApp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_whatsapp, 0, R.drawable.ic_edit, 0);
                                } else {
                                    mWhatsAppText = "";
                                    mWhatsApp.setText("");
                                    mWhatsApp.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                }
                                Prefs.putString(Constants.KEY_POST_WHATSAPP, mWhatsAppText);
                            } else if (type == 1) {
                                if (!TextUtils.isEmpty(userInput.getText().toString())) {

                                    if (!userInput.getText().toString().matches(Constants.INSTAGRAM_REGEX)) {
                                        onError(getString(R.string.alert_instagram_error_title), getString(R.string.alert_instagram_error_desc));
                                        hideKeyboard(userInput);
                                        return;
                                    }
                                    mInstagramText = userInput.getText().toString();
                                    mInstagram.setText(mInstagramText);
                                    mInstagram.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_instagram, 0, R.drawable.ic_edit, 0);
                                } else {
                                    mInstagramText = "";
                                    mInstagram.setText("");
                                    mInstagram.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                }
                                Prefs.putString(Constants.KEY_POST_INSTAGRAM, mInstagramText);
                            } else if (type == 2) {
                                if (!TextUtils.isEmpty(userInput.getText())) {
                                    mEmailText = userInput.getText().toString();
                                    mEmail.setText(mEmailText);
                                    mEmail.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_email, 0, R.drawable.ic_edit, 0);
                                } else {
                                    mEmailText = "";
                                    mEmail.setText("");
                                    mEmail.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                }
                                Prefs.putString(Constants.KEY_POST_EMAIL, mEmailText);
                            } else if (type == 3) {
                                if (!TextUtils.isEmpty(userInput.getText())) {
                                    mInputLocation = userInput.getText().toString();
                                    getValuesFromCity(mInputLocation);
                                    mLocationSelf.setText(mInputLocation);
                                    mLocationSelf.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit, 0);

                                    Prefs.remove(Constants.KEY_POST_CURRENT_LOCATION);
                                    Prefs.remove(Constants.KEY_POST_CURRENT_LOCATION_LATITUDE);
                                    Prefs.remove(Constants.KEY_POST_CURRENT_LOCATION_LONGITUDE);
                                    mFetchedLocation = "";
                                    mLocation.setText("");
                                    mLocation.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                } else {
                                    mInputLocation = "";
                                    mLocationSelf.setText("");
                                    mLocationSelf.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                }
                                Prefs.putString(Constants.KEY_POST_OWN_LOCATION, mInputLocation);
                            }


                            hideKeyboard(userInput);
                        })
                .setNegativeButton(R.string.btn_action_cancel, (dialogInterface, i) -> {
                    hideKeyboard(userInput);
                    dialogInterface.dismiss();
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            alertDialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_dialog));
        }

        // show it
        alertDialog.show();


    }

    private void onError(String titleMsg, String errorMsg) {

        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.into_alert_view, null);

        TextView errorTitle = ViewUtil.findById(view, R.id.into_title_error);
        TextView errorText = ViewUtil.findById(view, R.id.into_desc_error);

        if (!TextUtils.isEmpty(titleMsg))
            errorTitle.setText(titleMsg);

        if (!TextUtils.isEmpty(errorMsg))
            errorText.setText(errorMsg);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setView(view);

        builder.setPositiveButton(R.string.btn_action_ok, null);


        builder.show();
    }


    private void getValuesFromCity(String city) {

        if (Geocoder.isPresent()) {
            try {
                Geocoder gc = new Geocoder(this);
                List<Address> addresses = gc.getFromLocationName(city, 5); // get the found Address Objects

                if (addresses.size() > 0) {

                    Address address = addresses.get(0);

                    double mInputLocationLatitude = address.getLatitude();
                    double mInputLocationLongitude = address.getLongitude();

                    if (mInputLocationLatitude != 0 && mInputLocationLongitude != 0) {
                        Prefs.putDouble(Constants.KEY_POST_OWN_LOCATION_LATITUDE, mInputLocationLatitude);
                        Prefs.putDouble(Constants.KEY_POST_OWN_LOCATION_LONGITUDE, mInputLocationLongitude);
                    }
                }


            } catch (IOException e) {
                Crashlytics.logException(e);
            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SettingsActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);

    }


    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void continueLocationPermission() {

        mLocation.setVisibility(View.GONE);
        mLocationProgress.setVisibility(View.VISIBLE);

        SingleShotLocationProvider.requestSingleUpdate(getApplicationContext(), location -> {

            try {
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(this, Locale.getDefault());

                mFetchLocationLatitude = location.latitude;
                mFetchLocationLongitude = location.longitude;

                if (mFetchLocationLatitude != 0 && mFetchLocationLongitude != 0) {
                    Prefs.putFloat(Constants.KEY_POST_CURRENT_LOCATION_LATITUDE, mFetchLocationLatitude);
                    Prefs.putFloat(Constants.KEY_POST_CURRENT_LOCATION_LONGITUDE, mFetchLocationLongitude);
                }
                addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1); // Here one represent max location result to returned, by documents it recommended one to 5

                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();

                if (!TextUtils.isEmpty(city))
                    mFetchedLocation = city;

                if (!TextUtils.isEmpty(state))
                    mFetchedLocation = TextUtils.isEmpty(city) ? state : city + ", " + state;

                if (!TextUtils.isEmpty(mFetchedLocation)) {
                    mLocation.setText(mFetchedLocation);
                    mLocation.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit, 0);
                    Prefs.putString(Constants.KEY_POST_CURRENT_LOCATION, mFetchedLocation);
                    mInputLocation = "";
                    mLocationSelf.setText("");
                    mLocationSelf.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    Prefs.remove(Constants.KEY_POST_OWN_LOCATION);
                    Prefs.remove(Constants.KEY_POST_OWN_LOCATION_LATITUDE);
                    Prefs.remove(Constants.KEY_POST_OWN_LOCATION_LONGITUDE);
                } else
                    onError(getString(R.string.alert_location_error_title2), getString(R.string.alert_location_error_desc_3), false);
            } catch (IOException e) {
                Crashlytics.logException(e);
                onError(getString(R.string.alert_location_error_title2), getString(R.string.alert_location_error_desc_3), false);
            }

            mLocationProgress.setVisibility(View.GONE);
            mLocation.setVisibility(View.VISIBLE);


        });

    }


    private void onError(String titleMsg, String errorMsg, boolean isLogOut) {

        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.into_alert_view, null);

        TextView errorTitle = ViewUtil.findById(view, R.id.into_title_error);
        TextView errorText = ViewUtil.findById(view, R.id.into_desc_error);

        if (!TextUtils.isEmpty(titleMsg))
            errorTitle.setText(titleMsg);

        if (!TextUtils.isEmpty(errorMsg))
            errorText.setText(errorMsg);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setView(view);

        if (isLogOut) {
            builder.setPositiveButton(R.string.btn_action_logout, (dialogInterface, i) -> {

                mFacebook.performSignOut();

            });

            builder.setNegativeButton(R.string.btn_action_cancel, null);


        } else
            builder.setPositiveButton(R.string.btn_action_ok, null);

        builder.setCancelable(false);
        builder.show();
    }

    @OnPermissionDenied({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void showDeniedForLocation() {


        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.into_alert_view, null);
        builder.setView(view);
        TextView title = ViewUtil.findById(view, R.id.into_title_error);
        title.setText(getString(R.string.alert_default_permission_denied_title));
        TextView desc = ViewUtil.findById(view, R.id.into_desc_error);
        desc.setText(getString(R.string.alert_location_permission_desc));
        builder.setPositiveButton(R.string.btn_action_ok, null);
        builder.show();
    }

    @OnNeverAskAgain({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void showNeverAskForLocation() {


        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.into_alert_view, null);
        builder.setView(view);
        TextView title = ViewUtil.findById(view, R.id.into_title_error);
        title.setText(getString(R.string.alert_default_permission_denied_title));
        TextView desc = ViewUtil.findById(view, R.id.into_desc_error);
        desc.setText(getString(R.string.alert_location_permission_never_desc));
        builder.setPositiveButton(R.string.btn_action_settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);

            }
        });
        builder.setNegativeButton(R.string.btn_action_cancel, null);

        AlertDialog dialog = builder.create();
        dialog.show();
        //for negative side button
        dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.alertCancelBtn));


    }

    private void onError(String titleMsg, String errorMsg, String packageName) {

        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.into_alert_view, null);

        TextView errorTitle = ViewUtil.findById(view, R.id.into_title_error);
        TextView errorText = ViewUtil.findById(view, R.id.into_desc_error);

        if (!TextUtils.isEmpty(titleMsg))
            errorTitle.setText(titleMsg);

        if (!TextUtils.isEmpty(errorMsg))
            errorText.setText(errorMsg);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setView(view);

        builder.setPositiveButton(R.string.btn_action_install, (dialogInterface, i) -> openPlayStore(packageName));
        builder.setNegativeButton(R.string.btn_action_cancel, null);

        builder.setCancelable(false);
        builder.show();
    }


    private void onErrorFb(String titleMsg, String errorMsg, String positiveBtnMsg, boolean negativeBtnEnabled) {

        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.into_alert_view, null);

        TextView errorTitle = ViewUtil.findById(view, R.id.into_title_error);
        TextView errorText = ViewUtil.findById(view, R.id.into_desc_error);

        if (!TextUtils.isEmpty(titleMsg))
            errorTitle.setText(titleMsg);

        if (!TextUtils.isEmpty(errorMsg))
            errorText.setText(errorMsg);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setView(view);

        builder.setPositiveButton(positiveBtnMsg, (dialog, which) -> onFacebookClicked());

        if (negativeBtnEnabled)
            builder.setNegativeButton(R.string.btn_action_ok, null);
        builder.setCancelable(false);
        builder.show();
    }

    private void onErrorFbGeneral() {

        View view = LayoutInflater.from(this).inflate(R.layout.alert_view_error_fb, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setView(view);
        builder.setPositiveButton(R.string.btn_action_ok, null);
        builder.setCancelable(false);
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mFacebook.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onFbSignInCanceled() {

    }

    @Override
    public void onFbSignInFail(String errorMessage) {
        onErrorFbGeneral();
    }

    @Override
    public void onFbSignInSuccess(AccessToken accessToken, String authToken, String userId) {
        Prefs.putString(KEY_REGISTER_SOCIAL_AUTH_TOKEN, authToken);
        Prefs.putString(KEY_REGISTER_USER_ID, userId);

        Bundle parameters = new Bundle();
        parameters.putString(FIELDS, "id,name,gender,picture.width(200)");

        GraphRequest request = GraphRequest.newMeRequest(
                accessToken, (me, response) -> {

                    if (response.getError() != null) {
                        onFbSignInFail(response.getError().getErrorMessage());
                        return;
                    }

                    String id = me.optString(ID);
                    String name = me.optString(NAME);
                    String gender = me.optString(GENDER);

                    if (!TextUtils.isEmpty(id))
                        Prefs.putString(KEY_REGISTER_SOCIAL_ID, id);

                    if (!TextUtils.isEmpty(name))
                        Prefs.putString(KEY_REGISTER_DISPLAY_NAME, name);


                    if (!TextUtils.isEmpty(gender)) {
                        int genderChoice = gender.equals(MALE) ? 1 : 0;
                        Prefs.putInt(KEY_REGISTER_GENDER, genderChoice);
                    }

                    if (me.has(PICTURE)) {
                        try {
                            String profilePicUrl = me.getJSONObject(PICTURE).getJSONObject(DATA).getString(URL);
                            if (!TextUtils.isEmpty(profilePicUrl))
                                Prefs.putString(KEY_REGISTER_PROFILE_URL, profilePicUrl);
                        } catch (JSONException e) {
                            Crashlytics.logException(e);
                        }
                    }

                    JobLauncher.scheduleFacebookLogin();


                    Toast.makeText(getApplicationContext(), R.string.toast_notice_login_success, Toast.LENGTH_SHORT).show();

                    mFacebookSync.setText(getString(R.string.settings_facebook_button_title_on));

                });

        request.setParameters(parameters);
        request.executeAsync();

    }


    @Override
    public void onFBSignOut() {
        Prefs.remove(KEY_REGISTER_SOCIAL_AUTH_TOKEN);
        Prefs.remove(KEY_REGISTER_USER_ID);
        Prefs.remove(KEY_REGISTER_SOCIAL_ID);
        Prefs.remove(KEY_REGISTER_EMAIL);
        Prefs.remove(KEY_REGISTER_DISPLAY_NAME);
        Prefs.remove(KEY_REGISTER_BIRTHDAY);
        Prefs.remove(KEY_REGISTER_GENDER);
        Prefs.remove(KEY_REGISTER_PROFILE_LINK);
        Prefs.remove(KEY_REGISTER_PROFILE_URL);

        helper.close();

        NotificationCenter.getInstance().postNotificationName(NotificationCenter.logoutFromFacebook);

        deleteDatabase(SqlHelper.DB_NAME);

        Toast.makeText(getApplicationContext(), R.string.toast_notice_logout_success, Toast.LENGTH_SHORT).show();
        mFacebookSync.setText(getString(R.string.settings_facebook_button_title_off));
    }


    @Override
    public void onFeedBackSend(String message) {

        long timeOut = Prefs.getLong(Constants.FEED_BACK_TIME_OUT, 0);
        long currTime = AndroidUtilities.getCurrentTime();

        if (timeOut != 0) {
            if(currTime < timeOut){
                long waitTime = (timeOut - currTime) / 1000;

                String quantityString = getResources().getQuantityString(R.plurals.send_time_out, Math.round(waitTime)
                        , Math.round(waitTime));

                Toast.makeText(getApplicationContext(), quantityString, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Prefs.putLong(Constants.FEED_BACK_TIME_OUT, AndroidUtilities.getCurrentTime() + 60000);

        dismissDialogFragmentSupport(FeedBackDialog.TAG);

        JobLauncher.scheduleFeedBack(message);

    }

    @Override
    public void onSafeTradingTellingUs(String message) {

        long timeOut = Prefs.getLong(Constants.REPORT_TRADE_TIME_OUT, 0);
        long currTime = AndroidUtilities.getCurrentTime();
        ;

        if (timeOut != 0) {
            if(currTime < timeOut){
                long waitTime = (timeOut - currTime) / 1000;

                String quantityString = getResources().getQuantityString(R.plurals.trade_time_out, Math.round(waitTime)
                        , Math.round(waitTime));

                Toast.makeText(getApplicationContext(), quantityString, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Prefs.putLong(Constants.REPORT_TRADE_TIME_OUT, AndroidUtilities.getCurrentTime() + 60000);

        dismissDialogFragmentSupport(SafeTradeDialog.TAG);

        JobLauncher.scheduleTradeIssue(message);
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.into_anim_slide_from_left, R.anim.into_anim_slide_to_right);
    }

}