package com.shoppica.com.activities;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.github.chrisbanes.photoview.PhotoView;
import com.shoppica.com.R;
import com.shoppica.com.constants.Constants;
import com.shoppica.com.dialogs.ReportDialog;
import com.shoppica.com.jobs.JobLauncher;
import com.shoppica.com.notifications.NotificationCenter;
import com.shoppica.com.objects.FavoriteObject;
import com.shoppica.com.objects.FeedObject;
import com.shoppica.com.preferences.Prefs;
import com.shoppica.com.utils.AndroidUtilities;
import com.shoppica.com.utils.ViewUtil;
import com.shoppica.com.utils.typeface.SpannableBuilder;
import com.shoppica.com.view.GlideApp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;

public class ViewArticleActivity extends BaseActivity implements NotificationCenter.NotificationCenterDelegate, ReportDialog.OnCompleteListener {

    public static final String TAG = ViewArticleActivity.class.getSimpleName();

    public static final String MEDIUM = "fonts/MONTSERRAT-MEDIUM.TTF";
    public static final String BOLD = "fonts/MONTSERRAT-BOLD.TTF";

    public static final String OBJECT = "object";
    public static final String OWN_POST = "ownPost";

    @BindView(R.id.txtSold)
    TextView mSold;

    @BindView(R.id.back)
    ImageButton mBack;

    @BindView(R.id.share)
    ImageButton mShare;

    @BindView(R.id.favorite)
    ImageButton mFavorite;

    @BindView(R.id.multple_images)
    ImageButton mMultiple;

    @BindView(R.id.articleMore)
    ImageButton mMore;

    @BindView(R.id.imageView)
    PhotoView mImageView;

    @BindView(R.id.toolbar_name)
    TextView mToolbar;

    @BindView(R.id.article_title)
    TextView mArticleTitle;

    @BindView(R.id.article_price)
    TextView mArticlePrice;

    @BindView(R.id.article_state)
    TextView mArticleState;

    @BindView(R.id.article_state_value)
    TextView mArticleStateValue;

    @BindView(R.id.article_brand)
    TextView mArticleBrand;

    @BindView(R.id.article_brand_value)
    TextView mArticleBrandValue;

    @BindView(R.id.article_color)
    TextView mArticleColor;

    @BindView(R.id.article_color_value)
    TextView mArticleColorValue;

    @BindView(R.id.article_size)
    TextView mArticleSize;

    @BindView(R.id.article_size_value)
    TextView mArticleSizeValue;

    @BindView(R.id.article_gender)
    TextView mArticleGender;

    @BindView(R.id.article_gender_value)
    TextView mArticleGenderValue;

    @BindView(R.id.article_delivery)
    TextView mArticleDelivery;

    @BindView(R.id.article_delivery_value)
    TextView mArticleDeliveryValue;

    @BindView(R.id.contactSubTitle)
    TextView mContactSubTitle;

    @BindView(R.id.contactSubTitle2)
    TextView mContactSubTitle2;

    @BindView(R.id.count)
    TextView mCount;

    @BindView(R.id.contactWhatsAppView)
    View mWhatsAppView;

    @BindView(R.id.contactWhatsAppTitle)
    View mWhatsAppTitle;

    @BindView(R.id.contactInstagramView)
    View mInstagramView;

    @BindView(R.id.contactInstagramTitle)
    View mInstagramTitle;

    @BindView(R.id.contactEmailView)
    View mEmailView;

    @BindView(R.id.contactEmailTitle)
    View mEmailTitle;

    @BindView(R.id.postViews)
    TextView mPostViews;

    @BindView(R.id.postTime)
    TextView mPostTime;

    @BindView(R.id.userActive)
    TextView mUserActive;

    private ArrayList<String> imageList;

    private boolean mIsFavorite, mIsFavoriteChanged;

    private FeedObject mFeedObject;

