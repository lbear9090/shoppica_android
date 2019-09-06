package com.shoppica.com.objects;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.shoppica.com.db.DBContracts;

public class PostObject implements Parcelable {

    private static final String TAG = PostObject.class.getSimpleName();

    private int mPostId;
    private int mType;
    private String mTitle;
    private long mDate;


    public PostObject(Cursor cursor) {
        mPostId = cursor.getInt(DBContracts.PostArticleContract.COLUMN_INDEX_POST_ID);
        mType = cursor.getInt(DBContracts.PostArticleContract.COLUMN_INDEX_TYPE);
        mTitle = cursor.getString(DBContracts.PostArticleContract.COLUMN_INDEX_TITLE);
        mDate = cursor.getLong(DBContracts.PostArticleContract.COLUMN_INDEX_DATE);
    }

    private PostObject(Parcel in) {
        mPostId = in.readInt();
        mType = in.readInt();
        mTitle = in.readString();
        mDate = in.readLong();
    }

    public int getPostId() {
        return mPostId;
    }

    public long getDate() {
        return mDate;
    }

    public int getType() {
        return mType;
    }

    public String getTitle() {
        return mTitle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeInt(mPostId);
        dest.writeInt(mType);
        dest.writeString(mTitle);
        dest.writeLong(mDate);
    }

    public static final Creator<PostObject> CREATOR = new Creator<PostObject>() {
        public PostObject createFromParcel(Parcel in) {
            return new PostObject(in);
        }

        public PostObject[] newArray(int size) {
            return new PostObject[size];
        }
    };

}
