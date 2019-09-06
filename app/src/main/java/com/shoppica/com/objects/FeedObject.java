package com.shoppica.com.objects;

import android.os.Parcel;
import android.os.Parcelable;

public class FeedObject implements Parcelable {

    private static final String TAG = FeedObject.class.getSimpleName();

    private String mPostId;
    private String mPostUrl;
    private String mPostCountry;
    private String mPostPromoted;
    private String mPostPromotedTime;
    private String mPostTime;
    private String mPostTimeExpires;
    private String mPostViews;
    private String mPostViewsToday;
    private String mPostViewsYesterday;
    private String mPostDeleted;
    private String mFbAcctId;
    private String mFbName;
    private String mItemPrice;
    private String mItemGender;
    private String mItemCategory;
    private String mItemDelivery;
    private String mItemTitle;
    private String mItemCondition;
    private String mItemBrand;
    private String mItemSize;
    private String mItemColor;
    private String mContactWhatsApp;
    private String mContactInstagram;
    private String mContactEmail;
    private String mLocationLat;
    private String mLocationLong;
    private String mLocationPlace;
    private String mImage1;
    private String mImage1Width;
    private String mImage1Height;
    private String mImage2;
    private String mImage3;
    private String mImage4;
    private String mImage5;
    private String mThumbImage1;
    private String mThumbnailWidth;
    private String mThumbnailHeight;
    private String mPostStatus;
    private String mTimeActive;
    private int mType;
    private int mOwnPosts;
    private int mReportedUser;


    public FeedObject(String mPostId, String mPostUrl,String mPostCountry, String mPostPromoted, String mPostPromotedTime, String mPostTime, String mPostTimeExpires, String mPostViews, String mPostViewsToday, String mPostViewsYesterday, String mPostDeleted, String mFbAcctId, String mFbName, String mItemPrice, String mItemGender, String mItemCategory, String mItemDelivery, String mItemTitle, String mItemCondition, String mItemBrand, String mItemSize, String mItemColor, String mContactWhatsApp, String mContactMessenger, String mContactEmail, String mLocationLat, String mLocationLong, String mLocationPlace, String mImage1, String mImage1Width, String mImage1Height, String mImage2, String mImage3, String mImage4, String mImage5, String mThumbImage1, String mThumbnailWidth, String mThumbnailHeight, String mPostStatus, String mTimeActive, int mType, int mOwnPosts) {

        this.mPostId = mPostId;
        this.mPostUrl = mPostUrl;
        this.mPostCountry = mPostCountry;
        this.mPostPromoted = mPostPromoted;
        this.mPostPromotedTime = mPostPromotedTime;
        this.mPostTime = mPostTime;
        this.mPostTimeExpires = mPostTimeExpires;
        this.mPostViews = mPostViews;
        this.mPostViewsToday = mPostViewsToday;
        this.mPostViewsYesterday = mPostViewsYesterday;
        this.mPostDeleted = mPostDeleted;
        this.mFbAcctId = mFbAcctId;
        this.mFbName = mFbName;
        this.mItemPrice = mItemPrice;
        this.mItemGender = mItemGender;
        this.mItemCategory = mItemCategory;
        this.mItemDelivery = mItemDelivery;
        this.mItemTitle = mItemTitle;
        this.mItemCondition = mItemCondition;
        this.mItemBrand = mItemBrand;
        this.mItemSize = mItemSize;
        this.mItemColor = mItemColor;
        this.mContactWhatsApp = mContactWhatsApp;
        this.mContactInstagram = mContactMessenger;
        this.mContactEmail = mContactEmail;
        this.mLocationLat = mLocationLat;
        this.mLocationLong = mLocationLong;
        this.mLocationPlace = mLocationPlace;
        this.mImage1 = mImage1;
        this.mImage1Width = mImage1Width;
        this.mImage1Height = mImage1Height;
        this.mImage2 = mImage2;
        this.mImage3 = mImage3;
        this.mImage4 = mImage4;
        this.mImage5 = mImage5;
        this.mThumbImage1 = mThumbImage1;
        this.mThumbnailWidth = mThumbnailWidth;
        this.mThumbnailHeight = mThumbnailHeight;
        this.mPostStatus = mPostStatus;
        this.mTimeActive = mTimeActive;
        this.mType = mType;
        this.mOwnPosts = mOwnPosts;
    }

    private FeedObject(Parcel in) {
        mPostId = in.readString();
        mPostUrl = in.readString();
        mPostCountry = in.readString();
        mPostPromoted = in.readString();
        mPostPromotedTime = in.readString();
        mPostTime = in.readString();
        mPostTimeExpires = in.readString();
        mPostViews = in.readString();
        mPostViewsToday = in.readString();
        mPostViewsYesterday = in.readString();
        mPostDeleted = in.readString();
        mFbAcctId = in.readString();
        mFbName = in.readString();
        mItemPrice = in.readString();
        mItemGender = in.readString();
        mItemCategory = in.readString();
        mItemDelivery = in.readString();
        mItemTitle = in.readString();
        mItemCondition = in.readString();
        mItemBrand = in.readString();
        mItemSize = in.readString();
        mItemColor = in.readString();
        mContactWhatsApp = in.readString();
        mContactInstagram = in.readString();
        mContactEmail = in.readString();
        mLocationLat = in.readString();
        mLocationLong = in.readString();
        mLocationPlace = in.readString();
        mImage1 = in.readString();
        mImage1Width = in.readString();
        mImage1Height = in.readString();
        mImage2 = in.readString();
        mImage3 = in.readString();
        mImage4 = in.readString();
        mImage5 = in.readString();
        mThumbImage1 = in.readString();
        mThumbnailWidth = in.readString();
        mThumbnailHeight = in.readString();
        mPostStatus = in.readString();
        mTimeActive = in.readString();
        mType = in.readInt();
        mOwnPosts = in.readInt();
        mReportedUser = in.readInt();


    }

