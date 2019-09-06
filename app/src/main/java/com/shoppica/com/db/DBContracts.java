package com.shoppica.com.db;

import android.provider.BaseColumns;

/**
 * Created by kcampagna on 7/25/14.
 */
public class DBContracts {

    public static class FavoriteContract implements BaseColumns {
        static final String TABLE_NAME = "favoriteContract";

        static final String COLUMN_POST_ID = "postId";

        public static final int COLUMN_INDEX_POST_ID = 0;

        public static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                " (" + COLUMN_POST_ID + " INTEGER PRIMARY KEY)";

        public static final String DELETE = " DELETE FROM " + TABLE_NAME +
                " WHERE " + COLUMN_POST_ID + "=%d";

        public static final String GET_OBJECT = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + COLUMN_POST_ID + "=%d";

        public static final String GET_ALL_FAVORITES = "SELECT * FROM " + TABLE_NAME;

    }


    public static class PostArticleContract implements BaseColumns {
        static final String TABLE_NAME = "postArticleContract";

        static final String COLUMN_POST_ID = "postId";
        static final String COLUMN_TYPE = "type";
        static final String COLUMN_TITLE = "title";
        static final String COLUMN_DATE = "date";

        public static final int COLUMN_INDEX_POST_ID = 0;
        public static final int COLUMN_INDEX_TYPE= 1;
        public static final int COLUMN_INDEX_TITLE = 2;
        public static final int COLUMN_INDEX_DATE = 3;

        public static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                " (" + COLUMN_POST_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_TYPE + " INTEGER, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_DATE + " INTEGER)";

        public static final String DELETE = " DELETE FROM " + TABLE_NAME +
                " WHERE " + COLUMN_POST_ID + "=%d";

        public static final String GET_POST_OBJECT = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + COLUMN_POST_ID + "=%d";

        public static final String GET_ALL_POST_ARTICLES = "SELECT * FROM " + TABLE_NAME;

    }


}
