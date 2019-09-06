package com.shoppica.com.objects;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.shoppica.com.db.DBContracts;

public class FavoriteObject implements Parcelable {

    private static final String TAG = FavoriteObject.class.getSimpleName();

    private int mPostId;


    public FavoriteObject(Cursor cursor) {
        mPostId = cursor.getInt(DBContracts.FavoriteContract.COLUMN_INDEX_POST_ID);
    }

    private FavoriteObject(Parcel in) {
        mPostId = in.readInt();
    }

    public int getPostId() {
        return mPostId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeInt(mPostId);
    }

    public static final Creator<FavoriteObject> CREATOR = new Creator<FavoriteObject>() {
        public FavoriteObject createFromParcel(Parcel in) {
            return new FavoriteObject(in);
        }

        public FavoriteObject[] newArray(int size) {
            return new FavoriteObject[size];
        }
    };

}
