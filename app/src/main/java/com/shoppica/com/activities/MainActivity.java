package com.shoppica.com.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.shoppica.com.R;
import com.shoppica.com.constants.Constants;
import com.shoppica.com.fragments.FeedFragment;
import com.shoppica.com.fragments.ProfileFragment;
import com.shoppica.com.fragnav.FragNavController;
import com.shoppica.com.fragnav.FragmentHistory;
import com.shoppica.com.jobs.JobLauncher;
import com.shoppica.com.notifications.NotificationCenter;
import com.shoppica.com.preferences.Prefs;
import com.shoppica.com.utils.AndroidUtilities;
import com.shoppica.com.utils.ViewUtil;
import com.shoppica.com.view.RatingDialog;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindArray;
import butterknife.BindView;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import com.facebook.ads.*;

@RuntimePermissions
public class MainActivity extends BaseActivity implements FragNavController.RootFragmentListener, NotificationCenter.NotificationCenterDelegate {

    public static final String TAG = MainActivity.class.getSimpleName();

    public static final String POST_ID = "postId";
    public static final String POST_TYPE = "postType";

    private static final String BACK_STACK_POSITION = "backStackPosition";
    private static final String CURRENT_POSITION = "currentPosition";

    private FirebaseAnalytics mFirebaseAnalytics;

    @BindView(R.id.content)
    FrameLayout container;

    @BindArray(R.array.tab_name)
    String[] TABS;

    @BindView(R.id.into_bottom_tabs)
    TabLayout bottomTabLayout;

    private int mBackStackPosition;

    private int[] mTabIconsSelected = {
            R.drawable.tab_feed,
            R.drawable.tab_camera,
            R.drawable.tab_profile};

    private FragNavController mNavController;

    private FragmentHistory fragmentHistory;

    private boolean mIsFilterOpen;

    private int mCurrentPosition;


    private NativeAd nativeAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        initTab();

