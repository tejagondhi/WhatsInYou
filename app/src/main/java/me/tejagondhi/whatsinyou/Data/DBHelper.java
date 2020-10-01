package me.tejagondhi.whatsinyou.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "WhatsInYou.db";
    public static final String FEED_TABLE_NAME = "feedData";
    public static final String FEED_COLUMN_ID = "id";
    public static final String FEED_COLUMN_SOURCE = "source";
    public static final String FEED_COLUMN_URL = "url";
    public static final String FEED_COLUMN_TYPE = "type";
    public static final String FEED_COLUMN_NAME = "name";
    public static final String FEED_COLUMN_ORIGINAL_URL = "original_url";
    public static final String FEED_COLUMN_HEIGHT = "height";
    public static final String FEED_COLUMN_WIDTH = "width";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table feedData " +
                        "(id integer primary key AUTOINCREMENT, name text UNIQUE,source text,type text, url text , original_url text, height text, width text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS feedData");
        onCreate(db);
    }

    public boolean insertFeed (@NonNull FeedDataObject data) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FEED_COLUMN_NAME, data.getName());
        contentValues.put(FEED_COLUMN_URL, data.getUrl());
        contentValues.put(FEED_COLUMN_TYPE, data.getIsVideo()?"VIDEO":"IMAGE");
        contentValues.put(FEED_COLUMN_SOURCE, data.getSource());
        contentValues.put(FEED_COLUMN_ORIGINAL_URL, data.getOriginalURL());
        contentValues.put(FEED_COLUMN_HEIGHT,data.getHeight());
        contentValues.put(FEED_COLUMN_WIDTH,data.getWidth());
        try{
            db.insertOrThrow(FEED_TABLE_NAME, null, contentValues);
        }catch (SQLiteConstraintException e){
            return false;
        }
        return true;
    }

    public Cursor getFeed(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( "select * from "+FEED_TABLE_NAME+" where id="+id+"", null );
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, FEED_TABLE_NAME);
    }

    public boolean updateFeed (@NonNull FeedDataObject updateList,String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FEED_COLUMN_URL, updateList.getUrl());
        db.update(FEED_TABLE_NAME, contentValues, "id = ? ", new String[] { id } );
        return true;
    }

    public Boolean deleteFeed (String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(FEED_TABLE_NAME,
                "id = ? ",
                new String[] { id }) > 0;
    }

    public ArrayList<FeedDataObject> getFeed() {
        ArrayList<FeedDataObject> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+FEED_TABLE_NAME+" ORDER BY id DESC", null );
        res.moveToFirst();

        while(!res.isAfterLast()){
            FeedDataObject data = new FeedDataObject();
            data.setID(res.getString(res.getColumnIndex(FEED_COLUMN_ID)));
            data.setName(res.getString(res.getColumnIndex(FEED_COLUMN_NAME)));
            data.setUrl(res.getString(res.getColumnIndex(FEED_COLUMN_URL)));
            data.setIsVideo(res.getString(res.getColumnIndex(FEED_COLUMN_TYPE)).equalsIgnoreCase("VIDEO"));
            data.setSource(res.getString(res.getColumnIndex(FEED_COLUMN_SOURCE)));
            data.setOriginalURL(res.getString(res.getColumnIndex(FEED_COLUMN_ORIGINAL_URL)));
            data.setHeight(res.getString(res.getColumnIndex(FEED_COLUMN_HEIGHT)));
            data.setWidth(res.getString(res.getColumnIndex(FEED_COLUMN_WIDTH)));
            array_list.add(data);
            res.moveToNext();
        }
        res.close();
        return array_list;
    }
}
