package com.shoppica.com.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.shoppica.com.objects.FavoriteObject;
import com.shoppica.com.objects.PostObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by kcampagna on 7/25/14.
 */
public class SqlHelper extends SQLiteOpenHelper {
    private static final String TAG = "SqlHelper";

    private static final int DB_VERSION = 4;

    public static final String DB_NAME = "com.kledingkoopjes.net";

    private static SQLiteDatabase mReadableDatabase;

    private static SQLiteDatabase mWritableDatabase;

    private static SqlHelper sInstance;

    public static SqlHelper getInstance(@NonNull Context context) {
        if (sInstance == null) {
            sInstance = new SqlHelper(context.getApplicationContext());
        }

        return sInstance;
    }

    private SqlHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(TAG, "oncreate called db");
        sqLiteDatabase.execSQL(DBContracts.FavoriteContract.CREATE_TABLE_SQL);
        sqLiteDatabase.execSQL(DBContracts.PostArticleContract.CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {

        onCreate(db);
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        if (mReadableDatabase == null || !mReadableDatabase.isOpen()) {
            mReadableDatabase = super.getReadableDatabase();
        }

        return mReadableDatabase;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        if (mWritableDatabase == null || !mWritableDatabase.isOpen()) {
            mWritableDatabase = super.getWritableDatabase();
        }

        return mWritableDatabase;
    }
    // PHOTO CONTRACT

    public void insertFavoriteObject(int postId) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBContracts.FavoriteContract.COLUMN_POST_ID, postId);
        db.replace(DBContracts.FavoriteContract.TABLE_NAME, null, values);
    }

    public boolean isExisting(int postId) {

        boolean exist = false;
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(String.format(DBContracts.FavoriteContract.GET_OBJECT, postId), null);

        if (cursor != null && cursor.moveToNext()) {
            exist = true;
            cursor.close();
        }
        return exist;
    }


    @NonNull
    public List<FavoriteObject> getFavoriteObjects() {
        List<FavoriteObject> photos = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(DBContracts.FavoriteContract.GET_ALL_FAVORITES, null);

        if (cursor == null)
            return photos;

        while (cursor.moveToNext()) {
            photos.add(new FavoriteObject(cursor));
        }


        cursor.close();
        return photos;
    }


    @SuppressLint("DefaultLocale")
    public void deleteObject(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(String.format(DBContracts.FavoriteContract.DELETE, id));
    }




    public void insertPostObject(int type, int postId, String title, long date) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBContracts.PostArticleContract.COLUMN_TYPE, type);
        values.put(DBContracts.PostArticleContract.COLUMN_POST_ID, postId);
        values.put(DBContracts.PostArticleContract.COLUMN_TITLE, title);
        values.put(DBContracts.PostArticleContract.COLUMN_DATE, date);
        db.replace(DBContracts.PostArticleContract.TABLE_NAME, null, values);
    }

    public void updatePostObject(int postId, long date) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBContracts.PostArticleContract.COLUMN_DATE, date);
        db.update(DBContracts.PostArticleContract.TABLE_NAME, values, DBContracts.PostArticleContract.COLUMN_POST_ID + "= ?", new String[]{String.valueOf(postId)});
    }


    @NonNull
    public List<PostObject> getPostObjects() {
        List<PostObject> photos = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(DBContracts.PostArticleContract.GET_ALL_POST_ARTICLES, null);

        if (cursor == null)
            return photos;

        while (cursor.moveToNext()) {
            photos.add(new PostObject(cursor));
        }


        cursor.close();
        return photos;
    }


    @SuppressLint("DefaultLocale")
    public void deletePostObject(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(String.format(DBContracts.PostArticleContract.DELETE, id));
    }


    @Override
    public synchronized void close() {
        if (mReadableDatabase != null) {
            mReadableDatabase.close();
            mReadableDatabase = null;
        }

        if (mWritableDatabase != null) {
            mWritableDatabase.close();
            mWritableDatabase = null;
        }

        super.close();
    }
}
