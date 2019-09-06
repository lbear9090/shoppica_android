package com.shoppica.com.facebook;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;

public class FacebookHelper {
    private FacebookListener mListener;
    private CallbackManager mCallBackManager;

    public FacebookHelper(@NonNull FacebookListener facebookListener) {
        mListener = facebookListener;
        mCallBackManager = CallbackManager.Factory.create();
        FacebookCallback<LoginResult> mCallBack = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mListener.onFbSignInSuccess(loginResult.getAccessToken(), loginResult.getAccessToken().getToken(),
                        loginResult.getAccessToken().getUserId());
            }

            @Override
            public void onCancel() {
                mListener.onFbSignInCanceled();

            }

            @Override
            public void onError(FacebookException e) {
                mListener.onFbSignInFail(e.getMessage());
            }
        };
        LoginManager.getInstance().registerCallback(mCallBackManager, mCallBack);
    }

    @NonNull
    @CheckResult
    public CallbackManager getCallbackManager() {
        return mCallBackManager;
    }

    public void performSignIn(Activity activity) {
        LoginManager.getInstance()
                .logInWithReadPermissions(activity,
                        Arrays.asList("public_profile", /*"user_birthday",*/ "email"));
    }

    public void performSignIn(Fragment fragment) {
        LoginManager.getInstance()
                .logInWithReadPermissions(fragment,
                        Arrays.asList("public_profile", /*"user_birthday",*/ "email"));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallBackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void performSignOut() {
        LoginManager.getInstance().logOut();
        mListener.onFBSignOut();
    }


}
