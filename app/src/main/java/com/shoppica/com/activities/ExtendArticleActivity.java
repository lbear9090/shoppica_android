package com.shoppica.com.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.crashlytics.android.Crashlytics;
import com.shoppica.com.R;
import com.shoppica.com.constants.Constants;
import com.shoppica.com.constants.Purchases;
import com.shoppica.com.jobs.DailyNotificationJob;
import com.shoppica.com.jobs.JobLauncher;
import com.shoppica.com.objects.FeedObject;
import com.shoppica.com.objects.PostObject;
import com.shoppica.com.preferences.Prefs;
import com.shoppica.com.utils.AndroidUtilities;
import com.shoppica.com.utils.NetworkUtils;
import com.shoppica.com.utils.ViewUtil;
import com.shoppica.com.utils.typeface.SpannableBuilder;
import com.shoppica.com.view.GlideApp;
import com.shoppica.com.view.PagerBullet;
import com.shoppica.com.view.ViewPagerCustomDuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;

public class ExtendArticleActivity extends FullBaseActivity implements BillingProcessor.IBillingHandler {

    public static final String TAG = ExtendArticleActivity.class.getSimpleName();

    public static final String PHOTOS = "photos";
    public static final String OBJECT = "object";

    @BindView(R.id.viewPagerBullet)
    PagerBullet mViewPager;

    @BindView(R.id.viewPagerDuration)
    ViewPagerCustomDuration mViewPagerDuration;

    @BindView(R.id.back)
    ImageButton mBack;

    private ArrayList<String> imageList;
    private int mPostExtended;

    private TextView mOption2Text;
    private TextView mOption3Text;
    private TextView mOption4Text;
    private TextView mOption5Text;
    private TextView mOption6Text;
    private ImageButton mOption1, mOption2, mOption3, mOption4, mOption5, mOption6;
    private ImageView mOption1Done, mOption2Done, mOption3Done, mOption4Done, mOption5Done, mOption6Done;

    private boolean mIsBillingSupported = true;

    private boolean mIsReadyToPurchase, mBillingOpened;

    private SkuDetails mSku2, mSku3, mSku4, mSku5, mSku6;

    private BillingProcessor bp;

