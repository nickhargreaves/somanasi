package com.infinitedimensions.somanami.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.infinitedimensions.somanami.models.Book;
import com.infinitedimensions.somanami.models.Message;
import com.infinitedimensions.somanami.models.NotificationGCM;
import com.infinitedimensions.somanami.models.TrayItem;

import java.util.ArrayList;
import java.util.List;

public class SimpleDBHandler extends SQLiteOpenHelper {


    private static final int DATABASE_VERSION = 12;

    private static final String DATABASE_NAME = "somanami.db";

    public static final String TABLE_NOTIFICATIONS= "notifications";
    public static final String TABLE_MESSAGES= "messages";
    public static final String TABLE_BOOKS = "books";
    public static final String TABLE_TRAY="tray";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_BOOK = "book";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_USER = "user";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_IS_MINE = "is_mine";

    //book columns
    private static final String COLUMN_BOOK_ID = "_book_id";
    private static final String COLUMN_BOOK_THUMB = "_thumb_url";
    private static final String COLUMN_BOOK_TITLE = "_title";
    private static final String COLUMN_BOOK_DESCRIPTION = "_description";
    private static final String COLUMN_BOOK_DATE = "_date";
    private static final String COLUMN_BOOK_URL = "_url";
    private static final String COLUMN_BOOK_OWNER = "_owner";
    private static final String COLUMN_BOOK_OWNER_NAME = "_owner_name";
    private static final String COLUMN_BOOK_PUBLISHER = "_publisher";
    private static final String COLUMN_BOOK_GID = "_gid";
    private static final String COLUMN_BOOK_AUTHORS = "_authors";
    private static final String COLUMN_BOOK_PAGES = "_pages";
    private static final String COLUMN_BOOK_CATEGORIES = "_categories";

    //tray columns
    private static final String COLUMN_TRAY_ID = "_tray_id";
    private static final String COLUMN_DATE_DUE = "_date_due";
    private static final String COLUMN_BORROWED = "_borrowed";
    private static final String COLUMN_LENT = "_lent";
    private static final String COLUMN_PERSON_ID = "_person_id";
    private static final String COLUMN_PERSON_NAME = "_person_name";
    private static final String COLUMN_TRAY_BOOK_ID = "_book_id";
    private static final String COLUMN_TRAY_BOOK_THUMB = "_book_thumb";
    private static final String COLUMN_TRAY_BOOK_TITLE = "_book_title";


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
                + COLUMN_TYPE + " TEXT"
                + "); ";
        db.execSQL(CREATE_CONTENTS_TABLE);

        String CREATE_MESSAGES_TABLE = "CREATE TABLE " +
                TABLE_MESSAGES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + COLUMN_MESSAGE + " TEXT,"
                + COLUMN_USER + " TEXT,"
                + COLUMN_IS_MINE + " TEXT"
                + "); ";
        db.execSQL(CREATE_MESSAGES_TABLE);

        String CREATE_BOOKS_TABLE = "CREATE TABLE " +
                TABLE_BOOKS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + COLUMN_BOOK_ID + " TEXT,"
                + COLUMN_BOOK_THUMB + " TEXT,"
                + COLUMN_BOOK_TITLE + " TEXT,"
                + COLUMN_BOOK_DESCRIPTION + " TEXT,"
                + COLUMN_BOOK_DATE + " TEXT,"
                + COLUMN_BOOK_URL + " TEXT,"
                + COLUMN_BOOK_OWNER + " TEXT,"
                + COLUMN_BOOK_OWNER_NAME + " TEXT,"
                + COLUMN_BOOK_PUBLISHER + " TEXT,"
                + COLUMN_BOOK_GID + " TEXT,"
                + COLUMN_BOOK_AUTHORS + " TEXT,"
                + COLUMN_BOOK_PAGES + " TEXT,"
                + COLUMN_BOOK_CATEGORIES + " TEXT"
                + "); ";
        db.execSQL(CREATE_BOOKS_TABLE);

        String CREATE_TRAY_TABLE = "CREATE TABLE " +
                TABLE_TRAY + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + COLUMN_TRAY_ID + " TEXT,"
                + COLUMN_DATE_DUE + " TEXT,"
                + COLUMN_BORROWED + " TEXT,"
                + COLUMN_LENT + " TEXT,"
                + COLUMN_PERSON_ID + " TEXT,"
                + COLUMN_PERSON_NAME + " TEXT,"
                + COLUMN_TRAY_BOOK_ID + " TEXT,"
                + COLUMN_TRAY_BOOK_THUMB + " TEXT,"
                + COLUMN_TRAY_BOOK_TITLE + " TEXT"
                + "); ";
        db.execSQL(CREATE_TRAY_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRAY);
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

    public void addBook(Book book) {

        ContentValues values = new ContentValues();


        values.put(COLUMN_BOOK_ID, book.getId());
        values.put(COLUMN_BOOK_THUMB, book.getThumb_url());
        values.put(COLUMN_BOOK_TITLE, book.getTitle());
        values.put(COLUMN_BOOK_DESCRIPTION, book.getDescription());
        values.put(COLUMN_BOOK_DATE, book.getDate());
        values.put(COLUMN_BOOK_URL, book.getUrl());
        values.put(COLUMN_BOOK_OWNER, book.getOwner());
        values.put(COLUMN_BOOK_OWNER_NAME, book.getOwnerName());
        values.put(COLUMN_BOOK_PUBLISHER, book.getPublisher());
        values.put(COLUMN_BOOK_GID, book.getGid());
        values.put(COLUMN_BOOK_AUTHORS, book.getAuthors());
        values.put(COLUMN_BOOK_PAGES, book.getPages());
        values.put(COLUMN_BOOK_CATEGORIES, book.getCategories());

        SQLiteDatabase db = this.getWritableDatabase();

        db.insert(TABLE_BOOKS, null, values);
        db.close();
    }