    private boolean mOwnPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_article);

        init();

        initDrawables();

        initImage();
    }

    @SuppressLint("SetTextI18n")
    private void init() {

        try {

            Intent intent = getIntent();

            if (intent == null) {
                Toast.makeText(this, R.string.toast_error_intent_null, Toast.LENGTH_SHORT).show();
                AndroidUtilities.runOnUIThread(this::finish, 2000);
                return;
            }


            if (intent.hasExtra(OWN_POST))
                mOwnPost = intent.getBooleanExtra(OWN_POST, false);

            mFeedObject = intent.getExtras().getParcelable(OBJECT);

            if (mFeedObject == null) {
                Toast.makeText(this, R.string.toast_error_intent_null, Toast.LENGTH_SHORT).show();
                AndroidUtilities.runOnUIThread(this::finish, 2000);
                return;
            }

            String image1 = mFeedObject.getImage1();
            String image2 = mFeedObject.getImage2();
            String image3 = mFeedObject.getImage3();
            String image4 = mFeedObject.getImage4();
            String image5 = mFeedObject.getImage5();

            imageList = new ArrayList<>();

            if (image1 != null && !image1.isEmpty() && !image1.equals("null"))
                imageList.add(image1);

            if (image2 != null && !image2.isEmpty() && !image2.equals("null"))
                imageList.add(image2);

            if (image3 != null && !image3.isEmpty() && !image3.equals("null"))
                imageList.add(image3);

            if (image4 != null && !image4.isEmpty() && !image4.equals("null"))
                imageList.add(image4);

            if (image5 != null && !image5.isEmpty() && !image5.equals("null"))
                imageList.add(image5);

            if (imageList == null || imageList.size() == 0) {
                Toast.makeText(this, R.string.toast_error_intent_null, Toast.LENGTH_SHORT).show();
                AndroidUtilities.runOnUIThread(this::finish, 2000);
                return;
            }

            if (imageList.size() > 1) {
                mCount.setVisibility(View.VISIBLE);
                mCount.setText(String.valueOf(imageList.size()));
            } else {
                mCount.setVisibility(View.GONE);
            }

            int status = Integer.valueOf(mFeedObject.getPostStatus());

            if(status == 2){
                mSold.setVisibility(View.VISIBLE);
                mSold.setText(getResources().getText(R.string.view_article_sold));
                mSold.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            }else if(status == 1){
                mSold.setVisibility(View.VISIBLE);
                mSold.setText(getResources().getText(R.string.adapter_reserved));
                mSold.setBackgroundColor(getResources().getColor(R.color.colorReserved));
            }else{
                mSold.setVisibility(View.GONE);
            }
            SpannableBuilder builder = new SpannableBuilder(getApplicationContext());
            builder.createStyle()
                    .setFont(MEDIUM)
                    .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorTextSubTitle))
                    .apply();
            builder.append(getString(R.string.spannable_article_offered_by))
                    .append(" ");
            builder.clearStyle();
            builder.createStyle().setFont(BOLD)
                    .setColor(ContextCompat.getColor(getApplicationContext(), R.color.black))
                    .apply();
            builder.append(mFeedObject.getFbName());
            builder.append(" ");
            builder.clearStyle();
            builder.createStyle()
                    .setFont(MEDIUM)
                    .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorTextSubTitle))
                    .apply();
            builder.append(getString(R.string.spannable_article_sold_from))
                    .append(" ");
            builder.clearStyle();
            builder.createStyle().setFont(BOLD)
                    .setColor(ContextCompat.getColor(getApplicationContext(), R.color.black))
                    .apply();
            builder.append(mFeedObject.getLocationPlace());
            builder.clearStyle();
            builder.createStyle()
                    .setFont(MEDIUM)
                    .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorTextSubTitle))
                    .apply();
            builder.append(". ");

            int delivery = Integer.parseInt(mFeedObject.getItemDelivery());

            if (delivery == 0)
                builder.append(getString(R.string.view_article_contact_option_delivery1));
            else if (delivery == 1)
                builder.append(getString(R.string.view_article_contact_option_delivery2));
            else if (delivery == 2)
                builder.append(getString(R.string.view_article_contact_option_delivery3));

            mContactSubTitle.setText(builder.build());

            mArticleTitle.setText(mFeedObject.getItemTitle());
            String priceText = "$";
            String price = mFeedObject.getItemPrice();
            price = price.replace(",", ".");

            switch (mFeedObject.getPostCountry()) {
                case Constants.AU:
                    priceText = "A$";
                    price = price.replace(",", ".");
                    break;
                case Constants.NZ:
                    priceText = "NZ$";
                    price = price.replace(",", ".");
                    break;
                case Constants.BE:
                case Constants.NL:
                    priceText = "€";
                    price = price.replace(".", ",");
                    break;
                case Constants.GB:
                case Constants.IE:
                    priceText = "£";
                    price = price.replace(",", ".");
                    break;

            }
            mArticlePrice.setText(priceText + price);

            String[] conditions = getResources().getStringArray(R.array.condition);
            mArticleStateValue.setText(conditions[Integer.valueOf(mFeedObject.getItemCondition())]);

            String brand = mFeedObject.getItemBrand();
            String color = mFeedObject.getItemColor();
            String size = mFeedObject.getItemSize();

            if (brand != null && !brand.isEmpty() && !brand.equals("null"))
                mArticleBrandValue.setText(brand);

            if (color != null && !color.isEmpty() && !color.equals("null"))
                mArticleColorValue.setText(color);

            if (size != null && !size.isEmpty() && !size.equals("null"))
                mArticleSizeValue.setText(mFeedObject.getItemSize());

            String[] gender = getResources().getStringArray(R.array.gender);
            mArticleGenderValue.setText(gender[Integer.valueOf(mFeedObject.getItemGender())]);

            String[] send = getResources().getStringArray(R.array.send);
            mArticleDeliveryValue.setText(send[delivery]);

            if (!Prefs.getBoolean(Constants.FIRST_TIME_ZOOM, false)) {
                onError(getString(R.string.view_article_zoom_title), getString(R.string.view_article_zoom_desc));
                Prefs.putBoolean(Constants.FIRST_TIME_ZOOM, true);
            }

            String whatsApp = mFeedObject.getContactWhatsApp();
            String instagram = mFeedObject.getContactInstagram();
            String email = mFeedObject.getContactEmail();

            Log.d(TAG, "whatsApp ==" + whatsApp);
            Log.d(TAG, "instagram ==" + instagram);
            Log.d(TAG, "email ==" + email);

            boolean isWhatsAppContact = whatsApp != null && !whatsApp.isEmpty() && !whatsApp.equals("null");
            boolean isInstagramContact = instagram != null && !instagram.isEmpty() && !instagram.equals("null");
            boolean isEmailContact = email != null && !email.isEmpty() && !email.equals("null");

            if (!isInstagramContact) {
                if (isWhatsAppContact) {
                    ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) mWhatsAppView.getLayoutParams();
                    marginParams.setMargins(AndroidUtilities.dp(20), AndroidUtilities.dp(25), AndroidUtilities.dp(20), 0);
                    mWhatsAppView.setLayoutParams(marginParams);
                } else {
                    ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) mEmailView.getLayoutParams();
                    marginParams.setMargins(AndroidUtilities.dp(20), AndroidUtilities.dp(25), AndroidUtilities.dp(20), 0);
                    mEmailView.setLayoutParams(marginParams);
                }
            }


            if (!isWhatsAppContact) {
                mWhatsAppView.setVisibility(View.GONE);
                mWhatsAppTitle.setVisibility(View.GONE);
            }

            if (!isInstagramContact) {
                mInstagramView.setVisibility(View.GONE);
                mInstagramTitle.setVisibility(View.GONE);
            }

            if (!isEmailContact) {
                mEmailView.setVisibility(View.GONE);
                mEmailTitle.setVisibility(View.GONE);
            }

            boolean isExisting = helper.isExisting(Integer.parseInt(mFeedObject.getPostId()));

            if (isExisting) {
                mFavorite.setImageResource(R.drawable.ic_unfavorite_article);
                mIsFavorite = true;
                mIsFavoriteChanged = true;
            } else {
                mFavorite.setImageResource(R.drawable.ic_favorite_article);
                mIsFavorite = false;
                mIsFavoriteChanged = false;
            }


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (findViewById(android.R.id.content) != null) {
                    findViewById(android.R.id.content).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }

            NotificationCenter.getInstance().addObserver(this, NotificationCenter.addViewArticleObserver);

            String postViews = mFeedObject.getPostViews();

            Log.d(TAG, "postViews == " + postViews);
            Log.d(TAG, "postViews int == " + Integer.parseInt(postViews));

            if (postViews != null && !postViews.isEmpty() && !postViews.equals("0"))
                mPostViews.setText(getString(R.string.view_article_post_views, mFeedObject.getPostViews()));
            else
                mPostViews.setText(getString(R.string.view_article_post_views_0));


            int views = Integer.parseInt(mFeedObject.getPostViews());
            views++;
            mFeedObject.setPostViews(String.valueOf(views));

            Log.d(TAG, "postViewsAfter == "  + mFeedObject.getPostViews());

            mPostTime.setText(getString(R.string.view_article_post_time, AndroidUtilities.getTimeAgo(Long.valueOf(mFeedObject.getPostTime()))));

            mUserActive.setText(AndroidUtilities.getTimeAgoMember(Long.valueOf(mFeedObject.getTimeActive())));

            if (mOwnPost)
                mMore.setVisibility(View.GONE);


            JobLauncher.scheduleViewedArticleService(mFeedObject.getPostId());

        } catch (Exception e) {
            Crashlytics.logException(e);
        }

    }

    private void initDrawables() {

        Drawable backDrawable = AndroidUtilities.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(48), ContextCompat.getColor(getApplicationContext(),
                R.color.transparent), ContextCompat.getColor(getApplicationContext(), R.color.colorFilterPressed));

        Drawable favoriteDrawable = AndroidUtilities.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(48), ContextCompat.getColor(getApplicationContext(),
                R.color.transparent), ContextCompat.getColor(getApplicationContext(), R.color.colorFilterPressed));

        Drawable shareDrawable = AndroidUtilities.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(48), ContextCompat.getColor(getApplicationContext(),
                R.color.transparent), ContextCompat.getColor(getApplicationContext(), R.color.colorFilterPressed));

        Drawable multipleDrawable = AndroidUtilities.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(48), ContextCompat.getColor(getApplicationContext(),
                R.color.transparent), ContextCompat.getColor(getApplicationContext(), R.color.white_50));

        Drawable moreDrawable = AndroidUtilities.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(48), ContextCompat.getColor(getApplicationContext(),
                R.color.transparent), ContextCompat.getColor(getApplicationContext(), R.color.white_50));


        mBack.setBackground(backDrawable);
        mFavorite.setBackground(favoriteDrawable);
        mMultiple.setBackground(multipleDrawable);
        mMore.setBackground(moreDrawable);
        mShare.setBackground(shareDrawable);
    }

    private void initImage() {

        String firstUrl = imageList.get(0);

        int imageWidth = Integer.parseInt(mFeedObject.getImage1Width());
        int imageHeight = Integer.parseInt(mFeedObject.getImage1Height());

        int screenWidth = AndroidUtilities.displaySize.x;

        float ratio = ((float) screenWidth / imageWidth);
        imageHeight = Math.round(ratio * imageHeight);

        mImageView.getLayoutParams().width = screenWidth;
        mImageView.getLayoutParams().height = imageHeight;
        mImageView.requestLayout();

        int finalImageHeight = imageHeight;

        AndroidUtilities.runOnUIThread(() -> GlideApp.with(getApplicationContext())
                .load(firstUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(new RequestOptions().centerInside().override(screenWidth, finalImageHeight)).placeholder(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.transparent))).into(mImageView));

    }

    private void openReportDialog() {
        ReportDialog dialog = new ReportDialog();
        showDialogFragmentSupport(dialog, ReportDialog.TAG);
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

    private void onErrorLogin(String titleMsg, String errorMsg) {

        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.into_alert_view, null);

        TextView errorTitle = ViewUtil.findById(view, R.id.into_title_error);
        TextView errorText = ViewUtil.findById(view, R.id.into_desc_error);

        if (!TextUtils.isEmpty(titleMsg))
            errorTitle.setText(titleMsg);

        if (!TextUtils.isEmpty(errorMsg))
            errorText.setText(errorMsg);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setView(view);

        builder.setPositiveButton(R.string.btn_action_login, (dialogInterface, i) -> {
            Intent intent = new Intent(getApplicationContext(), FacebookExplainActivity.class);
            intent.putExtra(FacebookExplainActivity.FAVORITE, true);
            startActivityForResult(intent, 200);
            overridePendingTransition(R.anim.slide_bottom_in, R.anim.stay);
        });
        builder.setNegativeButton(R.string.btn_action_cancel, null);

        builder.setCancelable(false);
        builder.show();
    }


    private void onInstagramExplain(String titleMsg, String errorMsg) {

        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.into_alert_view, null);

        TextView errorTitle = ViewUtil.findById(view, R.id.into_title_error);
        TextView errorText = ViewUtil.findById(view, R.id.into_desc_error);

        if (!TextUtils.isEmpty(titleMsg))
            errorTitle.setText(titleMsg);

        if (!TextUtils.isEmpty(errorMsg))
            errorText.setText(errorMsg);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setView(view);

        builder.setPositiveButton(R.string.btn_action_show, (dialogInterface, i) -> {


            Intent intent = new Intent(getApplicationContext(), IntroActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.into_anim_slide_from_right, R.anim.into_anim_slide_to_left);
        });
        builder.setNegativeButton(R.string.btn_action_skip, null);

        builder.setCancelable(false);
        builder.show();
    }


    @OnClick(R.id.multple_images)
    void onMultipleImageClicked() {


        Intent intent = new Intent(getApplicationContext(), ViewPagerActivity.class);
        intent.putStringArrayListExtra(ViewPagerActivity.PHOTOS, imageList);
        startActivity(intent);
        overridePendingTransition(R.anim.into_anim_slide_from_right, R.anim.into_anim_slide_to_left);


    }

    @OnClick(R.id.articleMore)
    void onArticleMoreClicked() {


        if (mFeedObject.getReportedUser()) {
            Snackbar.make(findViewById(android.R.id.content), R.string.snackbar_error_report, Snackbar.LENGTH_SHORT).show();
            return;
        }

        openReportDialog();

    }


    @OnClick(R.id.favorite)
    void onFavoriteClicked() {

        boolean loggedIn = AccessToken.getCurrentAccessToken() != null;

        if (!loggedIn) {
            onErrorLogin(getString(R.string.view_article_login_fb_title), getString(R.string.view_article_login_fb_desc));
            return;
        }

        if (mIsFavorite) {
            mFavorite.setImageResource(R.drawable.ic_favorite_article);
            mIsFavorite = false;
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.snackbar_notice_favorite_removed), Snackbar.LENGTH_SHORT).show();
        } else {
            mFavorite.setImageResource(R.drawable.ic_unfavorite_article);
            mIsFavorite = true;
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.snackbar_notice_favorite_added), Snackbar.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.contactWhatsAppView)
    void onWhatsAppClicked() {

        if (mOwnPost) {
            Snackbar.make(findViewById(android.R.id.content), R.string.snackbar_error_contact_yourself, Snackbar.LENGTH_SHORT).show();
            return;
        }

        openWhatsApp();
    }

    @OnClick(R.id.contactInstagramView)
    void onInstagramClicked() {

        if (mOwnPost) {
            Snackbar.make(findViewById(android.R.id.content), R.string.snackbar_error_contact_yourself, Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (!Prefs.getBoolean(Constants.SHOWED_INSTAGRAM_EXPLAIN, false)) {
            onInstagramExplain(getString(R.string.view_activitiy_instagram_explain_title), getString(R.string.view_activitiy_instagram_explain_desc));
            Prefs.putBoolean(Constants.SHOWED_INSTAGRAM_EXPLAIN, true);
            return;
        }

        openInstagram();
    }

    @OnClick(R.id.contactEmailView)
    void onEmailClicked() {

        if (mOwnPost) {
            Snackbar.make(findViewById(android.R.id.content), R.string.snackbar_error_contact_yourself, Snackbar.LENGTH_SHORT).show();
            return;
        }

        openEmail();
    }

    @OnClick(R.id.back)
    void onBackClicked() {


        NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateArticle, mFeedObject.getPostId(), mFeedObject.getPostViews());


        finish();


    }

    @OnClick(R.id.share)
    void onShareClicked() {
        try {


            String url = AndroidUtilities.getShareUrl() + mFeedObject.getPostId() + "/" + mFeedObject.getPostUrl() + "/";
            String shareBody = getString(R.string.view_article_share_text_2, url, mFeedObject.getFbName(), mFeedObject.getLocationPlace());
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share"));
        } catch (Exception e) {
            Crashlytics.logException(e);
            onError(getString(R.string.view_article_share_error_title), getString(R.string.view_article_share_error_desc));
        }

    }

    private void onBackUpdated() {

        Log.d(TAG, "mIsFavoriteChanged == " + mIsFavoriteChanged);
        Log.d(TAG, "mIsFavorite == " + mIsFavorite);

        if (mIsFavoriteChanged != mIsFavorite) {
            if (mIsFavorite) {
                helper.insertFavoriteObject(Integer.valueOf(mFeedObject.getPostId()));
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.addToFavoritesFragment, mFeedObject);
                JobLauncher.scheduleFavoriteJob(Integer.valueOf(mFeedObject.getPostId()), Constants.ADD_TO_FAVORITES);
            } else {
                helper.deleteObject(Integer.valueOf(mFeedObject.getPostId()));
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.removeFromFavoritesFragment, mFeedObject);
                JobLauncher.scheduleFavoriteJob(Integer.valueOf(mFeedObject.getPostId()), Constants.DELETE_FROM_FAVORITES);
            }
        }


    }

    private void openWhatsApp() {

        if (!isAppInstalled(Constants.PACKAGE_NAME_WHATSAPP)) {
            onError(getString(R.string.view_article_not_installed_error_whatsapp_title), getString(R.string.view_article_not_installed_error_whatsapp_desc, mFeedObject.getFbName()), Constants.PACKAGE_NAME_WHATSAPP);
            return;
        }

        try {
            String url = AndroidUtilities.getShareUrl() + mFeedObject.getPostId() + "/" + mFeedObject.getPostUrl() + "/";

            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Instagram Text", getString(R.string.view_article_instagram_send, mFeedObject.getFbName(), mFeedObject.getItemTitle(), url));
            Objects.requireNonNull(clipboard).setPrimaryClip(clip);
            Toast.makeText(ViewArticleActivity.this, getString(R.string.toast_sen_text_copied), Toast.LENGTH_SHORT).show();
            String smsNumber = mFeedObject.getContactWhatsApp() + "@s.whatsapp.net";

            Uri uri = Uri.parse("smsto:" + smsNumber);
            Intent i = new Intent(Intent.ACTION_SENDTO, uri);
            i.putExtra("jid", smsNumber);
            i.setPackage("com.whatsapp");
            startActivity(i);

        } catch (Exception e) {
            Crashlytics.logException(e);
            onError(getString(R.string.view_article_send_error_whatsapp_title), getString(R.string.view_article_send_error_whatsapp_desc, mFeedObject.getFbName()));
        }
    }

    private void openInstagram() {

        if (!isAppInstalled(Constants.PACKAGE_NAME_INSTAGRAM)) {
            onError(getString(R.string.view_article_not_installed_error_instagram_title), getString(R.string.view_article_not_installed_error_instagram_desc2), Constants.PACKAGE_NAME_MESSENGER);
            return;
        }

        try {
            String url = AndroidUtilities.getShareUrl() + mFeedObject.getPostId() + "/" + mFeedObject.getPostUrl() + "/";

            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Instagram Text", getString(R.string.view_article_instagram_send, mFeedObject.getFbName(), mFeedObject.getItemTitle(), url));
            Objects.requireNonNull(clipboard).setPrimaryClip(clip);
            Toast.makeText(ViewArticleActivity.this, getString(R.string.toast_sen_text_copied), Toast.LENGTH_SHORT).show();

            Uri uri = Uri.parse("http://instagram.com/_u/" + mFeedObject.getContactInstagram());
            Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

            likeIng.setPackage("com.instagram.android");

            try {
                startActivity(likeIng);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://instagram.com/" + mFeedObject.getContactInstagram())));
            }


        } catch (Exception e) {
            Crashlytics.logException(e);
            onError(getString(R.string.view_article_instagram_error_title), getString(R.string.view_article_instagram_error_desc, mFeedObject.getFbName()));
        }

    }


    private void openEmail() {

        try {
            String url = "https://www.kledingkoopjes.net/" + mFeedObject.getPostId() + "/" + mFeedObject.getPostUrl() + "/";

            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            emailIntent.setType("vnd.android.cursor.item/email");
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{mFeedObject.getContactEmail()});
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.view_article_send_email_title, mFeedObject.getItemTitle()));
            emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.view_article_send_email_desc, mFeedObject.getFbName(), mFeedObject.getItemTitle(), url));
            startActivity(Intent.createChooser(emailIntent, getString(R.string.view_article_send_chooser)));

        } catch (Exception e) {
            Crashlytics.logException(e);
            onError(getString(R.string.view_article_email_error_title), getString(R.string.view_article_email_error_desc, mFeedObject.getFbName()));
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


    @Override
    protected void onDestroy() {
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.addViewArticleObserver);
        onBackUpdated();
        super.onDestroy();
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.addViewArticleObserver) {

            List<FavoriteObject> favoriteObjectList = helper.getFavoriteObjects();
            Log.d(TAG, "RESULT == OK");
            for (int i = 0; i < favoriteObjectList.size(); i++) {

                FavoriteObject favoriteObject = favoriteObjectList.get(i);

                if (favoriteObject == null)
                    continue;

                int postId = favoriteObject.getPostId();

                Log.d(TAG, "postId == " + postId);

                if (postId == Integer.valueOf(mFeedObject.getPostId())) {
                    mFavorite.setImageResource(R.drawable.ic_unfavorite_article);
                    mIsFavorite = true;
                    mIsFavoriteChanged = true;

                } else {
                    mFavorite.setImageResource(R.drawable.ic_favorite_article);
                    mIsFavorite = false;
                    mIsFavoriteChanged = false;
                }
            }

        }
    }




    @Override
    public void onComplete(int type, String message) {

        if (!TextUtils.isEmpty(message) && message.length() > 250) {
            Snackbar.make(findViewById(android.R.id.content), R.string.snackbar_error_report_length, Snackbar.LENGTH_SHORT).show();
            return;
        }

        JobLauncher.scheduleReportUser(Integer.parseInt(mFeedObject.getPostId()), type, message);
        mFeedObject.setReportedUser(true);

        dismissDialogFragmentSupport(ReportDialog.TAG);
    }

    @Override
    public void finish() {
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateArticle, mFeedObject.getPostId(), mFeedObject.getPostViews());
        super.finish();
    }
}