    private FeedObject mFeedObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extended);

        if (savedInstanceState != null)
            finish();

        init();

        initializeViewPager();

        initBilling();

        initDrawable();
    }

    private void initBilling() {

        boolean isAvailable = BillingProcessor.isIabServiceAvailable(this);
        if (!isAvailable) {
            mIsBillingSupported = false;
            return;
        }

        bp = new BillingProcessor(this, Purchases.API_KEY, this);


    }

    private void initDrawable() {

        Drawable backDrawable = AndroidUtilities.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(48), ContextCompat.getColor(getApplicationContext(),
                R.color.transparent), ContextCompat.getColor(getApplicationContext(), R.color.colorFilterPressed));

        mBack.setBackground(backDrawable);

    }

    private void init() {

        Intent intent = getIntent();

        if (intent == null) {

            Toast.makeText(this, R.string.toast_error_intent_null, Toast.LENGTH_SHORT).show();

            AndroidUtilities.runOnUIThread(this::finish, 2000);

            return;
        }


        imageList = intent.getStringArrayListExtra(PHOTOS);

        if (imageList == null) {

            Toast.makeText(this, R.string.toast_error_intent_null, Toast.LENGTH_SHORT).show();

            AndroidUtilities.runOnUIThread(this::finish, 2000);
            return;
        }

        mFeedObject = Objects.requireNonNull(intent.getExtras()).getParcelable(OBJECT);

        if (mFeedObject == null) {
            Toast.makeText(this, R.string.toast_error_intent_null, Toast.LENGTH_SHORT).show();
            AndroidUtilities.runOnUIThread(this::finish, 2000);
            return;
        }

        CustomPagerAdapter mPagerAdapter = new CustomPagerAdapter(this);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        if (imageList.size() == 1)
            mViewPager.setIndicatorVisibility(false);
        else
            mViewPager.setIndicatorVisibility(true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (findViewById(android.R.id.content) != null) {
                findViewById(android.R.id.content).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }


    }



    private void initializeViewPager() {
        mViewPagerDuration.setAdapter(new ContentPagerAdapter(this));
        mViewPagerDuration.setScrollDurationFactor(3.8);
        mViewPagerDuration.setOffscreenPageLimit(1);
    }


    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {

        JobLauncher.scheduleExtendArticle(Integer.parseInt(mFeedObject.getPostId()), mPostExtended);

        long day = 1000 * 60 * 60 * 24;

        long expireTime = 0;
        long currentTime = AndroidUtilities.getCurrentTime();

        switch (productId) {
            case Purchases.PURCHASE_EXTEND_2:
                expireTime += day * 7;
                break;
            case Purchases.PURCHASE_EXTEND_3:
                expireTime += day * 30;
                break;
            case Purchases.PURCHASE_EXTEND_4:
                expireTime += day * 90;
                break;
            case Purchases.PURCHASE_EXTEND_5:
                expireTime += day * 180;
                break;
            case Purchases.PURCHASE_EXTEND_6:
                expireTime += day * 365;
                break;
        }

        long currentExpireTime;
        List<PostObject> postObjectList = helper.getPostObjects();

        for (int i = 0; i < postObjectList.size(); i++) {

            PostObject object = postObjectList.get(i);

            if (object == null)
                continue;

            if (object.getPostId() == Integer.valueOf(mFeedObject.getPostId())) {
                currentExpireTime = object.getDate();

                if (currentExpireTime < currentTime)
                    helper.updatePostObject(Integer.valueOf(mFeedObject.getPostId()), (currentTime + expireTime) - (2 * day));
                else
                    helper.updatePostObject(Integer.valueOf(mFeedObject.getPostId()), (currentExpireTime + expireTime));
                break;
            }

        }

        DailyNotificationJob.schedule();

        finish();
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
        if (errorCode == 2)
            Snackbar.make(findViewById(android.R.id.content), R.string.snackbar_error_connection, Snackbar.LENGTH_LONG).show();
        else if (errorCode == 6)
            Snackbar.make(findViewById(android.R.id.content), R.string.snackbar_error_purchase, Snackbar.LENGTH_LONG).show();

        Crashlytics.logException(error);
    }

    @Override
    public void onBillingInitialized() {


        AndroidUtilities.globalQueue.postRunnable(() -> {

            bp.loadOwnedPurchasesFromGoogle();

            boolean isOneTimePurchaseSupported = bp.isOneTimePurchaseSupported();
            if (!isOneTimePurchaseSupported) {
                mIsBillingSupported = false;

                AndroidUtilities.runOnUIThread(() -> {
                    mOption1Done.setVisibility(View.VISIBLE);
                    mOption1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_selected));

                    mOption2.setEnabled(false);
                    mOption2.setAlpha(0.5f);
                    mOption2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                    mOption2Text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                    mOption2Text.setText(getString(R.string.btn_action_unavailable));
                    mOption2Done.setVisibility(View.GONE);

                    mOption3.setEnabled(false);
                    mOption3.setAlpha(0.5f);
                    mOption3.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                    mOption3Text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                    mOption3Text.setText(getString(R.string.btn_action_unavailable));
                    mOption3Done.setVisibility(View.GONE);

                    mOption4.setEnabled(false);
                    mOption4.setAlpha(0.5f);
                    mOption4.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                    mOption4Text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                    mOption4Text.setText(getString(R.string.btn_action_unavailable));
                    mOption4Done.setVisibility(View.GONE);

                    mOption5.setEnabled(false);
                    mOption5.setAlpha(0.5f);
                    mOption5.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                    mOption5Text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                    mOption5Text.setText(getString(R.string.btn_action_unavailable));
                    mOption5Done.setVisibility(View.GONE);

                    mOption6.setEnabled(false);
                    mOption6.setAlpha(0.5f);
                    mOption6.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                    mOption6Text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                    mOption6Text.setText(getString(R.string.btn_action_unavailable));
                    mOption6Done.setVisibility(View.GONE);

                });

                return;
            }

            mIsReadyToPurchase = true;

            mSku2 = bp.getPurchaseListingDetails(Purchases.PURCHASE_EXTEND_2);
            mSku3 = bp.getPurchaseListingDetails(Purchases.PURCHASE_EXTEND_3);
            mSku4 = bp.getPurchaseListingDetails(Purchases.PURCHASE_EXTEND_4);
            mSku5 = bp.getPurchaseListingDetails(Purchases.PURCHASE_EXTEND_5);
            mSku6 = bp.getPurchaseListingDetails(Purchases.PURCHASE_EXTEND_6);

            AndroidUtilities.runOnUIThread(() -> {

                if (mSku2 != null && !TextUtils.isEmpty(mSku2.priceText))
                    mOption2Text.setText(mSku2.priceText);
                else {
                    mSku2 = bp.getPurchaseListingDetails(Purchases.PURCHASE_EXTEND_2);

                    if (mSku2 != null && !TextUtils.isEmpty(mSku2.priceText))
                        mOption2Text.setText(mSku2.priceText);
                    else {
                        mOption2Text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                        mOption2Text.setText(getString(R.string.btn_action_unavailable));
                    }
                }

                if (mSku3 != null && !TextUtils.isEmpty(mSku3.priceText))
                    mOption3Text.setText(mSku3.priceText);
                else {
                    mSku3 = bp.getPurchaseListingDetails(Purchases.PURCHASE_EXTEND_3);

                    if (mSku3 != null && !TextUtils.isEmpty(mSku3.priceText))
                        mOption3Text.setText(mSku3.priceText);
                    else {
                        mOption3Text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                        mOption3Text.setText(getString(R.string.btn_action_unavailable));
                    }
                }

                if (mSku4 != null && !TextUtils.isEmpty(mSku4.priceText))
                    mOption4Text.setText(mSku4.priceText);
                else {
                    mSku4 = bp.getPurchaseListingDetails(Purchases.PURCHASE_EXTEND_4);

                    if (mSku4 != null && !TextUtils.isEmpty(mSku4.priceText))
                        mOption4Text.setText(mSku4.priceText);
                    else {
                        mOption4Text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                        mOption4Text.setText(getString(R.string.btn_action_unavailable));
                    }
                }

                if (mSku5 != null && !TextUtils.isEmpty(mSku5.priceText))
                    mOption5Text.setText(mSku5.priceText);
                else {
                    mSku5 = bp.getPurchaseListingDetails(Purchases.PURCHASE_EXTEND_5);

                    if (mSku5 != null && !TextUtils.isEmpty(mSku5.priceText))
                        mOption5Text.setText(mSku5.priceText);
                    else {
                        mOption5Text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                        mOption5Text.setText(getString(R.string.btn_action_unavailable));
                    }
                }

                if (mSku6 != null && !TextUtils.isEmpty(mSku6.priceText))
                    mOption6Text.setText(mSku6.priceText);
                else {
                    mSku6 = bp.getPurchaseListingDetails(Purchases.PURCHASE_EXTEND_6);

                    if (mSku6 != null && !TextUtils.isEmpty(mSku6.priceText))
                        mOption6Text.setText(mSku6.priceText);
                    else {
                        mOption6Text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                        mOption6Text.setText(getString(R.string.btn_action_unavailable));
                    }
                }
            });
        });


    }


    public class CustomPagerAdapter extends PagerAdapter {

        private Context mContext;

        CustomPagerAdapter(Context context) {
            mContext = context;
        }

        @Override
        public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup collection, final int position) {

            @SuppressLint("InflateParams") View layout = LayoutInflater.from(mContext).inflate(R.layout.item_view_edit_photo, null);

            ImageView imageView = ViewUtil.findById(layout, R.id.image);
            String image = imageList.get(position);

            GlideApp.with(getApplicationContext())
                    .load(image)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(new RequestOptions().fitCenter()
                            .override(800, 800)
                    ).into(imageView);

            collection.addView(layout, 0);


            return layout;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup collection, int position, @NonNull Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return imageList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    }

    public class ContentPagerAdapter extends PagerAdapter {

        private Context mContext;

        ContentPagerAdapter(Context context) {
            mContext = context;
        }

        @Override
        public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup collection, final int position) {


            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup layout;

            switch (position) {
                default:
                    layout = (ViewGroup) inflater.inflate(R.layout.item_edit_page_5_extend, collection, false);


                    long expireTime = (Long.parseLong(mFeedObject.getPostTimeExpires()) * 1000);
                    long currTime = AndroidUtilities.getCurrentTime();
                    long timeLeft = expireTime - currTime;
                    TextView mSubTitle = ViewUtil.findById(layout, R.id.chancesSubTitle);
                    if (timeLeft > 0) {

                        SpannableBuilder spannableBuilder = new SpannableBuilder(getApplicationContext());
                        spannableBuilder.createStyle().setFont("fonts/MONTSERRAT-REGULAR.TTF").apply();
                        spannableBuilder.append(getString(R.string.spannable_extend_desc1));
                        spannableBuilder.append(" ");
                        spannableBuilder.clearStyle();
                        spannableBuilder.createStyle().setFont("fonts/MONTSERRAT-BOLD.TTF").apply();
                        spannableBuilder.append(AndroidUtilities.getDurationBreakdown(timeLeft));
                        spannableBuilder.append(" ");
                        spannableBuilder.append(getString(R.string.spannable_extend_desc2));
                        spannableBuilder.clearStyle();
                        spannableBuilder.createStyle().setFont("fonts/MONTSERRAT-REGULAR.TTF").apply();
                        spannableBuilder.append(". ");
                        spannableBuilder.append(getString(R.string.spannable_extend_desc3));

                        mSubTitle.setText(spannableBuilder.build());
                    } else
                        mSubTitle.setText(getString(R.string.extend_post_sub_title));

                    mOption1 = ViewUtil.findById(layout, R.id.option1);
                    mOption1Done = ViewUtil.findById(layout, R.id.option1Done);

                    mOption2 = ViewUtil.findById(layout, R.id.option2);
                    mOption2Done = ViewUtil.findById(layout, R.id.option2Done);

                    mOption3 = ViewUtil.findById(layout, R.id.option3);
                    mOption3Done = ViewUtil.findById(layout, R.id.option3Done);

                    mOption4 = ViewUtil.findById(layout, R.id.option4);
                    mOption4Done = ViewUtil.findById(layout, R.id.option4Done);

                    mOption5 = ViewUtil.findById(layout, R.id.option5);
                    mOption5Done = ViewUtil.findById(layout, R.id.option5Done);

                    mOption6 = ViewUtil.findById(layout, R.id.option6);
                    mOption6Done = ViewUtil.findById(layout, R.id.option6Done);

                    mOption2Text = ViewUtil.findById(layout, R.id.option2SubTitle);
                    mOption3Text = ViewUtil.findById(layout, R.id.option3SubTitle);
                    mOption4Text = ViewUtil.findById(layout, R.id.option4SubTitle);
                    mOption5Text = ViewUtil.findById(layout, R.id.option5SubTitle);
                    mOption6Text = ViewUtil.findById(layout, R.id.option6SubTitle);

                    bp.initialize();

                    mPostExtended = Prefs.getInt(Constants.KEY_POST_EXTEND_CHOICE, 2);

                    if (!mIsBillingSupported) {
                        mOption1Done.setVisibility(View.VISIBLE);
                        mOption1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_selected));

                        mOption2.setEnabled(false);
                        mOption2.setAlpha(0.5f);
                        mOption2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption2Text.setText(getString(R.string.btn_action_unavailable));
                        mOption2Done.setVisibility(View.GONE);


                        mOption3.setEnabled(false);
                        mOption3.setAlpha(0.5f);
                        mOption3.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption3Text.setText(getString(R.string.btn_action_unavailable));
                        mOption3Done.setVisibility(View.GONE);

                        mOption4.setEnabled(false);
                        mOption4.setAlpha(0.5f);
                        mOption4.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption4Text.setText(getString(R.string.btn_action_unavailable));
                        mOption4Done.setVisibility(View.GONE);

                        mOption5.setEnabled(false);
                        mOption5.setAlpha(0.5f);
                        mOption5.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption5Text.setText(getString(R.string.btn_action_unavailable));
                        mOption5Done.setVisibility(View.GONE);

                        mOption6.setEnabled(false);
                        mOption6.setAlpha(0.5f);
                        mOption6.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption6Text.setText(getString(R.string.btn_action_unavailable));
                        mOption6Done.setVisibility(View.GONE);

                        mPostExtended = 0;

                    } else if (mPostExtended == 0) {
                        mOption1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_selected));
                        mOption1Done.setVisibility(View.VISIBLE);
                    } else if (mPostExtended == 1) {
                        mOption2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_selected));
                        mOption2Done.setVisibility(View.VISIBLE);
                    } else if (mPostExtended == 2) {
                        mOption3.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_selected));
                        mOption3Done.setVisibility(View.VISIBLE);
                    } else if (mPostExtended == 3) {
                        mOption4.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_selected));
                        mOption4Done.setVisibility(View.VISIBLE);
                    } else if (mPostExtended == 4) {
                        mOption5.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_selected));
                        mOption5Done.setVisibility(View.VISIBLE);
                    } else if (mPostExtended == 5) {
                        mOption6.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_selected));
                        mOption6Done.setVisibility(View.VISIBLE);
                    }

                    mOption1.setOnClickListener(view -> {
                        mOption1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_selected));
                        mOption2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption3.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption4.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption5.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption6.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption1Done.setVisibility(View.VISIBLE);
                        mOption2Done.setVisibility(View.GONE);
                        mOption3Done.setVisibility(View.GONE);
                        mOption4Done.setVisibility(View.GONE);
                        mOption5Done.setVisibility(View.GONE);
                        mOption6Done.setVisibility(View.GONE);
                        mPostExtended = 0;
                        Prefs.putInt(Constants.KEY_POST_EXTEND_CHOICE, mPostExtended);
                    });

                    mOption2.setOnClickListener(view -> {
                        mOption2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_selected));
                        mOption1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption3.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption4.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption5.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption6.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption2Done.setVisibility(View.VISIBLE);
                        mOption1Done.setVisibility(View.GONE);
                        mOption3Done.setVisibility(View.GONE);
                        mOption4Done.setVisibility(View.GONE);
                        mOption5Done.setVisibility(View.GONE);
                        mOption6Done.setVisibility(View.GONE);
                        mPostExtended = 1;
                        Prefs.putInt(Constants.KEY_POST_EXTEND_CHOICE, mPostExtended);
                    });

                    mOption3.setOnClickListener(view -> {
                        mOption3.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_selected));
                        mOption1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption4.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption5.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption6.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption3Done.setVisibility(View.VISIBLE);
                        mOption1Done.setVisibility(View.GONE);
                        mOption2Done.setVisibility(View.GONE);
                        mOption4Done.setVisibility(View.GONE);
                        mOption5Done.setVisibility(View.GONE);
                        mOption6Done.setVisibility(View.GONE);
                        mPostExtended = 2;
                        Prefs.putInt(Constants.KEY_POST_EXTEND_CHOICE, mPostExtended);
                    });

                    mOption4.setOnClickListener(view -> {
                        mOption4.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_selected));
                        mOption1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption3.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption5.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption6.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption4Done.setVisibility(View.VISIBLE);
                        mOption1Done.setVisibility(View.GONE);
                        mOption2Done.setVisibility(View.GONE);
                        mOption3Done.setVisibility(View.GONE);
                        mOption5Done.setVisibility(View.GONE);
                        mOption6Done.setVisibility(View.GONE);
                        mPostExtended = 3;
                        Prefs.putInt(Constants.KEY_POST_EXTEND_CHOICE, mPostExtended);
                    });

                    mOption5.setOnClickListener(view -> {
                        mOption5.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_selected));
                        mOption1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption3.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption4.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption6.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption5Done.setVisibility(View.VISIBLE);
                        mOption1Done.setVisibility(View.GONE);
                        mOption2Done.setVisibility(View.GONE);
                        mOption3Done.setVisibility(View.GONE);
                        mOption4Done.setVisibility(View.GONE);
                        mOption6Done.setVisibility(View.GONE);
                        mPostExtended = 4;
                        Prefs.putInt(Constants.KEY_POST_EXTEND_CHOICE, mPostExtended);
                    });


                    mOption6.setOnClickListener(view -> {
                        mOption6.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_selected));
                        mOption1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption3.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption4.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption5.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_un_selected));
                        mOption6Done.setVisibility(View.VISIBLE);
                        mOption1Done.setVisibility(View.GONE);
                        mOption2Done.setVisibility(View.GONE);
                        mOption3Done.setVisibility(View.GONE);
                        mOption4Done.setVisibility(View.GONE);
                        mOption5Done.setVisibility(View.GONE);
                        mPostExtended = 5;
                        Prefs.putInt(Constants.KEY_POST_EXTEND_CHOICE, mPostExtended);
                    });
                    break;


            }

            collection.addView(layout, 0);

            return layout;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup collection, int position, @NonNull Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    @OnClick(R.id.back)
    void onBackClicked() {

        finish();

    }


    @OnClick(R.id.next)
    void onNextClicked() {

        if (mBillingOpened)
            return;

        mBillingOpened = true;

        NetworkUtils.isNetworkAvailable(mIncomingHandler, 2000);

    }

    public Handler mIncomingHandler = new Handler(msg -> {

        if (msg.what == 1) {

            if (mPostExtended != 0) {

                if (!mIsReadyToPurchase) {
                    Snackbar.make(findViewById(android.R.id.content), R.string.snackbar_error_ready_purchase, Snackbar.LENGTH_SHORT).show();
                    return true;
                }

                String productId;

                if (mPostExtended == 1) {
                    productId = Purchases.PURCHASE_EXTEND_2;
                } else if (mPostExtended == 2) {
                    productId = Purchases.PURCHASE_EXTEND_3;
                } else if (mPostExtended == 3) {
                    productId = Purchases.PURCHASE_EXTEND_4;
                } else if (mPostExtended == 4) {
                    productId = Purchases.PURCHASE_EXTEND_5;
                } else {
                    productId = Purchases.PURCHASE_EXTEND_6;
                }
                AndroidUtilities.globalQueue.postRunnable(() -> {
                    mBillingOpened = false;
                    bp.consumePurchase(productId);
                    bp.purchase(ExtendArticleActivity.this, productId);
                });
            } else
                finish();


        } else {
            Toast.makeText(getApplicationContext(), R.string.toast_error_no_internet_connection, Toast.LENGTH_SHORT).show();
            mBillingOpened = false;
        }


        return true;
    });


    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }
}