        initViews(savedInstanceState);

    }

    private void loadNativeAd() {
        // Instantiate a NativeAd object.
        // NOTE: the placement ID will eventually identify this as your App, you can ignore it for
        // now, while you are testing and replace it later when you have signed up.
        // While you are using this temporary code you will only get test ads and if you release
        // your code like this to the Google Play your users will not receive ads (you will get a no fill error).
        nativeAd = new NativeAd(this, "222026065252479_360521158069635");

        nativeAd.setAdListener(new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
                // Native ad finished downloading all assets
                Log.e(TAG, "Native ad finished downloading all assets.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Native ad failed to load
                Log.e(TAG, "Native ad failed to load: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Native ad is loaded and ready to be displayed
                Log.d(TAG, "Native ad is loaded and ready to be displayed!");
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Native ad clicked
                Log.d(TAG, "Native ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Native ad impression
                Log.d(TAG, "Native ad impression logged!");
            }
        });

        // Request an ad
        nativeAd.loadAd();
    }

    private void initViews(Bundle savedInstanceState) {

        fragmentHistory = new FragmentHistory();

        mNavController = FragNavController.newBuilder(savedInstanceState, getSupportFragmentManager(), R.id.content)
                .rootFragmentListener(this, 3)
                .build();


        switchTab(mCurrentPosition);

        TabLayout.Tab tab = bottomTabLayout.getTabAt(mCurrentPosition);

        if (tab != null) {
            updateTabSelection(tab.getPosition());
        }

        bottomTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getPosition() == 1) {
                    MainActivityPermissionsDispatcher.showCameraWithCheck(MainActivity.this);
                    updateTabSelectionCamera(mBackStackPosition);
                    return;
                }


                updateTabSelection(tab.getPosition());

                mBackStackPosition = tab.getPosition();

                mCurrentPosition = tab.getPosition();

                fragmentHistory.push(mBackStackPosition);

                switchTab(mBackStackPosition);

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

                if (tab.getPosition() == 1) {
                    MainActivityPermissionsDispatcher.showCameraWithCheck(MainActivity.this);
                    updateTabSelectionCamera(mBackStackPosition);
                    return;
                }


                mNavController.clearStack();

                switchTab(tab.getPosition());

                updateTabSelection(tab.getPosition());

            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (findViewById(android.R.id.content) != null) {
                findViewById(android.R.id.content).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        NotificationCenter.getInstance().addObserver(this, NotificationCenter.startCameraFromOwnPosts);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.articleJustPosted);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.recreateActivity);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.isFilterOpen);

        boolean isAlreadyRated = Prefs.getBoolean(Constants.KEY_ALREADY_RATED, false);

        if (!isAlreadyRated)
            onRateSessions();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);

        try {

            int postId = intent.getIntExtra(POST_ID, 0);
            int postType = intent.getIntExtra(POST_TYPE, 0);

            int tabPosition = bottomTabLayout.getSelectedTabPosition();

            if (tabPosition != 2) {
                updateTabSelection(2);
                mBackStackPosition = 2;
                mCurrentPosition = 2;
                fragmentHistory.push(mBackStackPosition);
                switchTab(mBackStackPosition);
            }


            NotificationCenter.getInstance().postNotificationName(NotificationCenter.setOwnPostFragmentTab);
            AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance().postNotificationName(NotificationCenter.extendFromNotification, postId, postType), 500);

        } catch (Exception e) {
            Crashlytics.logException(e);
        }

    }

    private void onRateSessions() {

        Log.d(TAG, "onRateClicked");

        final RatingDialog ratingDialog = new RatingDialog.Builder(MainActivity.this)
                .title(getString(R.string.rating_dialog_rate_title))
                .formSubmitText(getString(R.string.rating_dialog_form_submit))
                .formCancelText(getString(R.string.rating_dialog_form_cancel))
                .positiveButtonText(getString(R.string.rating_dialog_form_positive))
                .formHint(getString(R.string.rating_dialog_form_hint))
                .threshold(4)
                .session(7)
                .icon(ContextCompat.getDrawable(getApplicationContext(), R.mipmap.ic_launcher))
                .onRatingBarFormSumbit(feedback -> {

                    if (TextUtils.isEmpty(feedback)) {
                        Snackbar.make(findViewById(android.R.id.content), R.string.toast_error_report_empty, Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    if (!TextUtils.isEmpty(feedback) && feedback.length() > 5000) {
                        Snackbar.make(findViewById(android.R.id.content), R.string.snackbar_error_report_length, Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    JobLauncher.scheduleFeedBack(feedback);
                }).build();

        ratingDialog.setCancelable(false);
        ratingDialog.show();

    }

    private void updateTabSelectionCamera(int currentTab) {
        TabLayout.Tab selectedTab = bottomTabLayout.getTabAt(currentTab);
        if (selectedTab != null) {
            View view = selectedTab.getCustomView();
            if (view != null)
                view.setSelected(true);
        }

    }

    private void initTab() {
        if (bottomTabLayout != null) {
            for (int i = 0; i < TABS.length; i++) {
                bottomTabLayout.addTab(bottomTabLayout.newTab());
                TabLayout.Tab tab = bottomTabLayout.getTabAt(i);
                if (tab != null)
                    tab.setCustomView(getTabView(i));

            }
        }
    }

    private void switchTab(int position) {

        mCurrentPosition = position;


        if (mNavController != null)
            mNavController.switchTab(position);
    }


    private void updateTabSelection(int currentTab) {

        Log.d(TAG, "UPDATE TAB SELECTION == " + currentTab);

        for (int i = 0; i < TABS.length; i++) {
            TabLayout.Tab selectedTab = bottomTabLayout.getTabAt(i);

            if (currentTab == i) {
                View view = selectedTab != null ? selectedTab.getCustomView() : null;
                if (view != null) {
                    if (i != 1) {
                        view.setSelected(true);
                        View indicator = view.findViewById(R.id.tab_indicator);
                        indicator.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                View view = selectedTab != null ? selectedTab.getCustomView() : null;
                if (view != null) {
                    if (i != 1) {
                        view.setSelected(false);
                        View indicator = view.findViewById(R.id.tab_indicator);
                        indicator.setVisibility(View.GONE);
                    }
                }
            }
        }

    }

    @SuppressLint("InflateParams")
    private View getTabView(int position) {

        View view;

        if (position == 1)
            view = LayoutInflater.from(MainActivity.this).inflate(R.layout.tab_item_bottom_camera, null);
        else
            view = LayoutInflater.from(MainActivity.this).inflate(R.layout.tab_item_bottom, null);


        ImageView icon = view.findViewById(R.id.tab_icon);
        icon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), mTabIconsSelected[position]));

        return view;
    }


    @Override
    public Fragment getRootFragment(int index) {
        switch (index) {
            case FragNavController.TAB1:
                return new FeedFragment();
            case FragNavController.TAB2:
                return null;
            case FragNavController.TAB3:
                return new ProfileFragment();
        }
        return null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(BACK_STACK_POSITION, mBackStackPosition);
        outState.putInt(CURRENT_POSITION, mNavController.getCurrentStackIndex());
        if (mNavController != null) {
            mNavController.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);


    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == 200) {

                int tabPosition = bottomTabLayout.getSelectedTabPosition();

                if (tabPosition != 2) {
                    updateTabSelection(2);
                    mBackStackPosition = 2;
                    mCurrentPosition = 2;
                    fragmentHistory.push(mBackStackPosition);
                    switchTab(mBackStackPosition);
                }


                NotificationCenter.getInstance().postNotificationName(NotificationCenter.setOwnPostFragmentTab);
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshOwnPostsFragment);
                return;
            }
            if(requestCode == UCrop.REQUEST_CROP){
                final Uri resultUri = UCrop.getOutput(data);
                ArrayList<String> fileNames = new ArrayList<>();
                fileNames.add(resultUri.getPath());
                Intent intent = new Intent(getApplicationContext(), EditPhotoActivity.class);
                intent.putStringArrayListExtra(EditPhotoActivity.PHOTOS, fileNames);
                startActivityForResult(intent, 200);
                overridePendingTransition(R.anim.into_anim_slide_from_right, R.anim.into_anim_slide_to_left);
            }else if (resultCode == UCrop.RESULT_ERROR) {
                final Throwable cropError = UCrop.getError(data);
            }

        }

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {

                onError(getString(R.string.alert_error_title), getString(R.string.alert_error_desc));

                Crashlytics.logException(e);
            }

            @Override
            public void onImagesPicked(List<File> imageFiles, EasyImage.ImageSource source, int type) {

                if (imageFiles.size() > 5) {
                    Snackbar.make(findViewById(android.R.id.content), R.string.snackbar_error_max_5_images, Snackbar.LENGTH_LONG).show();
                    return;
                }

                ArrayList<String> fileNames = new ArrayList<>();

                for (int i = 0; i < imageFiles.size(); i++) {

                    File file = imageFiles.get(i);

                    if (file == null)
                        continue;

                    fileNames.add(file.getAbsolutePath());
                }

                if(source == EasyImage.ImageSource.CAMERA){
                    String destinationFileName = "cropped" + UUID.randomUUID().toString() + ".jpg";
                    UCrop.Options options = new UCrop.Options();
                    options.setStatusBarColor(getResources().getColor(R.color.colorAccent));
                    options.setToolbarColor(getResources().getColor(R.color.colorAccent));
                    options.setActiveWidgetColor(getResources().getColor(R.color.colorAccent));

                    UCrop uCrop = UCrop.of(Uri.fromFile(imageFiles.get(0)), Uri.fromFile(new File(getCacheDir(), destinationFileName)));
                    uCrop.withOptions(options)
                            .start(MainActivity.this);
                }else{
                    Intent intent = new Intent(getApplicationContext(), EditPhotoActivity.class);
                    intent.putStringArrayListExtra(EditPhotoActivity.PHOTOS, fileNames);
                    startActivityForResult(intent, 200);
                    overridePendingTransition(R.anim.into_anim_slide_from_right, R.anim.into_anim_slide_to_left);
                }
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(MainActivity.this);
                    if (photoFile != null) photoFile.delete();
                }
            }
        });
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

        builder.setCancelable(false);
        builder.show();
    }


    private void showCaptureChoice() {
        String[] items;
        if (!EasyImage.canDeviceHandleGallery(this))
            items = new String[]{getString(R.string.option_camera)};
        else
            items = new String[]{getString(R.string.option_camera), getString(R.string.option_gallery)};


        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setItems(items, (dialog, which) -> {


            switch (which) {
                case 0:

                    EasyImage.openCamera(this, 0);
                    break;

                case 1:
                    EasyImage.openGallery(this, 0);

                    break;


            }

        });
        builder.show();


    }


    @NeedsPermission(Manifest.permission.CAMERA)
    void showCamera() {
        MainActivityPermissionsDispatcher.continueWithStoragePermissionWithCheck(MainActivity.this);
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    void showRationaleForCamera(final PermissionRequest request) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);

        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.into_alert_view, null);

        builder.setView(view);

        TextView title = ViewUtil.findById(view, R.id.into_title_error);
        title.setText(getString(R.string.alert_default_permission_title));

        TextView desc = ViewUtil.findById(view, R.id.into_desc_error);
        desc.setText(getString(R.string.alert_camera_permission_desc));

        builder.setPositiveButton(R.string.btn_action_allow, (dialog, which) -> request.proceed());
        builder.setNegativeButton(R.string.btn_action_deny, (dialog, which) -> request.cancel());


        AlertDialog dialog = builder.create();
        dialog.show();
        //for negative side button
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.alertCancelBtn));

    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    void showDeniedForCamera() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.into_alert_view, null);
        builder.setView(view);
        TextView title = ViewUtil.findById(view, R.id.into_title_error);
        title.setText(getString(R.string.alert_default_permission_denied_title));
        TextView desc = ViewUtil.findById(view, R.id.into_desc_error);
        desc.setText(getString(R.string.alert_camera_permission_desc));
        builder.setPositiveButton(R.string.btn_action_ok, null);
        builder.show();
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    void showNeverAskForCamera() {


        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.into_alert_view, null);
        builder.setView(view);
        TextView title = ViewUtil.findById(view, R.id.into_title_error);
        title.setText(getString(R.string.alert_default_permission_denied_title));
        TextView desc = ViewUtil.findById(view, R.id.into_desc_error);
        desc.setText(getString(R.string.alert_camera_permission_never_desc));
        builder.setPositiveButton(R.string.btn_action_settings, (dialog, which) -> {

            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        });
        builder.setNegativeButton(R.string.btn_action_cancel, null);

        AlertDialog dialog = builder.create();
        dialog.show();
        //for negative side button
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.alertCancelBtn));

    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void continueWithStoragePermission() {

        boolean loggedIn = AccessToken.getCurrentAccessToken() != null;

        if (!loggedIn) {
            Intent intent = new Intent(getApplicationContext(), FacebookExplainActivity.class);
            startActivityForResult(intent, 200);
            overridePendingTransition(R.anim.slide_bottom_in, R.anim.stay);
            return;
        }

        showCaptureChoice();


    }

    @OnShowRationale({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showRationaleForGallery(final PermissionRequest request) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);

        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.into_alert_view, null);

        builder.setView(view);

        TextView title = ViewUtil.findById(view, R.id.into_title_error);
        title.setText(getString(R.string.alert_default_permission_title));

        TextView desc = ViewUtil.findById(view, R.id.into_desc_error);
        desc.setText(getString(R.string.alert_gallery_permission_desc));

        builder.setPositiveButton(R.string.btn_action_allow, (dialog, which) -> request.proceed());
        builder.setNegativeButton(R.string.btn_action_deny, (dialog, which) -> request.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
        //for negative side button
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.alertCancelBtn));

    }

    @OnPermissionDenied({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showDeniedForGallery() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.into_alert_view, null);
        builder.setView(view);
        TextView title = ViewUtil.findById(view, R.id.into_title_error);
        title.setText(getString(R.string.alert_default_permission_denied_title));
        TextView desc = ViewUtil.findById(view, R.id.into_desc_error);
        desc.setText(getString(R.string.alert_gallery_permission_desc));
        builder.setPositiveButton(R.string.btn_action_ok, null);
        builder.show();

    }

    @OnNeverAskAgain({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showNeverAskForGallery() {


        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.into_alert_view, null);
        builder.setView(view);
        TextView title = ViewUtil.findById(view, R.id.into_title_error);
        title.setText(getString(R.string.alert_default_permission_denied_title));
        TextView desc = ViewUtil.findById(view, R.id.into_desc_error);
        desc.setText(getString(R.string.alert_gallery_permission_never_desc));
        builder.setPositiveButton(R.string.btn_action_settings, (dialog, which) -> {

            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        });
        builder.setNegativeButton(R.string.btn_action_cancel, null);

        AlertDialog dialog = builder.create();
        dialog.show();
        //for negative side button
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.alertCancelBtn));

    }


    @Override
    public void onBackPressed() {


        if (mIsFilterOpen) {
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeFilter);
            mIsFilterOpen = false;
            return;
        }

        if (!mNavController.isRootFragment()) {
            Log.d(TAG, "IS ROOT FRAGMENT");
            mNavController.popFragment();
        } else {

            if (fragmentHistory.isEmpty()) {

                if (mCurrentPosition != 0) {
                    switchTab(0);
                    updateTabSelection(0);

                } else
                    super.onBackPressed();

                Log.d(TAG, "HISTORY IS EMPTY");
            } else {

                if (fragmentHistory.getStackSize() > 1) {

                    int position = fragmentHistory.popPrevious();

                    switchTab(position);

                    updateTabSelection(position);
                    Log.d(TAG, "FRAGMENT HISTORY STACKSIZE > one");
                } else {

                    switchTab(0);

                    updateTabSelection(0);

                    fragmentHistory.emptyStack();

                    Log.d(TAG, "EMPTY STACK");
                }
            }

        }
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {

        if (id == NotificationCenter.startCameraFromOwnPosts) {

            MainActivityPermissionsDispatcher.showCameraWithCheck(MainActivity.this);

        } else if (id == NotificationCenter.recreateActivity) {
            recreate();
        } else if (id == NotificationCenter.isFilterOpen) {
            mIsFilterOpen = (boolean) args[0];
        }

    }

    @Override
    protected void onDestroy() {

        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.startCameraFromOwnPosts);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.articleJustPosted);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.recreateActivity);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.isFilterOpen);
        super.onDestroy();
    }
}