    public void addMessage(Message message) {

        ContentValues values = new ContentValues();

        values.put(COLUMN_MESSAGE, message.getMessage());
        values.put(COLUMN_USER, message.getUser());
        values.put(COLUMN_IS_MINE , message.getIsMine());

        SQLiteDatabase db = this.getWritableDatabase();

        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }

    public void addTrayItem(TrayItem trayItem) {

        ContentValues values = new ContentValues();

        values.put(COLUMN_TRAY_ID, trayItem.getId());
        values.put(COLUMN_DATE_DUE, trayItem.getDate_due());
        values.put(COLUMN_BORROWED , trayItem.getBorrowed());
        values.put(COLUMN_LENT , trayItem.getLent());
        values.put(COLUMN_PERSON_ID , trayItem.getPerson_id());
        values.put(COLUMN_PERSON_NAME, trayItem.getPerson_name());
        values.put(COLUMN_TRAY_BOOK_ID , trayItem.getBook_id());
        values.put(COLUMN_TRAY_BOOK_THUMB , trayItem.getBook_thumb());
        values.put(COLUMN_TRAY_BOOK_TITLE , trayItem.getBook_title());

        SQLiteDatabase db = this.getWritableDatabase();

        db.insert(TABLE_TRAY, null, values);
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

    public boolean bookExists(String sid){

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor mCount= db.rawQuery("select count(*) from " + TABLE_BOOKS + " where " + COLUMN_BOOK_ID + " ='" + sid + "'", null);
        mCount.moveToFirst();
        int count= mCount.getInt(0);
        mCount.close();

        if(count>0){
            return true;
        }else{
            return false;
        }
    }

    public boolean trayItemExists(String sid){

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor mCount= db.rawQuery("select count(*) from " + TABLE_TRAY + " where " + COLUMN_TRAY_ID + " ='" + sid + "'", null);
        mCount.moveToFirst();
        int count= mCount.getInt(0);
        mCount.close();

        if(count>0){
            return true;
        }else{
            return false;
        }
    }

    public List<TrayItem> getTrayItems() {
        List<TrayItem> bookList = new ArrayList<TrayItem>();

        String selectQuery;

        selectQuery = "SELECT * FROM " + TABLE_TRAY + " ORDER BY " + COLUMN_ID + " DESC";


        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                TrayItem content = new TrayItem();

                content.setId((cursor.getString(1)));
                content.setDate_due((cursor.getString(2)));
                content.setBorrowed(cursor.getString(3));
                content.setLent((cursor.getString(4)));
                content.setPerson_id((cursor.getString(5)));
                content.setPerson_name((cursor.getString(6)));
                content.setBook_id((cursor.getString(7)));
                content.setBook_thumb((cursor.getString(8)));
                content.setBook_title((cursor.getString(9)));

                bookList.add(content);
            } while (cursor.moveToNext());
        }
        return bookList;
    }

    public List<Book> getBooks(String user) {
        List<Book> bookList = new ArrayList<Book>();

        String selectQuery;

        if(user==null){

            selectQuery = "SELECT * FROM " + TABLE_BOOKS + " ORDER BY " + COLUMN_ID + " DESC";

        }else{

            selectQuery = "SELECT * FROM " + TABLE_BOOKS + " WHERE "+ COLUMN_BOOK_OWNER +" ='"+ user+"' ORDER BY " + COLUMN_ID + " DESC";

        }

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Book content = new Book();

                content.setId((cursor.getString(1)));
                content.setThumb_url((cursor.getString(2)));
                content.setTitle((cursor.getString(3)));
                content.setDescription((cursor.getString(4)));
                content.setDate((cursor.getString(5)));
                content.setUrl((cursor.getString(6)));
                content.setOwner((cursor.getString(7)));
                content.setOwnerName((cursor.getString(8)));
                content.setPublisher((cursor.getString(9)));
                content.setGid((cursor.getString(10)));
                content.setAuthors((cursor.getString(11)));
                content.setPages((cursor.getString(12)));
                content.setCategories((cursor.getString(13)));

                bookList.add(content);
            } while (cursor.moveToNext());
        }
        return bookList;
    }

    public List<NotificationGCM> getNotifications() {
        List<NotificationGCM> notificationsList = new ArrayList<NotificationGCM>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NOTIFICATIONS + " ORDER BY " + COLUMN_ID + " DESC";

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

    public List<Message> getMessages(String user_id) {
        List<Message> messagesList = new ArrayList<Message>();
        // Select All Query
        //String selectQuery = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + COLUMN_USER + " ='" + user_id + "'";
        String selectQuery = "SELECT * FROM " + TABLE_MESSAGES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Message content = new Message();

                content.setMessage((cursor.getString(1)));
                content.setUser((cursor.getString(2)));
                content.setIsMine((cursor.getString(3)));

                messagesList.add(content);
            } while (cursor.moveToNext());
        }

        // return messages as a list
        return messagesList;
    }


    public boolean deleteContent(String content_id) {

            SQLiteDatabase db = this.getWritableDatabase();

            db.delete(TABLE_NOTIFICATIONS, COLUMN_ID + "='" + content_id + "'", null);

            db.close();

        return true;
    }
}