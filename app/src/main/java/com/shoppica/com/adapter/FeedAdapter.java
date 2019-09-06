package com.shoppica.com.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.shoppica.com.R;
import com.shoppica.com.constants.Constants;
import com.shoppica.com.objects.FeedObject;
import com.shoppica.com.preferences.Prefs;
import com.shoppica.com.utils.AndroidUtilities;
import com.shoppica.com.view.GlideApp;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class FeedAdapter extends BaseRecyclerAdapter<FeedObject> {

    public static final String TAG = FeedAdapter.class.getSimpleName();

    public static final int ITEM_FEED_NORMAL_TYPE_0 = 0;
    public static final int ITEM_FEED_NORMAL_TYPE_1 = 1;
    public static final int ITEM_LOADING = 2;

    private clickListener mListener;

    private ConstraintSet set = new ConstraintSet();

    private Context mContext;

    public interface clickListener {

        void onItemClicked(FeedObject object);

        void onItemMoreClicked(FeedObject object);

        void onLongItemClicked(FeedObject object);

    }

    public FeedAdapter(Context context) {
        super(context);
        this.mContext = context;
    }

    public FeedAdapter(Context context, List<FeedObject> list) {
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
                    .inflate(R.layout.item_feed_layout_normal, viewGroup, false);
            return new FeedNormalViewHolder(v);
        } else if (viewType == ITEM_FEED_NORMAL_TYPE_1) {
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_feed_layout_normal, viewGroup, false);
            return new FeedNormalViewHolder(v);
        }else {
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

            int thumbNailWidth = Integer.parseInt(object.getThumbnailWidth());
            int thumbNailHeight = Integer.parseInt(object.getThumbnailHeight());

            @SuppressLint("DefaultLocale") String posterRatio = String.format("%d:%d", thumbNailWidth, thumbNailHeight);

            set.clone(holder.mConstraintLayout);
            set.setDimensionRatio(holder.mImageView.getId(), posterRatio);
            set.applyTo(holder.mConstraintLayout);


            GlideApp.with(mContext).load(object.getThumbImage1())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(new RequestOptions().placeholder(new ColorDrawable(ContextCompat.getColor(mContext, R.color.colorBackgroundFragment))))
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

            int status = Integer.valueOf(object.getPostStatus());

            if(status == 2){
                holder.mStatus.setVisibility(View.VISIBLE);
                holder.mStatus.setText(R.string.view_article_sold);
                holder.mStatus.setBackgroundColor(mContext.getResources().getColor(R.color.colorAccent));
            }else if(status == 1){
                holder.mStatus.setVisibility(View.VISIBLE);
                holder.mStatus.setText(R.string.adapter_reserved);
                holder.mStatus.setBackgroundColor(mContext.getResources().getColor(R.color.colorReserved));
            }else{
                holder.mStatus.setVisibility(View.GONE);
            }

            String country = Prefs.getString(Constants.KEY_REGISTER_COUNTRY_CODE,null);

            if(!TextUtils.isEmpty(country)){
                if(country.toLowerCase().equals(Constants.NL) || country.toLowerCase().equals(Constants.BE))
                    holder.mArticleSponsored.setImageResource(R.drawable.ic_sponsored);
                else
                    holder.mArticleSponsored.setImageResource(R.drawable.ic_sponsored_en);
            }

            if (Integer.valueOf(object.getPostPromoted()) == 1)
                holder.mArticleSponsored.setVisibility(View.VISIBLE);
            else
                holder.mArticleSponsored.setVisibility(View.GONE);

            if (Integer.valueOf(object.getPostStatus()) == 1) {
                holder.mArticleReserved.setVisibility(View.VISIBLE);
                holder.mArticleSponsored.setVisibility(View.GONE);
            } else {
                holder.mArticleReserved.setVisibility(View.GONE);
            }
            String location = object.getLocationPlace();
            location = location.substring(0, 1).toUpperCase() + location.substring(1).toLowerCase();
            holder.mLocation.setText(location);

            holder.mTitle.setText(object.getItemTitle());

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

            holder.itemView.setOnClickListener(view -> {

                mListener.onItemClicked(object);

            });

            boolean isFilterActive = Prefs.getBoolean(Constants.KEY_FILTER_ACTIVE, false);
            int isDistanceFiltered = Prefs.getInt(Constants.KEY_FILTER_DISTANCE_BAR, 0);

            if (isFilterActive && isDistanceFiltered > 0) {
                float ownLocationLatitude = Prefs.getFloat(Constants.KEY_FILTER_LOCATION_LATITUDE, 0);
                float ownLocationLongitude = Prefs.getFloat(Constants.KEY_FILTER_LOCATION_LONGITUDE, 0);

                float articleLocationLatitude = Float.parseFloat(object.getLocationLat());
                float articleLocationLongitude = Float.parseFloat(object.getLocationLong());

                if (ownLocationLatitude != 0 && ownLocationLongitude != 0) {
                    int distance = AndroidUtilities.calculateDistanceInKilometer(
                            (double) articleLocationLatitude,
                            (double) articleLocationLongitude,
                            (double) ownLocationLatitude,
                            (double) ownLocationLongitude);

                    String distanceText;


                    if (distance < 1)
                        distanceText = mContext.getString(R.string.adapter_location_article_away_1km);
                    else
                        distanceText = mContext.getString(R.string.adapter_location_article_away, distance);

                    holder.mLocation.setText(location + " " + distanceText);
                }

            }


        } else if (getItemViewType(position) == ITEM_FEED_NORMAL_TYPE_1) {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
            layoutParams.setFullSpan(false);

            FeedNormalViewHolder holder = (FeedNormalViewHolder) viewHolder;

            FeedObject object = getItem(position);

            if (object == null)
                return;


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


            String country = Prefs.getString(Constants.KEY_REGISTER_COUNTRY_CODE,null);

            if(!TextUtils.isEmpty(country)){
                if(country.toLowerCase().equals(Constants.NL) || country.toLowerCase().equals(Constants.BE))
                    holder.mArticleSponsored.setImageResource(R.drawable.ic_sponsored);
                else
                    holder.mArticleSponsored.setImageResource(R.drawable.ic_sponsored_en);
            }

            if (Integer.valueOf(object.getPostPromoted()) == 1)
                holder.mArticleSponsored.setVisibility(View.VISIBLE);
             else
                holder.mArticleSponsored.setVisibility(View.GONE);


            if (Integer.valueOf(object.getPostStatus()) == 1) {
                holder.mArticleReserved.setVisibility(View.VISIBLE);
                holder.mArticleSponsored.setVisibility(View.GONE);
            } else {
                holder.mArticleReserved.setVisibility(View.GONE);
            }

            String location = object.getLocationPlace();
            location = location.substring(0, 1).toUpperCase() + location.substring(1).toLowerCase();
            holder.mLocation.setText(location);

            holder.mTitle.setText(object.getItemTitle());

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

            holder.itemView.setOnClickListener(view -> {

                mListener.onItemClicked(object);

            });
            boolean isFilterActive = Prefs.getBoolean(Constants.KEY_FILTER_ACTIVE, false);
            int isDistanceFiltered = Prefs.getInt(Constants.KEY_FILTER_DISTANCE_BAR, 0);

            if (isFilterActive && isDistanceFiltered > 0) {
                float ownLocationLatitude = Prefs.getFloat(Constants.KEY_FILTER_LOCATION_LATITUDE, 0);
                float ownLocationLongitude = Prefs.getFloat(Constants.KEY_FILTER_LOCATION_LONGITUDE, 0);

                float articleLocationLatitude = Float.parseFloat(object.getLocationLat());
                float articleLocationLongitude = Float.parseFloat(object.getLocationLong());

                if (ownLocationLatitude != 0 && ownLocationLongitude != 0) {
                    int distance = AndroidUtilities.calculateDistanceInKilometer(
                            (double) articleLocationLatitude,
                            (double) articleLocationLongitude,
                            (double) ownLocationLatitude,
                            (double) ownLocationLongitude);

                    String distanceText;

                    if (distance < 1)
                        distanceText = mContext.getString(R.string.adapter_location_article_away_1km);
                    else
                        distanceText = mContext.getString(R.string.adapter_location_article_away, distance);

                    holder.mLocation.setText(location + " " + distanceText);
                }

            }

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

        @BindView(R.id.txt_article_status)
        TextView mStatus;

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

        @BindView(R.id.article_reserved)
        TextView mArticleReserved;

        @BindView(R.id.article_sponsored)
        ImageView mArticleSponsored;

        FeedNormalViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            Drawable drawable = AndroidUtilities.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(48), ContextCompat.getColor(mContext,
                    R.color.transparent), ContextCompat.getColor(mContext, R.color.white_50));


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