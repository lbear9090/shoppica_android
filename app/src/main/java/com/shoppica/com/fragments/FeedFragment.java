package com.shoppica.com.fragments;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.shoppica.com.R;
import com.shoppica.com.constants.Constants;
import com.shoppica.com.jobs.JobLauncher;
import com.shoppica.com.notifications.NotificationCenter;
import com.shoppica.com.preferences.Prefs;
import com.shoppica.com.rangeseekbar.widgets.CrystalRangeSeekbar;
import com.shoppica.com.rangeseekbar.widgets.CrystalSeekbar;
import com.shoppica.com.utils.AndroidUtilities;
import com.shoppica.com.utils.ViewUtil;
import com.shoppica.com.utils.typeface.SpannableBuilder;
import com.shoppica.com.view.RatingDialog;
import com.shoppica.com.view.SingleShotLocationProvider;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class FeedFragment extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    public static final String TAG = FeedFragment.class.getSimpleName();

    public static final String BOLD = "fonts/MONTSERRAT-BOLD.TTF";
    public static final String MEDIUM = "fonts/MONTSERRAT-MEDIUM.TTF";

    private static final int CATEGORY_STANDARD_POSITION = 35;
    private static final int CATEGORY_GENDER_POSITION = 4;
    private static final int CATEGORY_CONDITION_POSITION = 3;
    private static final int CATEGORY_COLOR_POSITION = 10;
    private static final int CATEGORY_SEND_POSITION = 3;

    @BindView(R.id.filterView)
    ConstraintLayout mFilterView;

    @BindView(R.id.toolbar_name)
    ImageView mToolbarName;

    @BindView(R.id.toolbar_shadow)
    View mToolbarShadow;

    @BindView(R.id.filter)
    ImageButton mFilter;

    @BindView(R.id.present)
    ImageButton mPresent;

    @BindView(R.id.gender)
    TextView mGender;

    @BindView(R.id.category)
    TextView mCategory;

    @BindView(R.id.condition)
    TextView mCondition;

    @BindView(R.id.color)
    TextView mColor;

    @BindView(R.id.send)
    TextView mSend;

    @BindView(R.id.priceBar)
    CrystalRangeSeekbar mPriceBar;

    @BindView(R.id.priceValue)
    TextView mPriceValue;

    @BindView(R.id.distanceBar)
    CrystalSeekbar mDistanceBar;

    @BindView(R.id.distanceValue)
    TextView mDistanceValue;

    @BindView(R.id.cancel_action)
    ImageView mCancel;

    @BindView(R.id.bg_filter)
    View mBgFilter;

    @BindView(R.id.bottomTabs)
    TabLayout mTabLayout;

    @BindView(R.id.pager)
    ViewPager mPager;


    private Context mContext;

    private Animator animator;

    private int mHeight, mRight, mLeft;

    private int mPosCategory = -1, mPosGender = -1, mPosCondition = -1, mPosColor = -1, mPosSend = -1, mFilterDistance, mFilterPriceMinimum, mFilterPriceMaximum;

    private float mFilterLatitude, mFilterLongitude;

    private ProgressDialog mProgressDialog;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context.getApplicationContext();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initTabs();

        initViews();

        initDrawable();

        initProgressDialog();
    }

    private void initTabs() {


        ViewPagerAdapter mAdapter = new ViewPagerAdapter(getChildFragmentManager());
        mPager.setOffscreenPageLimit(3);

        mPager.setAdapter(mAdapter);

        mTabLayout.setupWithViewPager(mPager);

        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            if (tab != null)
                tab.setCustomView(mAdapter.getTabView(i));
        }


        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                mPager.setCurrentItem(tab.getPosition());

                if (tab.getPosition() == 0) {
                    if (!Prefs.getBoolean(Constants.KEY_FILTER_TRENDING_LOADED, false)) {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTrendingFragment);
                        Prefs.putBoolean(Constants.KEY_FILTER_TRENDING_LOADED, true);
                    }

                    if (!Prefs.getBoolean(Constants.KEY_RESET_TRENDING_LOADED, false)) {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTrendingFragment);
                        Prefs.putBoolean(Constants.KEY_RESET_TRENDING_LOADED, true);
                    }
                }

                if (tab.getPosition() == 1) {
                    if (!Prefs.getBoolean(Constants.KEY_FILTER_DISCOVER_LOADED, false)) {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshDiscoverFragment);
                        Prefs.putBoolean(Constants.KEY_FILTER_DISCOVER_LOADED, true);
                    }

                    if (!Prefs.getBoolean(Constants.KEY_RESET_DISCOVER_LOADED, false)) {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshDiscoverFragment);
                        Prefs.putBoolean(Constants.KEY_RESET_DISCOVER_LOADED, true);
                    }
                }

                if (tab.getPosition() == 2) {
                    if (!Prefs.getBoolean(Constants.KEY_FILTER_PROMOTED_LOADED, false)) {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshPromotedFragment);
                        Prefs.putBoolean(Constants.KEY_FILTER_PROMOTED_LOADED, true);
                    }

                    if (!Prefs.getBoolean(Constants.KEY_RESET_PROMOTED_LOADED, false)) {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshPromotedFragment);
                        Prefs.putBoolean(Constants.KEY_RESET_PROMOTED_LOADED, true);
                    }
                }


            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    private void initViews() {


        mFilterView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        // gets called after layout has been done but before display
                        // so we can get the height then hide the view
                        mHeight = mFilterView.getHeight();
                        mLeft = mFilterView.getLeft();
                        mRight = mFilterView.getRight();

                        mFilterView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mFilterView.setVisibility(View.GONE);
                    }

                });


        mPosGender = Prefs.getInt(Constants.KEY_FILTER_GENDER, -1);

        String[] gender = getResources().getStringArray(R.array.gender_filter);
        if (mPosGender != -1 && mPosGender != CATEGORY_GENDER_POSITION)
            mGender.setText(gender[mPosGender]);

        int arrayChoice = R.array.categories_filter;

        String[] category = getResources().getStringArray(arrayChoice);

        mPosCategory = Prefs.getInt(Constants.KEY_FILTER_CATEGORY, -1);
        if (mPosCategory != -1) {

            if (mPosCategory != CATEGORY_STANDARD_POSITION)
                mCategory.setText(category[mPosCategory]);
        }


        String[] condition = getResources().getStringArray(R.array.condition_filter);


        mPosCondition = Prefs.getInt(Constants.KEY_FILTER_CONDITION, -1);
        if (mPosCondition != -1 && mPosCondition != CATEGORY_CONDITION_POSITION)
            mCondition.setText(condition[mPosCondition]);


        mPosColor = Prefs.getInt(Constants.KEY_FILTER_COLOR, -1);

        String[] color = getResources().getStringArray(R.array.color_filter);
        if (mPosColor != -1 && mPosColor != CATEGORY_COLOR_POSITION) {
            mColor.setText(color[mPosColor]);
        }
        String[] send = getResources().getStringArray(R.array.send_filter);
        mPosSend = Prefs.getInt(Constants.KEY_FILTER_SEND, -1);

        if (mPosSend != -1 && mPosSend != CATEGORY_SEND_POSITION)
            mSend.setText(send[mPosSend]);

        mFilterPriceMinimum = Prefs.getInt(Constants.KEY_FILTER_PRICE_BAR_MIN, 0);
        mFilterPriceMaximum = Prefs.getInt(Constants.KEY_FILTER_PRICE_BAR_MAX, 0);

        String countryCode = Prefs.getString(Constants.KEY_REGISTER_COUNTRY_CODE, Constants.NL);
        String currency;

        switch (countryCode.toLowerCase()) {
            case Constants.AU:
                currency = "A$";
                break;
            case Constants.NZ:
                currency = "NZ$";
                break;
            case Constants.BE:
            case Constants.NL:
                currency = "€";
                break;
            case Constants.GB:
            case Constants.IE:
                currency = "£";
                break;
            case Constants.HK:
                currency = "HK$";
                break;
            default:
                currency = "$";
                break;
        }


        if (mFilterPriceMinimum == 0 && mFilterPriceMaximum == 0)
            mPriceValue.setText(getString(R.string.filter_article_price));
        else
            mPriceValue.setText(getString(R.string.filter_article_price_value, currency, mFilterPriceMinimum, currency, mFilterPriceMaximum));

        mPriceBar.setMinValue(0);
        mPriceBar.setMaxValue(10000);
        mPriceBar.setMinStartValue(mFilterPriceMinimum);
        mPriceBar.setMaxStartValue(mFilterPriceMaximum);
        mPriceBar.apply();
        mPriceBar.setOnRangeSeekbarChangeListener((minValue, maxValue) -> {

            Prefs.putInt(Constants.KEY_FILTER_PRICE_BAR_MIN, minValue.intValue());
            Prefs.putInt(Constants.KEY_FILTER_PRICE_BAR_MAX, maxValue.intValue());

            mFilterPriceMinimum = minValue.intValue();
            mFilterPriceMaximum = maxValue.intValue();

            if (minValue.intValue() == 0 && maxValue.intValue() == 0)
                mPriceValue.setText(getString(R.string.filter_article_price));
            else {
                String currency2;

                switch (countryCode.toLowerCase()) {
                    case Constants.AU:
                        currency2 = "A$";
                        break;
                    case Constants.NZ:
                        currency2 = "NZ$";
                        break;
                    case Constants.BE:
                    case Constants.NL:
                        currency2 = "€";
                        break;
                    case Constants.GB:
                    case Constants.IE:
                        currency2 = "£";
                        break;
                    case Constants.HK:
                        currency2 = "HK$";
                        break;
                    default:
                        currency2 = "$";
                        break;
                }
                mPriceValue.setText(getString(R.string.filter_article_price_value, currency2, minValue.intValue(), currency2, maxValue.intValue()));

            }

        });

        mFilterDistance = Prefs.getInt(Constants.KEY_FILTER_DISTANCE_BAR, 0);

        if (mFilterDistance == 0)
            mDistanceValue.setText(getString(R.string.filter_article_distance));
        else
            mDistanceValue.setText(getString(R.string.filter_article_distance_value, mFilterDistance));

        mDistanceBar.setMinValue(0);
        mDistanceBar.setMaxValue(250);
        mDistanceBar.setMinStartValue(mFilterDistance);
        mDistanceBar.apply();
        mDistanceBar.setOnSeekbarChangeListener(value -> {

            mFilterDistance = value.intValue();

            Prefs.putInt(Constants.KEY_FILTER_DISTANCE_BAR, value.intValue());

            if (value.intValue() == 0)
                mDistanceValue.setText(getString(R.string.filter_article_distance));
            else
                mDistanceValue.setText(getString(R.string.filter_article_distance_value, value.intValue()));

        });

        mFilterLatitude = Prefs.getFloat(Constants.KEY_FILTER_LOCATION_LATITUDE, 0);
        mFilterLongitude = Prefs.getFloat(Constants.KEY_FILTER_LOCATION_LONGITUDE, 0);

        boolean isActive = Prefs.getBoolean(Constants.KEY_FILTER_ACTIVE, false);
        if (isActive)
            mFilter.setImageResource(R.drawable.ic_filter_active);
        else
            mFilter.setImageResource(R.drawable.ic_filter);

        NotificationCenter.getInstance().addObserver(this,NotificationCenter.closeFilter);

    }


    private void initDrawable() {

        Drawable filterDrawable = AndroidUtilities.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(48), ContextCompat.getColor(mContext,
                R.color.transparent), ContextCompat.getColor(mContext, R.color.colorFilterPressed));

        Drawable presentDrawable = AndroidUtilities.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(48), ContextCompat.getColor(mContext,
                R.color.transparent), ContextCompat.getColor(mContext, R.color.colorFilterPressed));

        Drawable cancelDrawable = AndroidUtilities.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(48), ContextCompat.getColor(mContext,
                R.color.transparent), ContextCompat.getColor(mContext, R.color.white_50));

        mPresent.setBackground(presentDrawable);
        mFilter.setBackground(filterDrawable);
        mCancel.setBackground(cancelDrawable);


    }


    private void initProgressDialog() {
        mProgressDialog = new ProgressDialog(getActivity(), R.style.into_progress_style);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setMessage(getString(R.string.alet_progress_searching));
        mProgressDialog.setCancelable(false);
    }


    @SuppressLint("ClickableViewAccessibility")
    @OnClick(R.id.filter)
    void onFilterClicked() {


        animator = io.codetail.animation.ViewAnimationUtils.createCircularReveal(mFilterView, mRight, 0, mLeft, mHeight);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(200);
        animator.start();
        mFilterView.setVisibility(View.VISIBLE);
        mBgFilter.setVisibility(View.VISIBLE);
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.isFilterOpen, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            Objects.requireNonNull(getActivity()).getWindow().setStatusBarColor(ContextCompat.getColor(mContext, R.color.colorAccentPressed));


    }


    @OnClick(R.id.present)
    void onPresentClicked() {
        onRateDialog();
    }


    private void onRateDialog() {


        boolean isFollow = Prefs.getBoolean(Constants.KEY_ALREADY_RATED, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()), R.style.DialogTheme);
        View view = LayoutInflater.from(getActivity()).inflate(isFollow ? R.layout.dialog_view_rate2 : R.layout.dialog_view_rate, null);

        TextView textView = view.findViewById(R.id.insta_follow_desc);

        if (Build.VERSION.SDK_INT >= 21) {
            ImageView imageView = view.findViewById(R.id.liveEmoji);
            AnimatedVectorDrawable drawable = (AnimatedVectorDrawable) imageView.getDrawable();
            if (drawable == null) {
                return;
            }

            drawable.start();

        }
        // Call some material design APIs here

        SpannableBuilder spannableBuilder = new SpannableBuilder(mContext, text -> {

            if (!AndroidUtilities.isPackageInstalled(Constants.PACKAGE_NAME_INSTAGRAM, getActivity().getPackageManager())) {
                onError(getString(R.string.view_article_not_installed_error_instagram_title), getString(R.string.view_article_not_installed_error_instagram_desc), Constants.PACKAGE_NAME_INSTAGRAM);
                return;
            }

            Uri uri = Uri.parse("http://instagram.com/_u/" + getString(R.string.spannable_follow_username));
            Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

            likeIng.setPackage("com.instagram.android");

            try {
                startActivity(likeIng);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://instagram.com/" + getString(R.string.spannable_follow_username))));
            }
        });
        spannableBuilder.append(getString(R.string.spannable_follow_us_title));
        spannableBuilder.append(" ");
        spannableBuilder.createStyle().setFont("fonts/MONTSERRAT-SEMIBOLD.TTF").setColor(ContextCompat.getColor(mContext, R.color.colorAccent)).apply();
        spannableBuilder.append("@" + getString(R.string.spannable_follow_username), this);
        spannableBuilder.clearStyle();
        spannableBuilder.append(" ");
        spannableBuilder.append(getString(R.string.spannable_follow_us_desc));

        textView.setText(spannableBuilder.build());
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);

        builder.setView(view);
        builder.setPositiveButton(isFollow ? R.string.btn_action_follow : R.string.btn_action_rate, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (isFollow) {
                    if (!AndroidUtilities.isPackageInstalled(Constants.PACKAGE_NAME_INSTAGRAM, getActivity().getPackageManager())) {
                        onError(getString(R.string.view_article_not_installed_error_instagram_title), getString(R.string.view_article_not_installed_error_instagram_desc), Constants.PACKAGE_NAME_INSTAGRAM);
                        return;
                    }

                    Uri uri = Uri.parse("http://instagram.com/_u/" + getString(R.string.spannable_follow_username));
                    Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

                    likeIng.setPackage("com.instagram.android");

                    try {
                        startActivity(likeIng);
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://instagram.com/" + getString(R.string.spannable_follow_username))));
                    }
                    return;
                }

                onRateClicked();
            }
        });
        builder.setNegativeButton(R.string.btn_action_cancel, null);
        builder.show();


    }

    private void onRateClicked() {

        Log.d(TAG, "onRateClicked");

        final RatingDialog ratingDialog = new RatingDialog.Builder(Objects.requireNonNull(getActivity()))
                .title(getString(R.string.rating_dialog_rate_title))
                .formSubmitText(getString(R.string.rating_dialog_form_submit))
                .formCancelText(getString(R.string.rating_dialog_form_cancel))
                .positiveButtonText(getString(R.string.rating_dialog_form_positive))
                .formHint(getString(R.string.rating_dialog_form_hint))
                .threshold(4)
                .icon(ContextCompat.getDrawable(mContext, R.mipmap.ic_launcher))
                .onRatingBarFormSumbit(feedback -> {
                    if (TextUtils.isEmpty(feedback)) {
                        Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.toast_error_report_empty, Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    if (!TextUtils.isEmpty(feedback) && feedback.length() > 5000) {
                        Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.snackbar_error_report_length, Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    JobLauncher.scheduleFeedBack(feedback);
                }).build();

        ratingDialog.setCancelable(false);
        ratingDialog.show();

    }

    private void openPlayStore(Context context, String packageNasme) {
        final Uri marketUri = Uri.parse("market://details?id=" + packageNasme);
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, marketUri));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "Couldn't find PlayStore on this device", Toast.LENGTH_SHORT).show();
        }
    }

    private void onError(String titleMsg, String errorMsg, String packageName) {

        @SuppressLint("InflateParams") View view = LayoutInflater.from(getActivity()).inflate(R.layout.into_alert_view, null);

        TextView errorTitle = ViewUtil.findById(view, R.id.into_title_error);
        TextView errorText = ViewUtil.findById(view, R.id.into_desc_error);

        if (!TextUtils.isEmpty(titleMsg))
            errorTitle.setText(titleMsg);

        if (!TextUtils.isEmpty(errorMsg))
            errorText.setText(errorMsg);

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()), R.style.DialogTheme);
        builder.setView(view);

        builder.setPositiveButton(R.string.btn_action_install, (dialogInterface, i) -> openPlayStore(mContext, packageName));
        builder.setNegativeButton(R.string.btn_action_cancel, null);

        builder.setCancelable(false);
        builder.show();
    }


    private void onErrorLocation(String titleMsg, String errorMsg) {

        @SuppressLint("InflateParams") View view = LayoutInflater.from(getActivity()).inflate(R.layout.into_alert_view, null);

        TextView errorTitle = ViewUtil.findById(view, R.id.into_title_error);
        TextView errorText = ViewUtil.findById(view, R.id.into_desc_error);

        if (!TextUtils.isEmpty(titleMsg))
            errorTitle.setText(titleMsg);

        if (!TextUtils.isEmpty(errorMsg))
            errorText.setText(errorMsg);

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()), R.style.DialogTheme);
        builder.setView(view);

        builder.setPositiveButton(R.string.btn_action_ok, (dialogInterface, i) -> {

            FeedFragmentPermissionsDispatcher.continueLocationPermissionWithCheck(this);

        });
        builder.setNegativeButton(R.string.btn_action_cancel, null);
        builder.show();
    }


    @OnClick(R.id.cancel_action)
    void onCancelClicked() {

        animator = io.codetail.animation.ViewAnimationUtils.createCircularReveal(mFilterView, mLeft, 0, mRight, 0);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(200);
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mFilterView.setVisibility(View.GONE);
                mBgFilter.setVisibility(View.GONE);
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.isFilterOpen, false);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    Objects.requireNonNull(getActivity()).getWindow().setStatusBarColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }

    @OnClick(R.id.resetBtn)
    void onResetClicked() {

        mPosGender = -1;
        mPosCategory = -1;
        mPosCondition = -1;
        mPosColor = -1;
        mPosSend = -1;
        mFilterDistance = 0;
        mFilterPriceMinimum = 0;
        mFilterPriceMaximum = 0;
        mFilterLongitude = 0;
        mFilterLatitude = 0;

        mGender.setText(getString(R.string.filter_article_gender));
        mCategory.setText(getString(R.string.filter_article_category));
        mCondition.setText(getString(R.string.filter_article_condition));
        mColor.setText(getString(R.string.filter_article_color));
        mSend.setText(getString(R.string.filter_article_send));

        Prefs.remove(Constants.KEY_FILTER_GENDER);
        Prefs.remove(Constants.KEY_FILTER_CATEGORY);
        Prefs.remove(Constants.KEY_FILTER_CONDITION);
        Prefs.remove(Constants.KEY_FILTER_GENDER);
        Prefs.remove(Constants.KEY_FILTER_COLOR);
        Prefs.remove(Constants.KEY_FILTER_SEND);
        Prefs.remove(Constants.KEY_FILTER_PRICE_BAR_MIN);
        Prefs.remove(Constants.KEY_FILTER_PRICE_BAR_MAX);
        Prefs.remove(Constants.KEY_FILTER_DISTANCE_BAR);
        Prefs.remove(Constants.KEY_FILTER_ACTIVE);

        mFilter.setImageResource(R.drawable.ic_filter);

        mPriceValue.setText(getString(R.string.filter_article_price));
        mDistanceValue.setText(getString(R.string.filter_article_distance));

        mPriceBar.setMinValue(0);
        mPriceBar.setMaxValue(10000);
        mPriceBar.setMinStartValue(0);
        mPriceBar.setMaxStartValue(0);
        mPriceBar.apply();

        mDistanceBar.setMinValue(0);
        mDistanceBar.setMaxValue(250);
        mDistanceBar.setMinStartValue(0);
        mDistanceBar.apply();

        onCancelClicked();

        int tabPosition = mTabLayout.getSelectedTabPosition();

        if (tabPosition == 0) {
            Prefs.putBoolean(Constants.KEY_RESET_TRENDING_LOADED, true);
            Prefs.putBoolean(Constants.KEY_RESET_DISCOVER_LOADED, false);
            Prefs.putBoolean(Constants.KEY_RESET_PROMOTED_LOADED, false);
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTrendingFragment);
        } else if (tabPosition == 1) {
            Prefs.putBoolean(Constants.KEY_RESET_DISCOVER_LOADED, true);
            Prefs.putBoolean(Constants.KEY_RESET_TRENDING_LOADED, false);
            Prefs.putBoolean(Constants.KEY_RESET_PROMOTED_LOADED, false);
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshDiscoverFragment);
        } else {
            Prefs.putBoolean(Constants.KEY_RESET_PROMOTED_LOADED, true);
            Prefs.putBoolean(Constants.KEY_RESET_TRENDING_LOADED, false);
            Prefs.putBoolean(Constants.KEY_RESET_DISCOVER_LOADED, false);
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshPromotedFragment);
        }


    }


    @OnClick(R.id.filterBtn)
    void onFilterSearch() {


        if (mFilterDistance == 0 &&
                mFilterPriceMaximum == 0 &&
                mFilterPriceMinimum == 0 &&
                mPosGender == -1 && mPosCategory == -1
                && mPosCondition == -1
                && mPosColor == -1
                && mPosSend == -1) {

            Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content), R.string.snackbar_error_filter_no_value_changed, Snackbar.LENGTH_SHORT).show();

            return;
        }

        Prefs.putBoolean(Constants.KEY_FILTER_ACTIVE, true);
        mFilter.setImageResource(R.drawable.ic_filter_active);

        Log.d(TAG, "latitude == " + mFilterLatitude);
        Log.d(TAG, "longitude == " + mFilterLongitude);
        Log.d(TAG, "mFilterDistance == " + mFilterDistance);

        if ((mFilterLatitude == 0 || mFilterLongitude == 0) && mFilterDistance > 0) {
            onErrorLocation(getString(R.string.alert_location_error_title), getString(R.string.alert_location_error_desc2));
            return;
        }

        onCancelClicked();

        int tabPosition = mTabLayout.getSelectedTabPosition();

        if (tabPosition == 0) {
            Prefs.putBoolean(Constants.KEY_FILTER_TRENDING_LOADED, true);
            Prefs.putBoolean(Constants.KEY_FILTER_DISCOVER_LOADED, false);
            Prefs.putBoolean(Constants.KEY_FILTER_PROMOTED_LOADED, false);
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTrendingFragment);
        } else if (tabPosition == 1) {
            Prefs.putBoolean(Constants.KEY_FILTER_DISCOVER_LOADED, true);
            Prefs.putBoolean(Constants.KEY_FILTER_TRENDING_LOADED, false);
            Prefs.putBoolean(Constants.KEY_FILTER_PROMOTED_LOADED, false);
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshDiscoverFragment);
        } else {
            Prefs.putBoolean(Constants.KEY_FILTER_PROMOTED_LOADED, true);
            Prefs.putBoolean(Constants.KEY_FILTER_TRENDING_LOADED, false);
            Prefs.putBoolean(Constants.KEY_FILTER_DISCOVER_LOADED, false);
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshPromotedFragment);
        }

    }

    private void showDialog(String title, String[] items, int type) {

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()), R.style.DialogTheme);
        mBuilder.setTitle(title);

        int checked = -1;

        if (type == 1)
            checked = mPosGender;
        else if (type == 2)
            checked = mPosCategory;
        else if (type == 3)
            checked = mPosCondition;
        else if (type == 4)
            checked = mPosColor;
        else if (type == 5)
            checked = mPosSend;

        mBuilder.setSingleChoiceItems(items, checked, (dialogInterface, i) -> {

            if (type == 1) {
                mGender.setText(i == CATEGORY_GENDER_POSITION ? getString(R.string.filter_article_gender) : items[i]);
                mPosGender = i;
                Prefs.putInt(Constants.KEY_FILTER_GENDER, i);
                mPosCategory = -1;
                mCategory.setText(getString(R.string.alert_add_category));
                Prefs.remove(Constants.KEY_FILTER_CATEGORY);
            } else if (type == 2) {

                mCategory.setText(i == CATEGORY_STANDARD_POSITION ? getString(R.string.filter_article_category) : items[i]);
                mPosCategory = i;
                Prefs.putInt(Constants.KEY_FILTER_CATEGORY, i);
            } else if (type == 3) {
                mCondition.setText(i == CATEGORY_CONDITION_POSITION ? getString(R.string.filter_article_condition) : items[i]);
                mPosCondition = i;
                Prefs.putInt(Constants.KEY_FILTER_CONDITION, i);
            } else if (type == 4) {
                mColor.setText(i == CATEGORY_COLOR_POSITION ? getString(R.string.filter_article_color) : items[i]);
                mPosColor = i;
                Prefs.putInt(Constants.KEY_FILTER_COLOR, i);
            } else if (type == 5) {
                mSend.setText(i == CATEGORY_SEND_POSITION ? getString(R.string.filter_article_send) : items[i]);
                mPosSend = i;
                Prefs.putInt(Constants.KEY_FILTER_SEND, i);
            }


            dialogInterface.dismiss();
        });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();

    }


    @OnClick(R.id.gender)
    void onGenderClicked() {

        String[] gender = getResources().getStringArray(R.array.gender_filter);
        showDialog(getString(R.string.alert_gender_title), gender, 1);

    }

    @OnClick(R.id.category)
    void onCategoryClicked() {

        mPosGender = Prefs.getInt(Constants.KEY_FILTER_GENDER, -1);

        if (mPosGender == -1)
            mPosGender = Prefs.getInt(Constants.KEY_REGISTER_GENDER, 0);

        int arrayChoice = R.array.categories_filter;

        String[] category = getResources().getStringArray(arrayChoice);

        showDialog(getString(R.string.alert_category_title), category, 2);

    }

    @OnClick(R.id.condition)
    void onConditionClicked() {

        String[] condition = getResources().getStringArray(R.array.condition_filter);
        showDialog(getString(R.string.alert_condition_title), condition, 3);

    }


    @OnClick(R.id.color)
    void onColorClicked() {

        String[] color = getResources().getStringArray(R.array.color_filter);
        showDialog(getString(R.string.alert_color_title), color, 4);

    }

    @OnClick(R.id.send)
    void onSendClicked() {
        String[] send = getResources().getStringArray(R.array.send_filter);
        showDialog(getString(R.string.alert_send_title), send, 5);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        FeedFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);

    }


    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void continueLocationPermission() {

        if (mProgressDialog != null)
            mProgressDialog.show();

        SingleShotLocationProvider.requestSingleUpdate(Objects.requireNonNull(getActivity()), location -> {

            try {
                mFilterLatitude = location.latitude;
                mFilterLongitude = location.longitude;

                if (mFilterLatitude != 0 || mFilterLongitude != 0) {
                    Prefs.putFloat(Constants.KEY_FILTER_LOCATION_LATITUDE, mFilterLatitude);
                    Prefs.putFloat(Constants.KEY_FILTER_LOCATION_LONGITUDE, mFilterLongitude);

                    onFilterSearch();
                }

            } catch (Exception e) {
                Crashlytics.logException(e);
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.alert_location_error_desc_3), Snackbar.LENGTH_SHORT).show();
            }

            if (mProgressDialog != null)
                mProgressDialog.dismiss();

        });

    }


    @OnPermissionDenied({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void showDeniedForLocation() {


        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()), R.style.DialogTheme);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getActivity()).inflate(R.layout.into_alert_view, null);
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


        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()), R.style.DialogTheme);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getActivity()).inflate(R.layout.into_alert_view, null);
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
                Uri uri = Uri.fromParts("package", mContext.getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);

            }
        });
        builder.setNegativeButton(R.string.btn_action_cancel, null);

        AlertDialog dialog = builder.create();
        dialog.show();
        //for negative side button
        dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(mContext, R.color.alertCancelBtn));


    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if(id == NotificationCenter.closeFilter){
            onCancelClicked();
        }
    }

    public class ViewPagerAdapter extends FragmentStatePagerAdapter {

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }


        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new TrendingFragment();
                case 1:
                    return new DiscoverFragment();
                default:
                    return new PromotedFragment();
            }
        }

        @SuppressLint("InflateParams")
        View getTabView(int position) {
            View v;

            switch (position) {
                case 0:
                    v = LayoutInflater.from(getActivity()).inflate(R.layout.tab_item_feed_1, null);
                    break;
                case 1:
                    v = LayoutInflater.from(getActivity()).inflate(R.layout.tab_item_feed_2, null);
                    break;
                default:
                    v = LayoutInflater.from(getActivity()).inflate(R.layout.tab_item_feed_3, null);
                    break;

            }

            return v;
        }

    }

    @Override
    public void onDestroyView() {
        NotificationCenter.getInstance().removeObserver(this,NotificationCenter.closeFilter);
        super.onDestroyView();
    }
}
