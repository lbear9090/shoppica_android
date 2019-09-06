package com.shoppica.com.activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.shoppica.com.R;
import com.shoppica.com.utils.AndroidUtilities;
import com.shoppica.com.view.GlideApp;
import com.shoppica.com.view.HackyViewPager;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class ViewPagerActivity extends FullBaseActivity {

    public static final String TAG = ViewPagerActivity.class.getSimpleName();

    public static final String PHOTOS = "photos";

    private List<String> imageList;

    @BindView(R.id.view_pager)
    HackyViewPager mPager;

    @BindView(R.id.back)
    ImageButton mBack;

    @BindView(R.id.toolbar_name)
    TextView mToolbarName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager);

        init();

    }

    private void init() {
        Intent intent = getIntent();

        if (intent == null) {

            Toast.makeText(this, R.string.toast_error_intent_null, Toast.LENGTH_SHORT).show();
            AndroidUtilities.runOnUIThread(this::finish, 2000);

            return;
        }

        imageList = intent.getStringArrayListExtra(PHOTOS);

        if (imageList == null || imageList.size() == 0) {
            Toast.makeText(this, R.string.toast_error_intent_null, Toast.LENGTH_SHORT).show();
            AndroidUtilities.runOnUIThread(this::finish, 2000);
        }


        mPager.setAdapter(new MultiplePhotoAdapter());

        Drawable backDrawable = AndroidUtilities.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(48), ContextCompat.getColor(getApplicationContext(),
                R.color.transparent), ContextCompat.getColor(getApplicationContext(), R.color.colorFilterPressed));


        mBack.setBackground(backDrawable);
        if (imageList.size() > 1)
            mToolbarName.setText(getString(R.string.toolbar_name_view_articles, imageList.size()));
        else
            mToolbarName.setText("View Photo");


    }


    private class MultiplePhotoAdapter extends PagerAdapter {


        @Override
        public int getCount() {
            return imageList.size();
        }

        @NonNull
        @Override
        public View instantiateItem(@NonNull ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());

            setMargins(photoView, AndroidUtilities.dp(48));

            String fileName = imageList.get(position);

            GlideApp.with(getApplicationContext()).load(fileName)
                    .apply(new RequestOptions().dontAnimate().centerInside().override(800, 800)).placeholder(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.black))).into(photoView);


            // Now just add PhotoView to ViewPager and return it
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            return photoView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        private void setMargins(View view, int top) {
            if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                p.setMargins(0, top, 0, 0);
                view.requestLayout();
            }
        }

    }

    @OnClick(R.id.back)
    void onBackClicked() {
        finish();
    }


    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.into_anim_slide_from_left, R.anim.into_anim_slide_to_right);
    }

}
