package com.shoppica.com.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.shoppica.com.app.KledingApp;
import com.shoppica.com.db.SqlHelper;

import java.util.Objects;

/**
 * Created by f22labs on 07/03/17.
 */

public class BaseFragment extends Fragment {

    public static final String TAG = BaseFragment.class.getSimpleName();

    public KledingApp app;
    public SqlHelper sqlHelper;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = KledingApp.getInstance(Objects.requireNonNull(getActivity()));
        sqlHelper = SqlHelper.getInstance(getActivity());
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    public void showDialogFragmentSupport(android.support.v4.app.DialogFragment fragment, String title) {
        try {
            Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().add(fragment, title).commit();
        } catch (IllegalStateException ignored) {

        }
    }

    /**
     * Dismisses the dialog fragment with the given title
     *
     * @param title The title of the Dialog Fragment
     */

    public void dismissDialogFragmentSupport(String title) {
        android.support.v4.app.Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag(title);

        if (fragment != null && fragment instanceof android.support.v4.app.DialogFragment) {
            ((android.support.v4.app.DialogFragment) fragment).dismissAllowingStateLoss();
        }
    }


}
