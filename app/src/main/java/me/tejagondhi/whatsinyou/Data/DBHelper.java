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

    public boolean insertFeed (String name,String url,String originalURL, String type, String source, String height, String width) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FEED_COLUMN_NAME, name);
        contentValues.put(FEED_COLUMN_URL, url);
        contentValues.put(FEED_COLUMN_TYPE, type);
        contentValues.put(FEED_COLUMN_SOURCE, source);
        contentValues.put(FEED_COLUMN_ORIGINAL_URL, originalURL);
        contentValues.put(FEED_COLUMN_HEIGHT,height);
        contentValues.put(FEED_COLUMN_WIDTH,width);
        try{
            db.insertOrThrow(FEED_TABLE_NAME, null, contentValues);
        }catch (SQLiteConstraintException e){
            Log.i("","");
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

    public boolean updateFeed (String name,Integer id, String url,String originalURL, String type, String source, String height, String width) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FEED_COLUMN_NAME, name);
        contentValues.put(FEED_COLUMN_URL, url);
        contentValues.put(FEED_COLUMN_TYPE, type);
        contentValues.put(FEED_COLUMN_SOURCE, source);
        contentValues.put(FEED_COLUMN_ORIGINAL_URL, originalURL);
        contentValues.put(FEED_COLUMN_HEIGHT,height);
        contentValues.put(FEED_COLUMN_WIDTH,width);
        db.update(FEED_TABLE_NAME, contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }
    public boolean updateFeed (HashMap<String,Instagram> updateList) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (String key:updateList.keySet()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(FEED_COLUMN_URL, updateList.get(key).getUrl());
            db.update(FEED_TABLE_NAME, contentValues, "id = ? ", new String[] { key } );
        }
        return true;
    }


    public Integer deleteFeed (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(FEED_TABLE_NAME,
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<HashMap<String,String>> getFeed() {
        ArrayList<HashMap<String,String>> array_list = new ArrayList<>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+FEED_TABLE_NAME+" ORDER BY id DESC", null );
        res.moveToFirst();

        while(!res.isAfterLast()){
            HashMap<String,String> hashMapList = new  HashMap<String, String>();
            hashMapList.put("ID",res.getString(res.getColumnIndex(FEED_COLUMN_ID)));
            hashMapList.put("NAME",res.getString(res.getColumnIndex(FEED_COLUMN_NAME)));
            hashMapList.put("URL",res.getString(res.getColumnIndex(FEED_COLUMN_URL)));
            hashMapList.put("TYPE",res.getString(res.getColumnIndex(FEED_COLUMN_TYPE)));
            hashMapList.put("SOURCE",res.getString(res.getColumnIndex(FEED_COLUMN_SOURCE)));
            hashMapList.put("ORIGINAL_URL",res.getString(res.getColumnIndex(FEED_COLUMN_ORIGINAL_URL)));
            hashMapList.put("HEIGHT",res.getString(res.getColumnIndex(FEED_COLUMN_HEIGHT)));
            hashMapList.put("WIDTH",res.getString(res.getColumnIndex(FEED_COLUMN_WIDTH)));
            array_list.add(hashMapList);
            res.moveToNext();
        }
        res.close();
        return array_list;
    }
}
