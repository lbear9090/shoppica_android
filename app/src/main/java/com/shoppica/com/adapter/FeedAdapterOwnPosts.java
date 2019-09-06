package com.shoppica.com.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.shoppica.com.R;
import com.shoppica.com.constants.Constants;
import com.shoppica.com.objects.FeedObject;
import com.shoppica.com.utils.AndroidUtilities;
import com.shoppica.com.view.CustomRunnable;
import com.shoppica.com.view.GlideApp;
import com.haipq.android.flagkit.FlagImageView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class FeedAdapterOwnPosts extends BaseRecyclerAdapter<FeedObject> {

    public static final String TAG = FeedAdapterOwnPosts.class.getSimpleName();

    public static final int ITEM_FEED_NORMAL_TYPE_0 = 0;
    public static final int ITEM_FEED_NORMAL_TYPE_1 = 1;
    public static final int ITEM_LOADING = 2;

    private clickListener mListener;

    private ConstraintSet set = new ConstraintSet();

    private Context mContext;

    private Handler extendHandler = new Handler();
    private Handler promoteHandler = new Handler();

    public interface clickListener {

        void onItemClicked(FeedObject object);

        void onItemExtendClicked(FeedObject object);

        void onItemPromoteClicked(FeedObject object);

        void onItemStatusClicked(FeedObject object);

        void onItemMoreClicked(FeedObject object);

        void onLongItemClicked(FeedObject object);

    }

    public FeedAdapterOwnPosts(Context context) {
        super(context);
        this.mContext = context;
    }

    public FeedAdapterOwnPosts(Context context, List<FeedObject> list) {
        super(context, list);
        this.mContext = context;
    }

    public void setListener(clickListener mListener) {
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View v;

        if (viewType == ITEM_FEED_NORMAL_TYPE_0) {
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_feed_layout_normal_own_post, viewGroup, false);
            return new FeedNormalViewHolder(v);
        } else if (viewType == ITEM_FEED_NORMAL_TYPE_1) {
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_feed_layout_normal_own_post, viewGroup, false);
            return new FeedNormalViewHolder(v);
        } else {
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_feed_loading, viewGroup, false);
            return new LoadingViewHolder(v);
        }
    }


    private List<FeedObject> getTotalItemCount() {
        return getAllItems();
    }


    @Override
    public int getItemViewType(int position) {

        FeedObject object = getItem(position);

        if (object != null) {
            int type = object.getType();

            if (type == 0)
                return ITEM_FEED_NORMAL_TYPE_0;
            else
                return ITEM_FEED_NORMAL_TYPE_1;
        } else
            return ITEM_LOADING;

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder viewHolder, final int position) {

        if (getItemViewType(position) == ITEM_FEED_NORMAL_TYPE_0) {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
            layoutParams.setFullSpan(false);


            FeedNormalViewHolder holder = (FeedNormalViewHolder) viewHolder;

            FeedObject object = getItem(position);

            if (object == null)
                return;


            Log.d(TAG, "Loaded thumb == " + object.getThumbImage1());

            long postTimeExpires = Long.valueOf(object.getPostTimeExpires()) * 1000;
            long postPromotedExpires = Long.valueOf(object.getPostPromotedTime()) * 1000;

            long currentTime = AndroidUtilities.getCurrentTime();
            long extendDifference = postTimeExpires - currentTime;
            long promoteDifference = postPromotedExpires - currentTime;

            int thumbNailWidth = Integer.parseInt(object.getThumbnailWidth());
            int thumbNailHeight = Integer.parseInt(object.getThumbnailHeight());

            @SuppressLint("DefaultLocale") String posterRatio = String.format("%d:%d", thumbNailWidth, thumbNailHeight);

            set.clone(holder.mConstraintLayout);
            set.setDimensionRatio(holder.mImageView.getId(), posterRatio);
            set.applyTo(holder.mConstraintLayout);


            GlideApp.with(mContext).load(object.getThumbImage1())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(new RequestOptions()
                            .placeholder(new ColorDrawable(ContextCompat.getColor(mContext, R.color.colorBackgroundFragment))))
                    .into(holder.mImageView);


            String image1 = object.getImage1();
            String image2 = object.getImage2();
            String image3 = object.getImage3();
            String image4 = object.getImage4();
            String image5 = object.getImage5();

            boolean isMultiplePhotos = false;

            if (image1 != null && !image1.isEmpty() && !image1.equals("null"))
                isMultiplePhotos = false;

            if (image2 != null && !image2.isEmpty() && !image2.equals("null"))
                isMultiplePhotos = true;

            if (image3 != null && !image3.isEmpty() && !image3.equals("null"))
                isMultiplePhotos = true;

            if (image4 != null && !image4.isEmpty() && !image4.equals("null"))
                isMultiplePhotos = true;

            if (image5 != null && !image5.isEmpty() && !image5.equals("null"))
                isMultiplePhotos = true;

            if (isMultiplePhotos)
                holder.mMultiplePhotos.setVisibility(View.GONE);
            else
                holder.mMultiplePhotos.setVisibility(View.GONE);

            if (object.getOwnPosts() == 1)
                holder.mArticleMore.setVisibility(View.VISIBLE);
            else
                holder.mArticleMore.setVisibility(View.GONE);


            String [] items = mContext.getResources().getStringArray(R.array.status);

            holder.mStatusTitle.setText(items[Integer.valueOf(object.getPostStatus())]);

            String location = object.getLocationPlace();
            location = location.substring(0, 1).toUpperCase() + location.substring(1).toLowerCase();
            holder.mLocation.setText(location);

            holder.mTitle.setText(object.getItemTitle());

            if (!TextUtils.isEmpty(object.getPostCountry())) {
                holder.mCountryBorder.setVisibility(View.VISIBLE);
                holder.mCountry.setVisibility(View.VISIBLE);
                holder.mCountry.setCountryCode(object.getPostCountry());
            } else {
                holder.mCountryBorder.setVisibility(View.GONE);
                holder.mCountry.setVisibility(View.GONE);
            }

            String priceText = "$";
            String price = object.getItemPrice();
            price = price.replace(",", ".");

            switch (object.getPostCountry()) {
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

            holder.mPrice.setText(mContext.getString(R.string.btn_action_price, priceText + price));

            holder.mArticleMore.setOnClickListener(view -> {
                mListener.onItemMoreClicked(object);
            });

            holder.mBtnExtend.setOnClickListener(view -> {
                mListener.onItemExtendClicked(object);
            });

            holder.mBtnPromote.setOnClickListener(view -> {
                mListener.onItemPromoteClicked(object);
            });

            holder.mBtnStatus.setOnClickListener(view ->
                    mListener.onItemStatusClicked(object));

            holder.itemView.setOnClickListener(view -> {
                mListener.onItemClicked(object);
            });

            extendHandler.removeCallbacks(holder.extendRunnable);
            holder.extendRunnable.mTitle = holder.mExtendTitle;
            holder.extendRunnable.mSubTitle = holder.mExtendSubTitle;
            holder.extendRunnable.mContext = mContext;
            holder.extendRunnable.isArticle = true;
            holder.extendRunnable.millisUntilFinished = extendDifference;//Current time - received time
            extendHandler.postDelayed(holder.extendRunnable, 100);

            promoteHandler.removeCallbacks(holder.promoteRunnable);
            holder.promoteRunnable.mTitle = holder.mPromoteTitle;
            holder.promoteRunnable.mSubTitle = holder.mPromoteSubTitle;
            holder.promoteRunnable.mContext = mContext;
            holder.promoteRunnable.isArticle = false;
            holder.promoteRunnable.millisUntilFinished = promoteDifference;//Current time - received time
            promoteHandler.postDelayed(holder.promoteRunnable, 100);

        } else if (getItemViewType(position) == ITEM_FEED_NORMAL_TYPE_1) {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
            layoutParams.setFullSpan(false);

            FeedNormalViewHolder holder = (FeedNormalViewHolder) viewHolder;

            FeedObject object = getItem(position);

            if (object == null)
                return;

            long postTimeExpires = Long.valueOf(object.getPostTimeExpires()) * 1000;
            long postPromotedExpires = Long.valueOf(object.getPostPromotedTime()) * 1000;

            long currentTime = AndroidUtilities.getCurrentTime();
            long extendDifference = postTimeExpires - currentTime;
            long promoteDifference = postPromotedExpires - currentTime;

            Log.d(TAG, "extendDifference == " + extendDifference);
            Log.d(TAG, "promoteDifference == " + promoteDifference);

            int thumbNailWidth = Integer.parseInt(object.getThumbnailWidth());
            int thumbNailHeight = Integer.parseInt(object.getThumbnailHeight());

            @SuppressLint("DefaultLocale") String posterRatio = String.format("%d:%d", thumbNailWidth, thumbNailHeight);

            set.clone(holder.mConstraintLayout);
            set.setDimensionRatio(holder.mImageView.getId(), posterRatio);
            set.applyTo(holder.mConstraintLayout);


            GlideApp.with(mContext).load(object.getThumbImage1())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(new RequestOptions()
                            .placeholder(new ColorDrawable(ContextCompat.getColor(mContext, R.color.colorBackgroundFragment))))
                    .into(holder.mImageView);

            String image1 = object.getImage1();
            String image2 = object.getImage2();
            String image3 = object.getImage3();
            String image4 = object.getImage4();
            String image5 = object.getImage5();

            boolean isMultiplePhotos = false;

            if (image1 != null && !image1.isEmpty() && !image1.equals("null"))
                isMultiplePhotos = false;

            if (image2 != null && !image2.isEmpty() && !image2.equals("null"))
                isMultiplePhotos = true;

            if (image3 != null && !image3.isEmpty() && !image3.equals("null"))
                isMultiplePhotos = true;

            if (image4 != null && !image4.isEmpty() && !image4.equals("null"))
                isMultiplePhotos = true;

            if (image5 != null && !image5.isEmpty() && !image5.equals("null"))
                isMultiplePhotos = true;

            if (isMultiplePhotos)
                holder.mMultiplePhotos.setVisibility(View.VISIBLE);
            else
                holder.mMultiplePhotos.setVisibility(View.VISIBLE);

            if (object.getOwnPosts() == 1)
                holder.mArticleMore.setVisibility(View.VISIBLE);
            else
                holder.mArticleMore.setVisibility(View.GONE);

            String location = object.getLocationPlace();
            location = location.substring(0, 1).toUpperCase() + location.substring(1).toLowerCase();
            holder.mLocation.setText(location);

            holder.mTitle.setText(object.getItemTitle());

            String [] items = mContext.getResources().getStringArray(R.array.status);

            holder.mStatusTitle.setText(items[Integer.valueOf(object.getPostStatus())]);


            String priceText = "$";
            String price = object.getItemPrice();
            price = price.replace(",", ".");

            switch (object.getPostCountry()) {
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

            holder.mPrice.setText(mContext.getString(R.string.btn_action_price, priceText + price));

            if (!TextUtils.isEmpty(object.getPostCountry())) {
                holder.mCountryBorder.setVisibility(View.VISIBLE);
                holder.mCountry.setVisibility(View.VISIBLE);
                holder.mCountry.setCountryCode(object.getPostCountry());
            } else {
                holder.mCountryBorder.setVisibility(View.GONE);
                holder.mCountry.setVisibility(View.GONE);
            }


            holder.mArticleMore.setOnClickListener(view -> {
                mListener.onItemMoreClicked(object);
            });

            holder.mBtnExtend.setOnClickListener(view -> {
                mListener.onItemExtendClicked(object);
            });

            holder.mBtnPromote.setOnClickListener(view -> {
                mListener.onItemPromoteClicked(object);
            });

            holder.mBtnStatus.setOnClickListener(view ->
                    mListener.onItemStatusClicked(object));

            holder.itemView.setOnClickListener(view -> {
                mListener.onItemClicked(object);
            });

            extendHandler.removeCallbacks(holder.extendRunnable);
            holder.extendRunnable.mTitle = holder.mExtendTitle;
            holder.extendRunnable.mSubTitle = holder.mExtendSubTitle;
            holder.extendRunnable.mContext = mContext;
            holder.extendRunnable.isArticle = true;
            holder.extendRunnable.millisUntilFinished = extendDifference;//Current time - received time
            extendHandler.postDelayed(holder.extendRunnable, 100);

            promoteHandler.removeCallbacks(holder.promoteRunnable);
            holder.promoteRunnable.mTitle = holder.mPromoteTitle;
            holder.promoteRunnable.mSubTitle = holder.mPromoteSubTitle;
            holder.promoteRunnable.mContext = mContext;
            holder.promoteRunnable.isArticle = false;
            holder.promoteRunnable.millisUntilFinished = promoteDifference;//Current time - received time
            promoteHandler.postDelayed(holder.promoteRunnable, 100);
        } else if (getItemViewType(position) == ITEM_LOADING) {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
            layoutParams.setFullSpan(true);
        }

    }


    public class FeedNormalViewHolder extends BaseViewHolder {

        @BindView(R.id.parentContsraint)
        ConstraintLayout mConstraintLayout;

        @BindView(R.id.article_photo)
        ImageView mImageView;

        @BindView(R.id.article_title)
        TextView mTitle;

        @BindView(R.id.article_location)
        TextView mLocation;

        @BindView(R.id.article_price)
        TextView mPrice;

        @BindView(R.id.articleMultiplePhotos)
        ImageView mMultiplePhotos;

        @BindView(R.id.articleMore)
        ImageView mArticleMore;

        @BindView(R.id.article_extend)
        Button mBtnExtend;

        @BindView(R.id.article_promote)
        Button mBtnPromote;

        @BindView(R.id.article_status)
        Button mBtnStatus;

        @BindView(R.id.article_extend_title)
        TextView mExtendTitle;

        @BindView(R.id.article_extend_sub_title)
        TextView mExtendSubTitle;

        @BindView(R.id.article_promote_title)
        TextView mPromoteTitle;

        @BindView(R.id.article_promote_sub_title)
        TextView mPromoteSubTitle;

        @BindView(R.id.article_status_title)
        TextView mStatusTitle;

        @BindView(R.id.countryBorder)
        ImageView mCountryBorder;

        @BindView(R.id.country)
        FlagImageView mCountry;

        CustomRunnable extendRunnable;
        CustomRunnable promoteRunnable;

        FeedNormalViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            Drawable drawable = AndroidUtilities.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(48), ContextCompat.getColor(mContext,
                    R.color.transparent), ContextCompat.getColor(mContext, R.color.white_50));

            extendRunnable = new CustomRunnable(extendHandler, mExtendTitle, mExtendSubTitle);
            promoteRunnable = new CustomRunnable(promoteHandler, mPromoteTitle, mPromoteSubTitle);

            mArticleMore.setBackground(drawable);

        }

    }



    public class LoadingViewHolder extends BaseViewHolder {

        LoadingViewHolder(View itemView) {
            super(itemView);

        }

    }

    @Override
    public void onDestroy() {
        clear();
        super.onDestroy();
    }

}