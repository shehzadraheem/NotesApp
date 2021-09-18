package com.example.notesapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.notesapp.model.Notes;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME="Notes.db";
    public static final String TABLE_NAME="notes";
    public static final String COL_1="ID";
    public static final String COL_2="TITLE";
    public static final String COL_3="SUBTITLE";
    public static final String COL_4="DATETIME";
    public static final String COL_5="IMAGE";
    public static final String COL_6="NOTETEXT";
    public static final String COL_7="COLOR";
    public static final String COL_8="WEBLINK";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table "+TABLE_NAME+ "(ID INTEGER PRIMARY KEY AUTOINCREMENT ," +
                " TITLE VARCHAR,SUBTITLE VARCHAR , DATETIME VARCHAR , IMAGE VARCHAR , NOTETEXT VARCHAR," +
                "COLOR VARCHAR , WEBLINK VARCHAR)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists "+TABLE_NAME);
        onCreate(db);
    }

    public boolean insert(Notes notes){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_2,notes.getTitle());
        values.put(COL_3,notes.getSubtitle());
        values.put(COL_4,notes.getDateTime());
        values.put(COL_5,notes.getImagePath());
        values.put(COL_6,notes.getNoteText());
        values.put(COL_7,notes.getColor());
        values.put(COL_8,notes.getWebLink());


        long success = db.insert(TABLE_NAME,null,values);
        if(success==-1){
            return false;
        }else {
            return true;
        }
    }

    public Cursor getAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cur = db.rawQuery("select * from "+TABLE_NAME+" ORDER by ID DESC",null);
        return cur;
    }
    public boolean updateData(Notes notes){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_1,notes.getId());
        values.put(COL_2,notes.getTitle());
        values.put(COL_3,notes.getSubtitle());
        values.put(COL_4,notes.getDateTime());
        values.put(COL_5,notes.getImagePath());
        values.put(COL_6,notes.getNoteText());
        values.put(COL_7,notes.getColor());
        values.put(COL_8,notes.getWebLink());
        db.update(TABLE_NAME,values,"ID=?",new String[]{String.valueOf(notes.getId())});
        return true;
    }

    public Integer delete(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME,"ID=?",new String[]{id});
    }
}
