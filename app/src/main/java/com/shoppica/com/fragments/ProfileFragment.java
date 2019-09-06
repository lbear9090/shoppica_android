package com.shoppica.com.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.shoppica.com.R;
import com.shoppica.com.activities.SettingsActivity;
import com.shoppica.com.constants.Constants;
import com.shoppica.com.notifications.NotificationCenter;
import com.shoppica.com.preferences.Prefs;
import com.shoppica.com.utils.AndroidUtilities;
import com.shoppica.com.view.GlideApp;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfileFragment extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    public static final String TAG = ProfileFragment.class.getSimpleName();

    @BindView(R.id.image)
    ImageView mImageView;

    @BindView(R.id.username)
    TextView mUsername;

    @BindView(R.id.settings)
    ImageButton mSettings;

    @BindView(R.id.toolbar_shadow)
    View mToolbarShadow;

    @BindView(R.id.bottomTabs)
    TabLayout mTabLayout;

    @BindView(R.id.pager)
    ViewPager mPager;


    private Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context.getApplicationContext();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        initDrawable();

        initViews();

        initTabs();

    }

    private void initDrawable() {

        Drawable settingsDrawable = AndroidUtilities.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(48), ContextCompat.getColor(mContext,
                R.color.transparent), ContextCompat.getColor(mContext, R.color.colorFilterPressed));

        mSettings.setBackground(settingsDrawable);

    }

    private void initViews() {

        String profileUrl = Prefs.getString(Constants.KEY_REGISTER_PROFILE_URL, null);

        if (!TextUtils.isEmpty(profileUrl)) {
            GlideApp.with(this).load(profileUrl).circleCrop().placeholder(R.drawable.ic_profile_placeholder).into(mImageView);
        }

        String username = Prefs.getString(Constants.KEY_REGISTER_DISPLAY_NAME, null);

        if (TextUtils.isEmpty(username))
            mUsername.setText(getString(R.string.btn_action_profile));
        else
            mUsername.setText(username);

        NotificationCenter.getInstance().addObserver(this, NotificationCenter.setOwnPostFragmentTab);

    }

    private void initTabs() {


        ViewPagerAdapter mAdapter = new ViewPagerAdapter(getChildFragmentManager());
        mPager.setOffscreenPageLimit(2);

        mPager.setAdapter(mAdapter);

        mTabLayout.setupWithViewPager(mPager);

        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            if (tab != null)
                tab.setCustomView(mAdapter.getTabView(i));
        }

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                mPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    @OnClick(R.id.settings)
    void onSettingsClicked() {


        Intent intent = new Intent(getActivity(), SettingsActivity.class);
        startActivity(intent);
        Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.into_anim_slide_from_right, R.anim.into_anim_slide_to_left);


    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.setOwnPostFragmentTab) {
            if (mPager != null)
                mPager.setCurrentItem(0);
        }
    }


    public class ViewPagerAdapter extends FragmentStatePagerAdapter {

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }


        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new OwnPostFragment();
                default:
                    return new FavoriteFragment();
            }
        }

        @SuppressLint("InflateParams")
        View getTabView(int position) {
            View v;

            switch (position) {
                case 0:
                    v = LayoutInflater.from(getActivity()).inflate(R.layout.tab_item_profile_1, null);
                    break;
                default:
                    v = LayoutInflater.from(getActivity()).inflate(R.layout.tab_item_profile_2, null);
                    break;

            }

            return v;
        }

    }

    @Override
    public void onDestroy() {
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.setOwnPostFragmentTab);
        super.onDestroy();
    }
}
