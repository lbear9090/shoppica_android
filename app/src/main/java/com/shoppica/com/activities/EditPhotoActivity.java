package com.shoppica.com.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
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
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.loopj.android.http.RequestParams;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.shoppica.com.R;
import com.shoppica.com.constants.Constants;
import com.shoppica.com.constants.Purchases;
import com.shoppica.com.country.CountryCodePicker;
import com.shoppica.com.jobs.DailyNotificationJob;
import com.shoppica.com.jobs.JobLauncher;
import com.shoppica.com.preferences.Prefs;
import com.shoppica.com.server.request;
import com.shoppica.com.utils.AndroidUtilities;
import com.shoppica.com.utils.NetworkUtils;
import com.shoppica.com.utils.ScalingUtilities;
import com.shoppica.com.utils.ViewUtil;
import com.shoppica.com.utils.typeface.SpannableBuilder;
import com.shoppica.com.view.GlideApp;
import com.shoppica.com.view.PagerBullet;
import com.shoppica.com.view.SingleShotLocationProvider;
import com.shoppica.com.view.ViewPagerCustomDuration;
import com.yalantis.ucrop.UCrop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

@RuntimePermissions
public class EditPhotoActivity extends FullBaseActivity implements BillingProcessor.IBillingHandler {

    public static final String TAG = EditPhotoActivity.class.getSimpleName();

    private static final String PAGE_1 = "1";
    private static final String PAGE_2 = "2";
    private static final String PAGE_3 = "3";
    private static final String PAGE_4 = "4";
    private static final String PAGE_5 = "5";
    private static final String PAGE_END = "/5";

    public static final String PHOTOS = "photos";

    @BindView(R.id.viewPagerBullet)
    PagerBullet mViewPager;

    @BindView(R.id.viewPagerDuration)
    ViewPagerCustomDuration mViewPagerDuration;

    @BindView(R.id.toolbar_status)
    TextView mStatusText;

    @BindView(R.id.nextText)
    TextView mNextText;

    @BindView(R.id.next)
    View mNext;

    @BindView(R.id.backText)
    TextView mBackText;

    @BindView(R.id.addPhoto)
    ImageView mAddPhoto;

    @BindView(R.id.back)
    View mBack;

    private ArrayList<String> imageList;
    private CustomPagerAdapter mPagerAdapter;

    private TextView mCategory, mGender, mCondition, mSend, mAskPrice, mTitle, mBrand, mColor, mSize, mWhatsapp, mInstagram, mEmail, mLocation, mLocationSelf;
    private int mPosCategory = -1, mPosGender, mPosCondition, mPosSend;
    private String mAskPriceText, mTitleText, mBrandText, mColorText, mSizeText, mWhatsAppText, mInstagramText, mEmailText, mFetchedLocation, mInputLocation;
    private float mFetchLocationLatitude, mFetchLocationLongitude;
    private double mInputLocationLatitude, mInputLocationLongitude;
    private ProgressWheel mLocationProgress;
    private String mEncodedPos1, mEncodedPos2, mEncodedPos3, mEncodedPos4, mEncodedPos5, mEncodedPos1Thumb, mEncodedPos2Thumb, mEncodedPos3Thumb, mEncodedPos4Thumb, mEncodedPos5Thumb, mPostUrl;
    private int mEncodedPos1ThumbWidth, mEncodedPos1ThumbHeight, mEncodedPos1Width, mEncodedPos1Height, mPostPromoted;

    private boolean mIsPosted, mPhotos1Loaded = true, mPhotos2Loaded = true, mPhotos3Loaded = true, mPhotos4Loaded = true, mPhotos5Loaded = true;
    private ProgressDialog mProgressDialog;

    private TextView mOption2Text;
    private TextView mOption3Text;
    private TextView mOption4Text;
    private TextView mOption5Text;
    private TextView mOption6Text;
    private ImageButton mOption1, mOption2, mOption3, mOption4, mOption5, mOption6;
    private ImageView mOption1Done, mOption2Done, mOption3Done, mOption4Done, mOption5Done, mOption6Done;

    private boolean mIsBillingSupported = true;

    private boolean mIsReadyToPurchase;

    private SkuDetails mSku2, mSku3, mSku4, mSku5, mSku6;


