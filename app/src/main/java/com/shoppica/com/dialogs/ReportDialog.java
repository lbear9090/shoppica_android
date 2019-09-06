package com.shoppica.com.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.shoppica.com.R;
import com.shoppica.com.utils.AndroidUtilities;
import com.shoppica.com.view.EditTextTint;
import com.crashlytics.android.Crashlytics;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

;

/**
 * Created by Carl on 3-11-2016.
 */

public class ReportDialog extends DialogFragment {

    public static final String TAG = ReportDialog.class.getSimpleName();
    private static int CLICK_ACTION_THRESHOLD = 200;

    private OnCompleteListener mListener;

    @BindView(R.id.into_switcher)
    ViewSwitcher mSwitcher;

    @BindView(R.id.into_report_1)
    Button mReport1;

    @BindView(R.id.into_report_2)
    Button mReport2;

    @BindView(R.id.into_report_3)
    Button mReport3;

    @BindView(R.id.into_report_message)
    EditText mReportMessage;

    private Context mContext;

    private long lastTouchDownReport1, lastTouchDownReport2;


    public interface OnCompleteListener {
        void onComplete(int type, String message);
    }

    @Override
    public void onAttach(Activity activity) {
        mListener = (OnCompleteListener) activity;
        super.onAttach(activity);

        mContext = activity.getApplicationContext();
    }


    public static ReportDialog newInstance() {
        ReportDialog frag = new ReportDialog();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }



    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Dialog(Objects.requireNonNull(getActivity()), R.style.CustomDialog) {
            @Override
            public void onBackPressed() {

                if (mSwitcher.getDisplayedChild() == 1)
                    mSwitcher.setDisplayedChild(0);
                else
                    dismiss();
            }
        };
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            EditTextTint.applyColor(mReportMessage, ContextCompat.getColor(Objects.requireNonNull(mContext), R.color.into_dialog_report_btn_color), ContextCompat.getColor(mContext, R.color.into_dialog_report_btn_color));
        } catch (EditTextTint.EditTextTintError e) {
            Crashlytics.logException(e);
        }
    }

    public ReportDialog() {
        // Empty constructor required for DialogFragment
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.into_view_report, container);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            Objects.requireNonNull(d.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }


    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        Objects.requireNonNull(getDialog().getWindow()).getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);


    }

    public static  void hideKeyboard(View view) {

        // hide keyboard
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    @OnTouch(R.id.into_report_1)
    boolean onReport1(View v, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.setAlpha(0.35f);
                lastTouchDownReport1 = AndroidUtilities.getCurrentTime();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                v.setAlpha(1f);
                if (System.currentTimeMillis() - lastTouchDownReport1 < CLICK_ACTION_THRESHOLD)
                    mListener.onComplete(0, null);

                break;
            default:
                break;
        }
        return true;

    }

    @OnTouch(R.id.into_report_2)
    boolean onReport2(View v, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.setAlpha(0.35f);
                lastTouchDownReport2 = AndroidUtilities.getCurrentTime();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                v.setAlpha(1f);
                if (AndroidUtilities.getCurrentTime() - lastTouchDownReport2 < CLICK_ACTION_THRESHOLD)
                    mListener.onComplete(1, null);

                break;
            default:
                break;
        }
        return true;

    }

    @OnTouch(R.id.into_report_3)
    boolean onReport3(View v, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.setAlpha(0.35f);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mSwitcher.setDisplayedChild(1);
                v.setAlpha(1f);
                break;
            default:
                break;
        }
        return true;

    }

    @OnClick(R.id.into_report_user)
    void onUserReportClicked() {

        String message = mReportMessage.getText().toString();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(mContext, R.string.toast_error_report_empty, Toast.LENGTH_SHORT).show();
            return;
        }


        if (message.length() < 10) {
            Toast.makeText(mContext, R.string.toast_error_report_length, Toast.LENGTH_SHORT).show();
            return;
        }

        hideKeyboard(mReportMessage);

        if (mListener != null)
            mListener.onComplete(2, message);

    }


    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();

    }

}