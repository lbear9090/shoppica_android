package com.shoppica.com.facebook;

import com.facebook.AccessToken;

public interface FacebookListener {

    void onFbSignInCanceled();

    void onFbSignInFail(String errorMessage);

    void onFbSignInSuccess(AccessToken accessToken, String authToken, String userId);

    void onFBSignOut();
}