    private BillingProcessor bp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photos);

        if (savedInstanceState != null)
            finish();

        init();

        initProgressDialog();

        initializeViewPager();

        initBilling();


    }

    private void initBilling() {

        boolean isAvailable = BillingProcessor.isIabServiceAvailable(this);
        if (!isAvailable) {
            mIsBillingSupported = false;
            return;
        }

        bp = new BillingProcessor(this, Purchases.API_KEY, this);


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

        initData();

        mPagerAdapter = new CustomPagerAdapter(this);
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

    private void initProgressDialog() {
        mProgressDialog = new ProgressDialog(this, R.style.into_progress_style);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setMessage(getString(R.string.alert_progress_uploading));
        mProgressDialog.setCancelable(false);
    }

    private void showDialog(String message) {

        if (mProgressDialog != null) {
            mProgressDialog.setMessage(message);

            if (!mProgressDialog.isShowing())
                mProgressDialog.show();
        }
    }

    private void dismissDialog() {

        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

    public void updateStatusText(String page) {
        SpannableBuilder builder = new SpannableBuilder(this);
        builder.createStyle().setColor(ContextCompat.getColor(getApplicationContext(), R.color.black)).setFont("fonts/MONTSERRAT-BOLD.TTF").apply().append(page);
        builder.clearStyle();
        builder.createStyle().setColor(ContextCompat.getColor(getApplicationContext(), R.color.black)).setFont("fonts/MONTSERRAT-SEMIBOLD.TTF").apply().append(PAGE_END);
        mStatusText.setText(builder.build());
        mStatusText.setVisibility(View.VISIBLE);
    }


    private void initializeViewPager() {
        mViewPagerDuration.setAdapter(new ContentPagerAdapter(this));
        mViewPagerDuration.setScrollDurationFactor(3.8);
        mViewPagerDuration.setOffscreenPageLimit(6);

        updateStatusText(PAGE_1);
    }

    private void initData() {

        for (int i = 0; i < imageList.size(); i++) {
            int finalI = i;

            AndroidUtilities.globalQueue.postRunnable(() -> {

                try {

                    if (finalI == 0) {

                        if (TextUtils.isEmpty(mEncodedPos1) || TextUtils.isEmpty(mEncodedPos1Thumb)) {

                            Bitmap bitData = BitmapFactory.decodeFile(imageList.get(0));
                            Bitmap bitmapScale = ScalingUtilities.createScaledBitmap(bitData, 800, 800, ScalingUtilities.ScalingLogic.FIT);

                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            bitmapScale.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
                            byte[] array = outputStream.toByteArray();

                            mEncodedPos1 = Base64.encodeToString(array, Base64.DEFAULT);
                            mEncodedPos1Width = bitmapScale.getWidth();
                            mEncodedPos1Height = bitmapScale.getHeight();

                            Bitmap bitThumb = ScalingUtilities.createScaledBitmap(bitData, 400, 400, ScalingUtilities.ScalingLogic.FIT);
                            ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
                            bitThumb.compress(Bitmap.CompressFormat.JPEG, 85, outputStream2);
                            byte[] array2 = outputStream.toByteArray();

                            mEncodedPos1Thumb = Base64.encodeToString(array2, Base64.DEFAULT);
                            mEncodedPos1ThumbWidth = bitThumb.getWidth();
                            mEncodedPos1ThumbHeight = bitThumb.getHeight();

                            bitData.recycle();
                            bitmapScale.recycle();
                            bitThumb.recycle();
                            array = null;
                            array2 = null;

                            if (TextUtils.isEmpty(mEncodedPos1) || TextUtils.isEmpty(mEncodedPos1Thumb))
                                mPhotos1Loaded = false;
                        }


                    } else if (finalI == 1) {
                        if (TextUtils.isEmpty(mEncodedPos2) || TextUtils.isEmpty(mEncodedPos2Thumb)) {


                            Bitmap bitData = BitmapFactory.decodeFile(imageList.get(1));
                            Bitmap bitmapScale = ScalingUtilities.createScaledBitmap(bitData, 800, 800, ScalingUtilities.ScalingLogic.FIT);

                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            bitmapScale.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
                            byte[] array = outputStream.toByteArray();

                            mEncodedPos2 = Base64.encodeToString(array, Base64.DEFAULT);

                            Bitmap bitThumb = ScalingUtilities.createScaledBitmap(bitData, 400, 400, ScalingUtilities.ScalingLogic.FIT);
                            ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
                            bitThumb.compress(Bitmap.CompressFormat.JPEG, 85, outputStream2);
                            byte[] array2 = outputStream.toByteArray();

                            mEncodedPos2Thumb = Base64.encodeToString(array2, Base64.DEFAULT);

                            bitData.recycle();
                            bitmapScale.recycle();
                            bitThumb.recycle();
                            array = null;
                            array2 = null;

                            if (TextUtils.isEmpty(mEncodedPos2) || TextUtils.isEmpty(mEncodedPos2Thumb))
                                mPhotos2Loaded = false;

                        }

                    } else if (finalI == 2) {

                        if (TextUtils.isEmpty(mEncodedPos3) || TextUtils.isEmpty(mEncodedPos3Thumb)) {


                            Bitmap bitData = BitmapFactory.decodeFile(imageList.get(2));
                            Bitmap bitmapScale = ScalingUtilities.createScaledBitmap(bitData, 800, 800, ScalingUtilities.ScalingLogic.FIT);

                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            bitmapScale.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
                            byte[] array = outputStream.toByteArray();

                            mEncodedPos3 = Base64.encodeToString(array, Base64.DEFAULT);

                            Bitmap bitThumb = ScalingUtilities.createScaledBitmap(bitData, 400, 400, ScalingUtilities.ScalingLogic.FIT);
                            ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
                            bitThumb.compress(Bitmap.CompressFormat.JPEG, 85, outputStream2);
                            byte[] array2 = outputStream.toByteArray();

                            mEncodedPos3Thumb = Base64.encodeToString(array2, Base64.DEFAULT);

                            bitData.recycle();
                            bitmapScale.recycle();
                            bitThumb.recycle();
                            array = null;
                            array2 = null;

                            if (TextUtils.isEmpty(mEncodedPos3) || TextUtils.isEmpty(mEncodedPos3Thumb))
                                mPhotos3Loaded = false;

                        }

                    } else if (finalI == 3) {

                        if (TextUtils.isEmpty(mEncodedPos4) || TextUtils.isEmpty(mEncodedPos4Thumb)) {


                            Bitmap bitData = BitmapFactory.decodeFile(imageList.get(3));
                            Bitmap bitmapScale = ScalingUtilities.createScaledBitmap(bitData, 800, 800, ScalingUtilities.ScalingLogic.FIT);

                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            bitmapScale.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
                            byte[] array = outputStream.toByteArray();

                            mEncodedPos4 = Base64.encodeToString(array, Base64.DEFAULT);

                            Bitmap bitThumb = ScalingUtilities.createScaledBitmap(bitData, 400, 400, ScalingUtilities.ScalingLogic.FIT);
                            ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
                            bitThumb.compress(Bitmap.CompressFormat.JPEG, 85, outputStream2);
                            byte[] array2 = outputStream.toByteArray();

                            mEncodedPos4Thumb = Base64.encodeToString(array2, Base64.DEFAULT);

                            bitData.recycle();
                            bitmapScale.recycle();
                            bitThumb.recycle();
                            array = null;
                            array2 = null;

                            if (TextUtils.isEmpty(mEncodedPos4) || TextUtils.isEmpty(mEncodedPos4Thumb))
                                mPhotos4Loaded = false;

                        }

                    } else if (finalI == 4) {

                        if (TextUtils.isEmpty(mEncodedPos5) || TextUtils.isEmpty(mEncodedPos5Thumb)) {

                            Bitmap bitData = BitmapFactory.decodeFile(imageList.get(4));
                            Bitmap bitmapScale = ScalingUtilities.createScaledBitmap(bitData, 800, 800, ScalingUtilities.ScalingLogic.FIT);

                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            bitmapScale.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
                            byte[] array = outputStream.toByteArray();

                            mEncodedPos5 = Base64.encodeToString(array, Base64.DEFAULT);

                            Bitmap bitThumb = ScalingUtilities.createScaledBitmap(bitData, 400, 400, ScalingUtilities.ScalingLogic.FIT);
                            ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
                            bitThumb.compress(Bitmap.CompressFormat.JPEG, 85, outputStream2);
                            byte[] array2 = outputStream.toByteArray();

                            mEncodedPos5Thumb = Base64.encodeToString(array2, Base64.DEFAULT);

                            bitData.recycle();
                            bitmapScale.recycle();
                            bitThumb.recycle();
                            array = null;
                            array2 = null;

                            if (TextUtils.isEmpty(mEncodedPos5) || TextUtils.isEmpty(mEncodedPos5Thumb))
                                mPhotos5Loaded = false;

                        }

                    }

                } catch (Exception e) {
                    Crashlytics.logException(e);
                }
            });
        }
    }


    private boolean checkSize() {

        int size = imageList.size();

        if (size == 1) {
            return mPhotos1Loaded;
        } else if (size == 2) {
            return mPhotos1Loaded & mPhotos2Loaded;
        } else if (size == 3) {
            return mPhotos1Loaded & mPhotos2Loaded & mPhotos3Loaded;
        } else if (size == 4) {
            return mPhotos1Loaded & mPhotos2Loaded & mPhotos3Loaded & mPhotos4Loaded;
        } else
            return mPhotos1Loaded & mPhotos2Loaded & mPhotos3Loaded & mPhotos4Loaded & mPhotos5Loaded;
    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {

        showDialog(getString(R.string.alert_progress_payment_success));

        makeRequest();

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

            mSku2 = bp.getPurchaseListingDetails(Purchases.PURCHASE_PROMOTION_2);
            mSku3 = bp.getPurchaseListingDetails(Purchases.PURCHASE_PROMOTION_3);
            mSku4 = bp.getPurchaseListingDetails(Purchases.PURCHASE_PROMOTION_4);
            mSku5 = bp.getPurchaseListingDetails(Purchases.PURCHASE_PROMOTION_5);
            mSku6 = bp.getPurchaseListingDetails(Purchases.PURCHASE_PROMOTION_6);


            AndroidUtilities.runOnUIThread(() -> {

                if (mSku2 != null && !TextUtils.isEmpty(mSku2.priceText))
                    mOption2Text.setText(mSku2.priceText);
                else {
                    mSku2 = bp.getPurchaseListingDetails(Purchases.PURCHASE_PROMOTION_2);

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
                    mSku3 = bp.getPurchaseListingDetails(Purchases.PURCHASE_PROMOTION_3);

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
                    mSku4 = bp.getPurchaseListingDetails(Purchases.PURCHASE_PROMOTION_4);

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
                    mSku5 = bp.getPurchaseListingDetails(Purchases.PURCHASE_PROMOTION_5);

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
                    mSku6 = bp.getPurchaseListingDetails(Purchases.PURCHASE_PROMOTION_6);

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

            GlideApp.with(getApplicationContext()).load(image)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(new RequestOptions().centerInside().override(800, 800)).into(imageView);

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
            ViewGroup layout = null;

            switch (position) {

                case 0:
                    layout = (ViewGroup) inflater.inflate(R.layout.item_edit_page_1, collection, false);
                    String[] sendMethodes = getResources().getStringArray(R.array.send);
                    String[] gender = getResources().getStringArray(R.array.gender);

                    mAskPrice = ViewUtil.findById(layout, R.id.articlePriceTitle);
                    View mAskPriceView = ViewUtil.findById(layout, R.id.articlePriceView);

                    mAskPriceView.setOnClickListener(view -> {

                        showEditDialog(getString(R.string.alert_add_price), getString(R.string.alert_add_price_sub), getString(R.string.alert_add_price_hint), mAskPriceText, 0);

                    });

                    View mSendView = ViewUtil.findById(layout, R.id.articleSendView);
                    mSendView.setOnClickListener(view -> {
                        showDialog(getString(R.string.alert_send_title), sendMethodes, 0);
                    });

                    View mGenderView = ViewUtil.findById(layout, R.id.articleGenderView);
                    mGenderView.setOnClickListener(view -> {
                        showDialog(getString(R.string.alert_gender_title), gender, 1);
                    });

                    View mCategoryView = ViewUtil.findById(layout, R.id.articleCategoryView);
                    mCategoryView.setOnClickListener(view -> {

                        mPosGender = Prefs.getInt(Constants.KEY_POST_GENDER, -1);

                        if (mPosGender == -1)
                            mPosGender = Prefs.getInt(Constants.KEY_REGISTER_GENDER, 0);

                        int arrayChoice = R.array.categories;


                        String[] category = getResources().getStringArray(arrayChoice);

                        showDialog(getString(R.string.alert_category_title), category, 2);


                    });

                    mCategory = ViewUtil.findById(layout, R.id.articleCategoryTitle);

                    mSend = ViewUtil.findById(layout, R.id.articleSendTitle);

                    mPosSend = Prefs.getInt(Constants.KEY_POST_SEND, 0);

                    mSend.setText(getString(R.string.article_send_method, sendMethodes[mPosSend]));

                    mGender = ViewUtil.findById(layout, R.id.articleGenderTitle);

                    mPosGender = Prefs.getInt(Constants.KEY_POST_GENDER, -1);

                    if (mPosGender == -1)
                        mPosGender = Prefs.getInt(Constants.KEY_REGISTER_GENDER, 0);

                    if (mPosGender != -1)
                        mGender.setText(gender[mPosGender]);
                    else
                        mGender.setText(gender[0]);

                    break;
                case 1:
                    layout = (ViewGroup) inflater.inflate(R.layout.item_edit_page_2, collection, false);

                    String[] condition = getResources().getStringArray(R.array.condition);

                    View mConditionView = ViewUtil.findById(layout, R.id.articleConditionView);
                    mConditionView.setOnClickListener(view -> {
                        showDialog(getString(R.string.alert_condition_title), condition, 3);
                    });

                    View mTitleView = ViewUtil.findById(layout, R.id.articleTitleView);
                    mTitleView.setOnClickListener(view -> {
                        showEditDialog(getString(R.string.alert_add_title), getString(R.string.alert_add_title_sub), getString(R.string.alert_add_title_hint), mTitleText, 1);
                    });

                    View mBrandView = ViewUtil.findById(layout, R.id.articleBrandView);
                    mBrandView.setOnClickListener(view -> {
                        showEditDialog(getString(R.string.alert_add_brand), getString(R.string.alert_add_brand_sub), getString(R.string.alert_add_brand_hint), mBrandText, 2);
                    });

                    View mColorView = ViewUtil.findById(layout, R.id.articleColorView);
                    mColorView.setOnClickListener(view -> {
                        showEditDialog(getString(R.string.alert_add_color), getString(R.string.alert_add_color_sub), getString(R.string.alert_add_color_hint), mColorText, 3);
                    });

                    View mSizeView = ViewUtil.findById(layout, R.id.articleSizeView);
                    mSizeView.setOnClickListener(view -> {
                        showEditDialog(getString(R.string.alert_add_size), getString(R.string.alert_add_size_sub), getString(R.string.alert_add_size_hint), mSizeText, 4);
                    });


                    mTitle = ViewUtil.findById(layout, R.id.articleTitleText);
                    mBrand = ViewUtil.findById(layout, R.id.articleBrandText);
                    mColor = ViewUtil.findById(layout, R.id.articleColorText);
                    mSize = ViewUtil.findById(layout, R.id.articleSizeText);


                    mCondition = ViewUtil.findById(layout, R.id.articleConditionTitle);

                    mPosCondition = Prefs.getInt(Constants.KEY_POST_CONDITION, 0);

                    mCondition.setText(condition[mPosCondition]);

                    break;
                case 2:
                    layout = (ViewGroup) inflater.inflate(R.layout.item_edit_page_3, collection, false);


                    View mWhatsappView = ViewUtil.findById(layout, R.id.contactWhatsAppView);
                    mWhatsappView.setOnClickListener(view -> {

                        if (!isAppInstalled(Constants.PACKAGE_NAME_WHATSAPP)) {
                            onError(getString(R.string.view_article_not_installed_error_whatsapp_title), getString(R.string.view_article_not_installed_error_whatsapp_desc2), Constants.PACKAGE_NAME_WHATSAPP);
                            return;
                        }

                        showEditDialog(getString(R.string.alert_add_whatsapp), getString(R.string.alert_add_whatsapp_sub), getString(R.string.alert_add_whatsapp_hint), mWhatsAppText, 5);
                    });

                    mWhatsapp = ViewUtil.findById(layout, R.id.contactWhatsAppTitle);

                    mWhatsAppText = Prefs.getString(Constants.KEY_POST_WHATSAPP, null);
                    if (!TextUtils.isEmpty(mWhatsAppText)) {
                        mWhatsapp.setText(mWhatsAppText);
                        mWhatsapp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_whatsapp, 0, R.drawable.ic_edit, 0);
                    }


                    View mInstagramView = ViewUtil.findById(layout, R.id.contactInstagramView);
                    mInstagramView.setOnClickListener(view -> {
                        showEditDialog(getString(R.string.alert_add_instagram), getString(R.string.alert_add_instagram_sub), getString(R.string.alert_add_instagram_hint), mInstagramText, 6);
                    });

                    mInstagram = ViewUtil.findById(layout, R.id.contactInstagramTitle);

                    mInstagramText = Prefs.getString(Constants.KEY_POST_INSTAGRAM, null);
                    if (!TextUtils.isEmpty(mInstagramText)) {
                        mInstagram.setText(mInstagramText);
                        mInstagram.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_instagram, 0, R.drawable.ic_edit, 0);
                    }

                    View mEmailView = ViewUtil.findById(layout, R.id.contactEmailView);
                    mEmailView.setOnClickListener(view -> {
                        showEditDialog(getString(R.string.alert_add_email), getString(R.string.alert_add_email_sub), getString(R.string.alert_add_email_hint), mEmailText, 7);
                    });

                    mEmail = ViewUtil.findById(layout, R.id.contactEmailTitle);

                    mEmailText = Prefs.getString(Constants.KEY_POST_EMAIL, null);
                    if (!TextUtils.isEmpty(mEmailText)) {
                        mEmail.setText(mEmailText);
                        mEmail.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_email, 0, R.drawable.ic_edit, 0);
                    }

                    break;
                case 3:
                    layout = (ViewGroup) inflater.inflate(R.layout.item_edit_page_4, collection, false);


                    View mCurrentLocation = ViewUtil.findById(layout, R.id.currentLocationView);
                    mCurrentLocation.setOnClickListener(view -> {

                        EditPhotoActivityPermissionsDispatcher.continueLocationPermissionWithCheck(EditPhotoActivity.this);

                    });

                    mLocation = ViewUtil.findById(layout, R.id.currentLocationTitle);
                    mLocationProgress = ViewUtil.findById(layout, R.id.locationProgress);


                    mFetchedLocation = Prefs.getString(Constants.KEY_POST_CURRENT_LOCATION, null);
                    if (!TextUtils.isEmpty(mFetchedLocation)) {
                        mLocation.setText(mFetchedLocation);
                        mLocation.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit, 0);
                        mFetchLocationLatitude = Prefs.getFloat(Constants.KEY_POST_CURRENT_LOCATION_LATITUDE, 0);
                        mFetchLocationLongitude = Prefs.getFloat(Constants.KEY_POST_CURRENT_LOCATION_LONGITUDE, 0);
                    }

                    mLocationSelf = ViewUtil.findById(layout, R.id.ownLocationTitle);

                    View mOwnLocation = ViewUtil.findById(layout, R.id.ownLocationView);
                    mOwnLocation.setOnClickListener(view -> {
                        showEditDialog(getString(R.string.alert_add_own_location), getString(R.string.alert_add_own_location_sub), getString(R.string.alert_add_own_location_sub_hint), mInputLocation, 8);
                    });


                    mInputLocation = Prefs.getString(Constants.KEY_POST_OWN_LOCATION, null);

                    if (!TextUtils.isEmpty(mInputLocation)) {
                        mLocationSelf.setText(mInputLocation);
                        mLocationSelf.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit, 0);
                        Prefs.remove(Constants.KEY_POST_CURRENT_LOCATION);
                        Prefs.remove(Constants.KEY_POST_CURRENT_LOCATION_LATITUDE);
                        Prefs.remove(Constants.KEY_POST_CURRENT_LOCATION_LONGITUDE);
                        mFetchedLocation = "";
                        mLocation.setText("");
                        mLocation.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        mInputLocationLatitude = Prefs.getDouble(Constants.KEY_POST_OWN_LOCATION_LATITUDE, 0);
                        mInputLocationLongitude = Prefs.getDouble(Constants.KEY_POST_OWN_LOCATION_LONGITUDE, 0);
                    }

                    break;
                case 4:
                    layout = (ViewGroup) inflater.inflate(R.layout.item_edit_page_5, collection, false);

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
                    if(bp != null)
                        bp.initialize();

                    mPostPromoted = Prefs.getInt(Constants.KEY_POST_PROMOTE_CHOICE, 0);

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

                        mPostPromoted = 0;

                    } else if (mPostPromoted == 0) {
                        mOption1.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_selected));
                        mOption1Done.setVisibility(View.VISIBLE);
                    } else if (mPostPromoted == 1) {
                        mOption2.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_selected));
                        mOption2Done.setVisibility(View.VISIBLE);
                    } else if (mPostPromoted == 2) {
                        mOption3.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_selected));
                        mOption3Done.setVisibility(View.VISIBLE);
                    } else if (mPostPromoted == 3) {
                        mOption4.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_selected));
                        mOption4Done.setVisibility(View.VISIBLE);
                    } else if (mPostPromoted == 4) {
                        mOption5.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_selected));
                        mOption5Done.setVisibility(View.VISIBLE);
                    } else if (mPostPromoted == 5) {
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
                        mPostPromoted = 0;
                        Prefs.putInt(Constants.KEY_POST_PROMOTE_CHOICE, mPostPromoted);
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
                        mPostPromoted = 1;
                        Prefs.putInt(Constants.KEY_POST_PROMOTE_CHOICE, mPostPromoted);
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
                        mPostPromoted = 2;
                        Prefs.putInt(Constants.KEY_POST_PROMOTE_CHOICE, mPostPromoted);
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
                        mPostPromoted = 3;
                        Prefs.putInt(Constants.KEY_POST_PROMOTE_CHOICE, mPostPromoted);
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
                        mPostPromoted = 4;
                        Prefs.putInt(Constants.KEY_POST_PROMOTE_CHOICE, mPostPromoted);
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
                        mPostPromoted = 5;
                        Prefs.putInt(Constants.KEY_POST_PROMOTE_CHOICE, mPostPromoted);
                    });
                    break;

                default:
                    layout = (ViewGroup) inflater.inflate(R.layout.item_edit_page_6, collection, false);


                    View sharePost = ViewUtil.findById(layout, R.id.sharePostView);
                    sharePost.setOnClickListener(view -> {

                        if (!ShareDialog.canShow(ShareLinkContent.class)) {
                            Toast.makeText(getApplicationContext(), R.string.toast_error_post, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String countryCode = Prefs.getString(Constants.KEY_REGISTER_COUNTRY_CODE, null);

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
                            default:
                                currency = "$";
                                break;
                        }


                        Log.d(TAG, "mAskPrice == " + mAskPriceText);

                        ShareLinkContent shareLinkContent = new ShareLinkContent.Builder()
                                .setQuote(getString(R.string.view_article_share_text, mTitleText, currency + mAskPriceText))
                                .setContentUrl(Uri.parse(mPostUrl))
                                .build();

                        ShareDialog.show(EditPhotoActivity.this, shareLinkContent);

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
            return 6;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

    }

    private void showDialog(String title, String[] items, int type) {

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this, R.style.DialogTheme);
        mBuilder.setTitle(title);

        int checked = -1;

        if (type == 0)
            checked = mPosSend;
        else if (type == 1)
            checked = mPosGender;
        else if (type == 2)
            checked = mPosCategory;
        else if (type == 3)
            checked = mPosCondition;

        mBuilder.setSingleChoiceItems(items, checked, (dialogInterface, i) -> {

            if (type == 0) {
                mSend.setText(getString(R.string.article_send_method, items[i]));
                mPosSend = i;
                Prefs.putInt(Constants.KEY_POST_SEND, i);
            } else if (type == 1) {
                mGender.setText(items[i]);
                mPosGender = i;
                Prefs.putInt(Constants.KEY_POST_GENDER, i);
            } else if (type == 2) {
                mCategory.setText(items[i]);
                mPosCategory = i;
            } else if (type == 3) {
                mCondition.setText(items[i]);
                mPosCondition = i;
                Prefs.putInt(Constants.KEY_POST_CONDITION, i);
            }


            dialogInterface.dismiss();
        });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();

    }


    @SuppressLint("SetTextI18n")
    private void showEditDialog(String title, String subTitle, String hint, String currentText, int type) {

        LayoutInflater li = LayoutInflater.from(this);

        int layout;

        if (type == 0)
            layout = R.layout.item_edit_dialog_number;
        else if (type == 5) {
            layout = R.layout.item_edit_dialog_number_code;
        } else
            layout = R.layout.item_edit_dialog;

        @SuppressLint("InflateParams") View promptsView = li.inflate(layout, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.DialogTheme);
        alertDialogBuilder.setView(promptsView);

        TextView titleTextView = ViewUtil.findById(promptsView, R.id.title);
        TextView subTitleTextView = ViewUtil.findById(promptsView, R.id.subTitle);
        CountryCodePicker countryCodePicker = null;

        String countryCode = Prefs.getString(Constants.KEY_REGISTER_COUNTRY_CODE, null);

        if (type == 5) {
            countryCodePicker = ViewUtil.findById(promptsView, R.id.ccp);
            int phoneCode = Prefs.getInt(Constants.KEY_REGISTER_PHONE_CODE, 0);

            String phoneNumber = Prefs.getString(Constants.KEY_REGISTER_PHONE_NUMBER, null);

            if (!TextUtils.isEmpty(phoneNumber))
                currentText = phoneNumber;

            if (phoneCode != 0)
                countryCodePicker.setCountryForPhoneCode(phoneCode);

        }


        titleTextView.setText(title);
        subTitleTextView.setText(subTitle);

        final EditText userInput = promptsView.findViewById(R.id.editText);
        userInput.setHint(hint);


        if (type == 0) {
            Log.d(TAG, "countryCode == " + countryCode);

            int drawable = R.drawable.ic_dollar;

            switch (countryCode.toLowerCase()) {
                case Constants.AU:
                    drawable = R.drawable.ic_aus_dollar;
                    break;
                case Constants.NZ:
                    drawable = R.drawable.ic_nz_dollar;
                    break;
                case Constants.BE:
                case Constants.NL:
                    drawable = R.drawable.ic_euro;
                    break;
                case Constants.GB:
                case Constants.IE:
                    drawable = R.drawable.ic_pound;
                    break;
            }

            userInput.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0);
            userInput.setCompoundDrawablePadding(AndroidUtilities.dp(5));
        }


        if (type == 5) {
            int maxLength = 18;
            userInput.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
            userInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        }


        if (!TextUtils.isEmpty(currentText)) {
            userInput.setText(currentText);
            userInput.setSelection(currentText.length());
        }
        showKeyboard(userInput);

        // set dialog message
        CountryCodePicker finalCountryCodePicker1 = countryCodePicker;
        alertDialogBuilder
                .setPositiveButton(R.string.btn_action_ok,
                        (dialog, id) -> {


                            updateInput(type, userInput, finalCountryCodePicker1);
                        })
                .setNegativeButton(R.string.btn_action_cancel, (dialogInterface, i) -> {
                    hideKeyboard(userInput);
                    dialogInterface.dismiss();
                });


        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        CountryCodePicker finalCountryCodePicker = countryCodePicker;
        userInput.setOnEditorActionListener((textView, i, keyEvent) -> {

            if (i == EditorInfo.IME_ACTION_DONE) {
                updateInput(type, userInput, finalCountryCodePicker);

                alertDialog.dismiss();

                return true;
            }

            return false;
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            alertDialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_dialog));
        }
        // show it
        alertDialog.show();


    }

    @SuppressLint("SetTextI18n")
    private void updateInput(int type, EditText userInput, CountryCodePicker countryCodePicker) {
        if (type == 0) {
            if (!TextUtils.isEmpty(userInput.getText())) {
                mAskPriceText = userInput.getText().toString();

                try {
                    Double myDouble = Double.parseDouble(mAskPriceText);

                    mAskPriceText = String.format(Locale.getDefault(), "%.2f", myDouble);
                    mAskPriceText = mAskPriceText.replace(getString(R.string.currency_symbol_replace_from), getString(R.string.currency_symbol_replace_to));

                    String countryCode = Prefs.getString(Constants.KEY_REGISTER_COUNTRY_CODE, null);

                    String currency = "$";

                    switch (countryCode.toLowerCase()) {
                        case Constants.AU:
                            currency = "A$";
                            mAskPriceText = mAskPriceText.replace(",", ".");
                            break;
                        case Constants.NZ:
                            currency = "NZ$";
                            mAskPriceText = mAskPriceText.replace(",", ".");
                            break;
                        case Constants.BE:
                        case Constants.NL:
                            currency = "€";
                            mAskPriceText = mAskPriceText.replace(".", ",");
                            break;
                        case Constants.GB:
                        case Constants.IE:
                            currency = "£";
                            mAskPriceText = mAskPriceText.replace(",", ".");
                            break;

                    }

                    mAskPrice.setText(currency + mAskPriceText);

                } catch (NumberFormatException e) {
                    Crashlytics.logException(e);

                    String countryCode = Prefs.getString(Constants.KEY_REGISTER_COUNTRY_CODE, null);
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
                        case Constants.ES:
                        case Constants.FR:
                        case Constants.DE:
                        case Constants.IT:
                            currency = "€";
                            break;
                        case Constants.GB:
                        case Constants.IE:
                            currency = "£";
                            break;
                        case Constants.IN:
                            currency = "₹";
                            break;
                        case Constants.JP:
                            currency = "¥";
                            break;
                        case Constants.SG:
                            currency = "S$";
                            break;
                        default:
                            currency = "$";
                            break;


                    }


                    mAskPrice.setText(currency + mAskPriceText);
                }

            } else {
                mAskPriceText = "";
                mAskPrice.setText("");
            }
        } else if (type == 1) {
            if (!TextUtils.isEmpty(userInput.getText())) {
                mTitleText = userInput.getText().toString();
                mTitle.setText(mTitleText);
            } else {
                mTitleText = "";
                mTitle.setText("");
            }
        } else if (type == 2) {
            if (!TextUtils.isEmpty(userInput.getText())) {
                mBrandText = userInput.getText().toString();
                mBrand.setText(mBrandText);
            } else {
                mBrandText = "";
                mBrand.setText("");
            }
        } else if (type == 3) {
            if (!TextUtils.isEmpty(userInput.getText())) {
                mColorText = userInput.getText().toString();
                mColor.setText(mColorText);
            } else {
                mColorText = "";
                mColor.setText("");
            }
        } else if (type == 4) {
            if (!TextUtils.isEmpty(userInput.getText())) {
                mSizeText = userInput.getText().toString();
                mSize.setText(mSizeText);
            } else {
                mSizeText = "";
                mSize.setText("");
            }
        } else if (type == 5) {
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

                int phoneCode = countryCodePicker.getSelectedCountryCodeAsInt();

                Prefs.putInt(Constants.KEY_REGISTER_PHONE_CODE, phoneCode);
                Prefs.putString(Constants.KEY_REGISTER_PHONE_NUMBER, mWhatsAppText);

                mWhatsAppText = "+" + String.valueOf(phoneCode) + mWhatsAppText;
                mWhatsapp.setText(mWhatsAppText);
                mWhatsapp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_whatsapp, 0, R.drawable.ic_edit, 0);
            } else {
                mWhatsAppText = "";
                mWhatsapp.setText("");
                mWhatsapp.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
            Prefs.putString(Constants.KEY_POST_WHATSAPP, mWhatsAppText);
        } else if (type == 6) {
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
        } else if (type == 7) {
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
        } else if (type == 8) {
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
    }

    @OnClick(R.id.addPhoto)
    void onAddPhotoClicked() {

        showCaptureChoice();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (bp != null && !bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        if(requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            String fileName = resultUri.getPath();

            int imageListSize = imageList.size();

            boolean isOk = (imageListSize + 1) <= 5;

            if (!isOk) {
                Snackbar.make(findViewById(android.R.id.content), R.string.snackbar_error_max_5_images2, Snackbar.LENGTH_LONG).show();
                return;
            }

            imageList.add(fileName);

            initData();

            if (imageList.size() > 1)
                mViewPager.setIndicatorVisibility(true);

            mPagerAdapter.notifyDataSetChanged();
            mViewPager.setCurrentItem(mPagerAdapter.getCount() - 1);
            mViewPager.invalidateBullets(mPagerAdapter);
            mViewPager.requestLayout();

        }else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {

                onError(getString(R.string.alert_error_title), getString(R.string.alert_error_desc));

                Crashlytics.logException(e);
            }

            @Override
            public void onImagesPicked(List<File> imageFiles, EasyImage.ImageSource source, int type) {

                if(source == EasyImage.ImageSource.CAMERA){
                    String destinationFileName = "cropped" + UUID.randomUUID().toString() + ".jpg";

                    UCrop.Options options = new UCrop.Options();
                    options.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                    options.setToolbarColor(getResources().getColor(R.color.colorAccent));
                    options.setActiveWidgetColor(getResources().getColor(R.color.colorAccent));

                    UCrop uCrop = UCrop.of(Uri.fromFile(imageFiles.get(0)), Uri.fromFile(new File(getCacheDir(), destinationFileName)));

                    uCrop.withOptions(options)
                            .start(EditPhotoActivity.this);
                }else{
                    int imageListSize = imageList.size();
                    int imageFileSize = imageFiles.size();

                    boolean isOk = (imageListSize + imageFileSize) <= 5;

                    if (!isOk) {
                        Snackbar.make(findViewById(android.R.id.content), R.string.snackbar_error_max_5_images2, Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    for (int i = 0; i < imageFiles.size(); i++) {

                        File file = imageFiles.get(i);

                        if (file == null)
                            continue;

                        imageList.add(file.getAbsolutePath());
                    }

                    initData();

                    if (imageList.size() > 1)
                        mViewPager.setIndicatorVisibility(true);

                    mPagerAdapter.notifyDataSetChanged();
                    mViewPager.setCurrentItem(mPagerAdapter.getCount() - 1);
                    mViewPager.invalidateBullets(mPagerAdapter);
                    mViewPager.requestLayout();
                }

            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(EditPhotoActivity.this);
                    if (photoFile != null) photoFile.delete();
                }
            }
        });
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

    private boolean checks(int page) {

        if (page == 1) {

            if (TextUtils.isEmpty(mAskPriceText)) {
                onError(getString(R.string.alert_ask_price_error_title), getString(R.string.alert_ask_price_error_desc));
                return false;
            }

            String regex = "[0-9, /,/.]+";
            if (!mAskPriceText.matches(regex)) {
                onError(getString(R.string.alert_ask_price_error_title), getString(R.string.alert_ask_price_error_desc2));
                return false;
            }

            if (mPosCategory == -1) {
                onError(getString(R.string.alert_category_error_title), getString(R.string.alert_category_error_desc));
                return false;
            }

        } else if (page == 2) {

            if (TextUtils.isEmpty(mTitleText)) {
                onError(getString(R.string.alert_article_error_title), getString(R.string.alert_article_error_desc));
                return false;
            }

        } else if (page == 3) {


            if (TextUtils.isEmpty(mInstagramText) && TextUtils.isEmpty(mWhatsAppText) && TextUtils.isEmpty(mEmailText)) {
                onError(getString(R.string.alert_contact_error_title), getString(R.string.alert_contact_error_desc));
                return false;
            }

            if (!TextUtils.isEmpty(mInstagramText)) {
                if (!mInstagramText.matches(Constants.INSTAGRAM_REGEX)) {
                    onError(getString(R.string.alert_instagram_error_title), getString(R.string.alert_instagram_error_desc));
                    return false;
                }
            }


        } else if (page == 4) {

            if (TextUtils.isEmpty(mFetchedLocation) && TextUtils.isEmpty(mInputLocation)) {
                onError(getString(R.string.alert_location_error_title), getString(R.string.alert_location_error_desc));
                return false;
            }

        }

        return true;
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


    @OnClick(R.id.back)
    void onBackClicked() {

        if (mViewPagerDuration.getCurrentItem() == 0) {
            setResult(RESULT_CANCELED);
            finish();
        } else if (mViewPagerDuration.getCurrentItem() == 1) {
            mViewPagerDuration.setCurrentItem(0);
            updateStatusText(PAGE_1);
        } else if (mViewPagerDuration.getCurrentItem() == 2) {
            mViewPagerDuration.setCurrentItem(1);
            updateStatusText(PAGE_2);
            mAddPhoto.setVisibility(View.VISIBLE);
        } else if (mViewPagerDuration.getCurrentItem() == 3) {
            mViewPagerDuration.setCurrentItem(2);
            updateStatusText(PAGE_3);
        } else if (mViewPagerDuration.getCurrentItem() == 4) {
            mViewPagerDuration.setCurrentItem(3);
            updateStatusText(PAGE_4);
            mNextText.setText(getString(R.string.btn_action_continue));
        } else if (mViewPagerDuration.getCurrentItem() == 5) {
            if (!mIsPosted) {
                mViewPagerDuration.setCurrentItem(4);
                updateStatusText(PAGE_5);
            }
        }
    }


    @OnClick(R.id.next)
    void onNextClicked() {


        if (mViewPagerDuration.getCurrentItem() == 0) {
            if (checks(1)) {
                mViewPagerDuration.setCurrentItem(1);
                updateStatusText(PAGE_2);
            }
        } else if (mViewPagerDuration.getCurrentItem() == 1) {
            if (checks(2)) {
                mAddPhoto.setVisibility(View.GONE);
                mViewPagerDuration.setCurrentItem(2);
                updateStatusText(PAGE_3);
            }
        } else if (mViewPagerDuration.getCurrentItem() == 2) {
            if (checks(3)) {
                mViewPagerDuration.setCurrentItem(3);
                updateStatusText(PAGE_4);
            }
        } else if (mViewPagerDuration.getCurrentItem() == 3) {
            if (checks(4)) {
                mViewPagerDuration.setCurrentItem(4);
                updateStatusText(PAGE_5);
                mNextText.setText(getString(R.string.btn_action_post));
            }
        } else if (mViewPagerDuration.getCurrentItem() == 4) {

            showDialog(getString(R.string.alert_progress_check_connection));

            NetworkUtils.isNetworkAvailable(mIncomingHandler, 3000);

        } else if (mIsPosted) {

            setResult(RESULT_OK);
            finish();
        }


    }

    public Handler mIncomingHandler = new Handler(msg -> {

        if (msg.what == 1) {


            if (mPostPromoted != 0) {

                dismissDialog();


                if (!checkSize()) {
                    Snackbar.make(findViewById(android.R.id.content), R.string.snackbar_error_purchase, Snackbar.LENGTH_SHORT).show();
                    return true;
                }

                if (!mIsReadyToPurchase) {
                    Snackbar.make(findViewById(android.R.id.content), R.string.snackbar_error_ready_purchase, Snackbar.LENGTH_SHORT).show();
                    return true;
                }

                String productId;

                if (mPostPromoted == 1) {
                    productId = Purchases.PURCHASE_PROMOTION_2;
                } else if (mPostPromoted == 2) {
                    productId = Purchases.PURCHASE_PROMOTION_3;
                } else if (mPostPromoted == 3) {
                    productId = Purchases.PURCHASE_PROMOTION_4;
                } else if (mPostPromoted == 4) {
                    productId = Purchases.PURCHASE_PROMOTION_5;
                } else {
                    productId = Purchases.PURCHASE_PROMOTION_6;
                }
                AndroidUtilities.globalQueue.postRunnable(() -> {
                    bp.consumePurchase(productId);
                    bp.purchase(EditPhotoActivity.this, productId);
                });
            } else
                makeRequest();


        } else {
            dismissDialog();
            Toast.makeText(getApplicationContext(), R.string.toast_error_no_internet_connection, Toast.LENGTH_SHORT).show();
        }


        return true;
    });

    private void makeRequest() {

        showDialog(getString(R.string.alert_progress_uploading));

        AndroidUtilities.globalQueue.postRunnable(() -> {
            try {

                RequestParams params = new RequestParams();
                params.add(Constants.ACTION, Constants.POST_ARTICLE);
                params.add(Constants.APP_KEY, Constants.APP_KEY_VALUE);
                params.add(Constants.FB_ACCT_ID, Prefs.getString(Constants.KEY_REGISTER_USER_ID, null));
                params.add(Constants.ITEM_PRICE, mAskPriceText);
                params.add(Constants.ITEM_GENDER, String.valueOf(mPosGender));
                params.add(Constants.ITEM_CATEGORY, String.valueOf(mPosCategory));
                params.add(Constants.ITEM_DELIVERY, String.valueOf(mPosSend));
                params.add(Constants.ITEM_TITLE, mTitleText);
                params.add(Constants.POST_COUNTRY,
                        Objects.equals(Prefs.getString(Constants.KEY_REGISTER_COUNTRY_CODE, null).toLowerCase(), Constants.BE) ? Constants.NL : Prefs.getString(Constants.KEY_REGISTER_COUNTRY_CODE, null).toLowerCase());
                params.add(Constants.ITEM_CONDITION, String.valueOf(mPosCondition));
                params.add(Constants.FB_NAME, Prefs.getString(Constants.KEY_REGISTER_DISPLAY_NAME, null));

                if (!TextUtils.isEmpty(mBrandText))
                    params.add(Constants.ITEM_BRAND, mBrandText);

                if (!TextUtils.isEmpty(mSizeText))
                    params.add(Constants.ITEM_SIZE, mSizeText);

                if (!TextUtils.isEmpty(mColorText))
                    params.add(Constants.ITEM_COLOR, mColorText);

                if (!TextUtils.isEmpty(mWhatsAppText)) {

                    if (mWhatsAppText.startsWith("+"))
                        mWhatsAppText = mWhatsAppText.substring(1);

                    params.add(Constants.CONTACT_WHATSAPP, mWhatsAppText);
                }

                if (!TextUtils.isEmpty(mInstagramText))
                    params.add(Constants.CONTACT_INSTAGRAM, mInstagramText);


                if (!TextUtils.isEmpty(mEmailText))
                    params.add(Constants.CONTACT_EMAIL, mEmailText);

                if (!TextUtils.isEmpty(mFetchedLocation)) {

                    params.add(Constants.LOCATION_PLACE, mFetchedLocation);

                    if (mFetchLocationLatitude != 0 && mFetchLocationLongitude != 0) {
                        params.add(Constants.LOCATION_LAT, String.valueOf(mFetchLocationLatitude));
                        params.add(Constants.LOCATION_LONG, String.valueOf(mFetchLocationLongitude));
                    }
                }

                if (!TextUtils.isEmpty(mInputLocation)) {
                    params.add(Constants.LOCATION_PLACE, mInputLocation);
                    if (mInputLocationLatitude != 0 && mInputLocationLongitude != 0) {
                        params.add(Constants.LOCATION_LAT, String.valueOf(mInputLocationLatitude));
                        params.add(Constants.LOCATION_LONG, String.valueOf(mInputLocationLongitude));
                    }
                }

                if (!TextUtils.isEmpty(mEncodedPos1) && !TextUtils.isEmpty(mEncodedPos1Thumb)) {
                    params.add(Constants.IMAGE_1, mEncodedPos1);
                    params.add(Constants.IMAGE_1_WIDTH, String.valueOf(mEncodedPos1Width));
                    params.add(Constants.IMAGE_1_HEIGHT, String.valueOf(mEncodedPos1Height));

                    params.add(Constants.THUMB_1, mEncodedPos1Thumb);
                    params.add(Constants.THUMB_1_WIDTH, String.valueOf(mEncodedPos1ThumbWidth));
                    params.add(Constants.THUMB_1_HEIGHT, String.valueOf(mEncodedPos1ThumbHeight));
                }

                if (!TextUtils.isEmpty(mEncodedPos2) && !TextUtils.isEmpty(mEncodedPos2Thumb)) {
                    params.add(Constants.IMAGE_2, mEncodedPos2);
                    params.add(Constants.THUMB_2, mEncodedPos2Thumb);
                }

                if (!TextUtils.isEmpty(mEncodedPos3) && !TextUtils.isEmpty(mEncodedPos3Thumb)) {
                    params.add(Constants.IMAGE_3, mEncodedPos3);
                    params.add(Constants.THUMB_3, mEncodedPos3Thumb);
                }

                if (!TextUtils.isEmpty(mEncodedPos4) && !TextUtils.isEmpty(mEncodedPos4Thumb)) {
                    params.add(Constants.IMAGE_4, mEncodedPos4);
                    params.add(Constants.THUMB_4, mEncodedPos4Thumb);
                }

                if (!TextUtils.isEmpty(mEncodedPos5) && !TextUtils.isEmpty(mEncodedPos5Thumb)) {
                    params.add(Constants.IMAGE_5, mEncodedPos5);
                    params.add(Constants.THUMB_5, mEncodedPos5Thumb);
                }


                params.add(Constants.POST_PROMOTED, String.valueOf(mPostPromoted));

                request.execute(params, new request.getResponseListener() {
                    @Override
                    public void onSuccess(JSONObject object) {

                        AndroidUtilities.runOnUIThread(() -> dismissDialog());

                        try {
                            int success = object.getInt(Constants.SUCCESS);

                            if (success == 1) {

                                mPostUrl = object.getString(Constants.POST_URL);

                                mPostUrl = AndroidUtilities.getShareUrl() + mPostUrl;

                                String postId = object.getString(Constants.POST_ID);

                                AndroidUtilities.runOnUIThread(() -> {

                                    Toast.makeText(getApplicationContext(), R.string.alert_progress_uploading_success, Toast.LENGTH_SHORT).show();
                                    mViewPagerDuration.setCurrentItem(5);
                                    mBack.setVisibility(View.GONE);
                                    mNextText.setText(getString(R.string.btn_action_view_post));
                                    mNext.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_box_watch));
                                    mStatusText.setVisibility(View.GONE);
                                    mAddPhoto.setVisibility(View.GONE);
                                    mIsPosted = true;

                                    long hour = 1000 * 60 * 60;
                                    long day = 1000 * 60 * 60 * 24;
                                    long expireTime = AndroidUtilities.getCurrentTime() + day * 28;
                                    helper.insertPostObject(0, Integer.valueOf(postId), mTitleText, expireTime);

                                    if (mPostPromoted > 0) {
                                        long promotionExpireTime = AndroidUtilities.getCurrentTime();
                                        long warningTime = 0;

                                        switch (mPostPromoted) {
                                            case 1:
                                                promotionExpireTime += hour * 6;
                                                warningTime += hour * 1.5;
                                                break;
                                            case 2:
                                                promotionExpireTime += day;
                                                warningTime += hour * 1.5;
                                                break;
                                            case 3:
                                                promotionExpireTime += day * 3;
                                                warningTime += day * 1.5;
                                                break;
                                            case 4:
                                                promotionExpireTime += day * 7;
                                                warningTime += day * 1.5;
                                                break;
                                            case 5:
                                                promotionExpireTime += day * 30;
                                                warningTime += day * 2;
                                                break;

                                        }
                                        promotionExpireTime = promotionExpireTime - warningTime;
                                        helper.insertPostObject(1, Integer.valueOf(postId), mTitleText, promotionExpireTime);
                                    }
                                    DailyNotificationJob.schedule();

                                    if (mPostPromoted == 1)
                                        JobLauncher.schedulePromotionJob(5, 6);
                                    else if (mPostPromoted == 2)
                                        JobLauncher.schedulePromotionJob(23, 24);

                                });

                            } else {
                                AndroidUtilities.runOnUIThread(() -> onError(getString(R.string.alert_error_title), getString(R.string.alert_error_desc2)));
                            }

                        } catch (JSONException e) {
                            Crashlytics.logException(e);
                            AndroidUtilities.runOnUIThread(() -> onError(getString(R.string.alert_error_title), getString(R.string.alert_error_desc)));
                        }


                    }

                    @Override
                    public void onFailure(String error) {

                        AndroidUtilities.runOnUIThread(() -> {
                            dismissDialog();
                            onError(getString(R.string.alert_error_title), error);
                        });


                    }
                });

            } catch (Exception e) {
                Crashlytics.logException(e);

                AndroidUtilities.runOnUIThread(() -> {
                    dismissDialog();
                    AndroidUtilities.runOnUIThread(() -> onError(getString(R.string.alert_error_title), getString(R.string.alert_error_desc)));
                });
            }
        });

    }


    private void getValuesFromCity(String city) {

        if (Geocoder.isPresent()) {
            try {
                Geocoder gc = new Geocoder(this);
                List<Address> addresses = gc.getFromLocationName(city, 5); // get the found Address Objects

                if (addresses.size() > 0) {

                    Address address = addresses.get(0);

                    mInputLocationLatitude = address.getLatitude();
                    mInputLocationLongitude = address.getLongitude();

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
        EditPhotoActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);

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
                    onError(getString(R.string.alert_location_error_title2), getString(R.string.alert_location_error_desc_3));
            } catch (IOException e) {
                Crashlytics.logException(e);
                onError(getString(R.string.alert_location_error_title2), getString(R.string.alert_location_error_desc_3));
            }

            mLocationProgress.setVisibility(View.GONE);
            mLocation.setVisibility(View.VISIBLE);

        });

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


    @Override
    public void onBackPressed() {

        if (mViewPagerDuration.getCurrentItem() == 0) {
            setResult(RESULT_CANCELED);
            super.onBackPressed();
        } else if (mViewPagerDuration.getCurrentItem() == 1) {
            mViewPagerDuration.setCurrentItem(0);
            updateStatusText(PAGE_1);
        } else if (mViewPagerDuration.getCurrentItem() == 2) {
            mViewPagerDuration.setCurrentItem(1);
            updateStatusText(PAGE_2);
            mAddPhoto.setVisibility(View.VISIBLE);
        } else if (mViewPagerDuration.getCurrentItem() == 3) {
            mViewPagerDuration.setCurrentItem(2);
            updateStatusText(PAGE_3);
        } else if (mViewPagerDuration.getCurrentItem() == 4) {
            mViewPagerDuration.setCurrentItem(3);
            updateStatusText(PAGE_4);
            mNextText.setText(getString(R.string.btn_action_continue));
        } else if (mViewPagerDuration.getCurrentItem() == 5) {
            if (!mIsPosted) {
                mViewPagerDuration.setCurrentItem(4);
                updateStatusText(PAGE_5);
            }
        }

    }

    @Override
    public void onDestroy() {

        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }
}
