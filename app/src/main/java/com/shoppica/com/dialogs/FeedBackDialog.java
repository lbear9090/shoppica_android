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
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.shoppica.com.R;
import com.shoppica.com.view.EditTextTint;
import com.crashlytics.android.Crashlytics;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Carl on 3-11-2016.
 */

public class FeedBackDialog extends DialogFragment {

    public static final String TAG = FeedBackDialog.class.getSimpleName();

    private OnCompleteListener mListener;

    @BindView(R.id.into_switcher)
    ViewSwitcher mSwitcher;

    @BindView(R.id.into_report_message)
    EditText mFeedBackMsg;

    private Context mContext;

    public interface OnCompleteListener {
        void onFeedBackSend(String message);
    }

    @Override
    public void onAttach(Activity activity) {
        mListener = (OnCompleteListener) activity;
        super.onAttach(activity);

        mContext = activity.getApplicationContext();
    }


    public static FeedBackDialog newInstance() {
        FeedBackDialog frag = new FeedBackDialog();
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
            EditTextTint.applyColor(mFeedBackMsg, ContextCompat.getColor(Objects.requireNonNull(mContext), R.color.colorAccent), ContextCompat.getColor(mContext, R.color.colorAccent));
        } catch (EditTextTint.EditTextTintError e) {
            Crashlytics.logException(e);
        }
    }

    public FeedBackDialog() {
        // Empty constructor required for DialogFragment
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.into_view_feed_back, container);
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

    public static void hideKeyboard(View view) {

        // hide keyboard
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @OnClick(R.id.into_report_user)
    void onUserReportClicked() {

        String message = mFeedBackMsg.getText().toString();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(mContext, R.string.toast_error_report_empty, Toast.LENGTH_SHORT).show();
            return;
        }


        if (message.length() < 10) {
            Toast.makeText(mContext, R.string.toast_error_report_length, Toast.LENGTH_SHORT).show();
            return;
        }

        hideKeyboard(mFeedBackMsg);

        if (mListener != null)
            mListener.onFeedBackSend(message);

    }


    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();

    }

}