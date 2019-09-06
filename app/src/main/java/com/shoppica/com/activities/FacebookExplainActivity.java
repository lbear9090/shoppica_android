package com.shoppica.com.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.shoppica.com.R;
import com.shoppica.com.facebook.FacebookHelper;
import com.shoppica.com.facebook.FacebookListener;
import com.shoppica.com.jobs.JobLauncher;
import com.shoppica.com.preferences.Prefs;
import com.shoppica.com.utils.AndroidUtilities;
import com.shoppica.com.utils.NetworkUtils;
import com.shoppica.com.utils.ViewUtil;
import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;

import org.json.JSONException;

import butterknife.BindView;
import butterknife.OnClick;

import static com.shoppica.com.constants.Constants.KEY_REGISTER_DISPLAY_NAME;
import static com.shoppica.com.constants.Constants.KEY_REGISTER_GENDER;
import static com.shoppica.com.constants.Constants.KEY_REGISTER_PROFILE_URL;
import static com.shoppica.com.constants.Constants.KEY_REGISTER_SOCIAL_AUTH_TOKEN;
import static com.shoppica.com.constants.Constants.KEY_REGISTER_SOCIAL_ID;
import static com.shoppica.com.constants.Constants.KEY_REGISTER_USER_ID;

public class FacebookExplainActivity extends FullBaseActivity implements FacebookListener {

    public static final String TAG = FacebookExplainActivity.class.getSimpleName();

    public static final String FAVORITE = "favorite";

    private static final String MALE = "male";
    private static final String FIELDS = "fields";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String GENDER = "gender";
    private static final String PICTURE = "picture";
    private static final String DATA = "data";
    private static final String URL = "url";


    private FacebookHelper mFacebook;
    private boolean mIsFavoriteLogin;

    @BindView(R.id.back)
    ImageButton mBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_explain);

        mFacebook = new FacebookHelper(this);

        if (getIntent() != null)
            mIsFavoriteLogin = getIntent().getBooleanExtra(FAVORITE, false);


        initDrawable();
    }

    private void initDrawable() {

        Drawable backDrawable = AndroidUtilities.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(48), ContextCompat.getColor(getApplicationContext(),
                R.color.transparent), ContextCompat.getColor(getApplicationContext(), R.color.white_85));

        mBack.setBackground(backDrawable);

    }

    @OnClick(R.id.back)
    void onBackClicked() {
        finish();
    }

    @OnClick(R.id.btnContinue)
    void onContinueClicked() {

        if (!NetworkUtils.isConnected()) {

            onErrorFb(getString(R.string.alert_error_network_title), getString(R.string.alert_error_internet_facebook_desc), getString(R.string.btn_action_retry), true);

            return;
        }

        mFacebook.performSignIn(this);


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

                    if (!mIsFavoriteLogin) {
                        Intent intent = new Intent(getApplicationContext(), CameraExplainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                        startActivity(intent);
                        overridePendingTransition(R.anim.into_anim_slide_from_right, R.anim.into_anim_slide_to_left);
                    }


                    Toast.makeText(getApplicationContext(), getString(R.string.toast_notice_login_success), Toast.LENGTH_SHORT).show();

                    finish();

                });

        request.setParameters(parameters);
        request.executeAsync();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mFacebook.onActivityResult(requestCode, resultCode, data);
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

        builder.setPositiveButton(positiveBtnMsg, (dialog, which) -> onContinueClicked());

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
    public void onFBSignOut() {

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.stay, R.anim.slide_bottom_out);
    }
}
