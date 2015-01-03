package com.infinitedimensions.somanami.gcm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SimpleDBHandler extends SQLiteOpenHelper {


    private static final int DATABASE_VERSION = 7;
    private static final String DATABASE_NAME = "somanami.db";
    public static final String TABLE_NOTIFICATIONS= "notifications";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_BOOK = "book";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_USER = "user";
    public static final String COLUMN_TYPE = "type";

    public SimpleDBHandler(Context context, String name,
                       SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTENTS_TABLE = "CREATE TABLE " +
                TABLE_NOTIFICATIONS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + COLUMN_BOOK + " TEXT,"
                + COLUMN_MESSAGE + " TEXT,"
                + COLUMN_USER + " TEXT,"
                + COLUMN_TYPE + " TEXT,"
                + "); ";
        db.execSQL(CREATE_CONTENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        onCreate(db);
    }
    public void addNotification(NotificationGCM notification) {

        ContentValues values = new ContentValues();


        values.put(COLUMN_BOOK, notification.getBook());
        values.put(COLUMN_MESSAGE, notification.getMesage());
        values.put(COLUMN_USER, notification.getUser());
        values.put(COLUMN_TYPE , notification.getType());

        SQLiteDatabase db = this.getWritableDatabase();

        db.insert(TABLE_NOTIFICATIONS, null, values);
        db.close();
    }

    public int getTotalNotifications(){

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor mCount= db.rawQuery("select count(*) from " + TABLE_NOTIFICATIONS, null);
        mCount.moveToFirst();
        int count= mCount.getInt(0);
        mCount.close();

        return  count;
    }

    public List<NotificationGCM> getNotifications() {
        List<NotificationGCM> notificationsList = new ArrayList<NotificationGCM>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NOTIFICATIONS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                NotificationGCM content = new NotificationGCM();

                content.setBook((cursor.getString(1)));
                content.setMessage((cursor.getString(2)));
                content.setUser((cursor.getString(3)));
                content.setType((cursor.getString(4)));

                notificationsList.add(content);
        } while (cursor.moveToNext());
    }
    
    // return contact list
    return notificationsList;
    }


    public boolean deleteContent(String content_id) {

            SQLiteDatabase db = this.getWritableDatabase();

            db.delete(TABLE_NOTIFICATIONS, COLUMN_ID + "='" + content_id + "'", null);

            db.close();

        return true;
    }
}