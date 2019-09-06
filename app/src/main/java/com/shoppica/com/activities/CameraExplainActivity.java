package com.shoppica.com.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.shoppica.com.R;
import com.shoppica.com.utils.AndroidUtilities;
import com.shoppica.com.utils.ViewUtil;
import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class CameraExplainActivity extends FullBaseActivity {

    public static final String TAG = CameraExplainActivity.class.getSimpleName();

    @BindView(R.id.back)
    ImageButton mBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_explain);

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


        showCaptureChoice();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 200) {
                setResult(RESULT_OK);
                finish();
                return;
            }

        } else if (resultCode == RESULT_CANCELED) {
            finish();
        }

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {

                onError(getString(R.string.alert_error_title), getString(R.string.alert_error_desc));

                Crashlytics.logException(e);
            }

            @Override
            public void onImagesPicked(@NonNull List<File> imageFiles, EasyImage.ImageSource source, int type) {


                ArrayList<String> fileNames = new ArrayList<>();

                for (int i = 0; i < imageFiles.size(); i++) {

                    File file = imageFiles.get(i);

                    if (file == null)
                        continue;

                    fileNames.add(file.getAbsolutePath());
                }


                Intent intent = new Intent(getApplicationContext(), EditPhotoActivity.class);
                intent.putStringArrayListExtra(EditPhotoActivity.PHOTOS, fileNames);
                startActivityForResult(intent, 200);
                overridePendingTransition(R.anim.into_anim_slide_from_right, R.anim.into_anim_slide_to_left);


            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(CameraExplainActivity.this);
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

}