    public String getPostPromotedTime() {
        return mPostPromotedTime;
    }

    public String getPostPromoted() {
        return mPostPromoted;
    }

    public String getPostTime() {
        return mPostTime;
    }

    public String getPostTimeExpires() {
        return mPostTimeExpires;
    }

    public String getPostViews() {
        return mPostViews;
    }


    public String getPostViewsToday() {
        return mPostViewsToday;
    }

    public String getPostViewsYesterday() {
        return mPostViewsYesterday;
    }

    public String getPostDeleted() {
        return mPostDeleted;
    }

    public String getPostId() {
        return mPostId;
    }

    public String getPostUrl() {
        return mPostUrl;
    }

    public String getFbUserId() {
        return mFbAcctId;
    }

    public String getItemPrice() {
        return mItemPrice;
    }

    public String getItemGender() {
        return mItemGender;
    }

    public String getItemCategory() {
        return mItemCategory;
    }

    public String getItemDelivery() {
        return mItemDelivery;
    }

    public String getItemTitle() {
        return mItemTitle;
    }

    public String getFbName() {
        return mFbName;
    }

    public String getItemBrand() {
        return mItemBrand;
    }

    public String getItemCondition() {
        return mItemCondition;
    }

    public String getItemSize() {
        return mItemSize;
    }

    public String getItemColor() {
        return mItemColor;
    }

    public String getContactWhatsApp() {
        return mContactWhatsApp;
    }

    public String getContactInstagram() {
        return mContactInstagram;
    }

    public String getContactEmail() {
        return mContactEmail;
    }

    public String getLocationLat() {
        return mLocationLat;
    }

    public String getLocationLong() {
        return mLocationLong;
    }

    public String getLocationPlace() {
        return mLocationPlace;
    }

    public String getImage1() {
        return mImage1;
    }

    public String getImage1Width() {
        return mImage1Width;
    }

    public String getImage1Height() {
        return mImage1Height;
    }

    public String getImage2() {
        return mImage2;
    }

    public String getImage3() {
        return mImage3;
    }

    public String getImage4() {
        return mImage4;
    }

    public String getImage5() {
        return mImage5;
    }

    public String getThumbImage1() {
        return mThumbImage1;
    }

    public String getThumbnailWidth() {
        return mThumbnailWidth;
    }

    public String getThumbnailHeight() {
        return mThumbnailHeight;
    }

    public void setPostStatus(String mPostStatus) {
        this.mPostStatus = mPostStatus;
    }

    public String getPostStatus() {
        return mPostStatus;
    }

    public boolean isSponsored() {
        return false;
    }

    public int getType() {
        return mType;
    }

    public int getOwnPosts() {
        return mOwnPosts;
    }

    public String getTimeActive() {
        return mTimeActive;
    }

    public String getPostCountry() {
        return mPostCountry;
    }

    public void setReportedUser(boolean mReportedUser) {
        this.mReportedUser = mReportedUser ? 1 : 0;
    }
    public void setPostViews(String mPostViews) {
        this.mPostViews = mPostViews;
    }

    public boolean getReportedUser() {
        return mReportedUser == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeString(mPostId);
        dest.writeString(mPostUrl);
        dest.writeString(mPostCountry);
        dest.writeString(mPostPromoted);
        dest.writeString(mPostPromotedTime);
        dest.writeString(mPostTime);
        dest.writeString(mPostTimeExpires);
        dest.writeString(mPostViews);
        dest.writeString(mPostViewsToday);
        dest.writeString(mPostViewsYesterday);
        dest.writeString(mPostDeleted);
        dest.writeString(mFbAcctId);
        dest.writeString(mFbName);
        dest.writeString(mItemPrice);
        dest.writeString(mItemGender);
        dest.writeString(mItemCategory);
        dest.writeString(mItemDelivery);
        dest.writeString(mItemTitle);
        dest.writeString(mItemCondition);
        dest.writeString(mItemBrand);
        dest.writeString(mItemSize);
        dest.writeString(mItemColor);
        dest.writeString(mContactWhatsApp);
        dest.writeString(mContactInstagram);
        dest.writeString(mContactEmail);
        dest.writeString(mLocationLat);
        dest.writeString(mLocationLong);
        dest.writeString(mLocationPlace);
        dest.writeString(mImage1);
        dest.writeString(mImage1Width);
        dest.writeString(mImage1Height);
        dest.writeString(mImage2);
        dest.writeString(mImage3);
        dest.writeString(mImage4);
        dest.writeString(mImage5);
        dest.writeString(mThumbImage1);
        dest.writeString(mThumbnailWidth);
        dest.writeString(mThumbnailHeight);
        dest.writeString(mPostStatus);
        dest.writeString(mTimeActive);
        dest.writeInt(mType);
        dest.writeInt(mOwnPosts);
        dest.writeInt(mReportedUser);
    }

    public static final Creator<FeedObject> CREATOR = new Creator<FeedObject>() {
        public FeedObject createFromParcel(Parcel in) {
            return new FeedObject(in);
        }

        public FeedObject[] newArray(int size) {
            return new FeedObject[size];
        }
    };


}
