package com.example.evolv;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.mindrot.jbcrypt.BCrypt;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "evolv.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_USERS = "users";
    private static final String COL_ID = "id";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";

    private static final String CREATE_TABLE_USERS = 
        "CREATE TABLE " + TABLE_USERS + "(" +
        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COL_USERNAME + " TEXT UNIQUE NOT NULL, " +
        COL_PASSWORD + " TEXT NOT NULL)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public boolean insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COL_USERNAME, username);
            values.put(COL_PASSWORD, BCrypt.hashpw(password, BCrypt.gensalt()));
            return db.insert(TABLE_USERS, null, values) != -1;
        } catch (Exception e) {
            return false;
        } finally {
            db.close();
        }
    }

    public String checkLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String[] columns = {COL_USERNAME, COL_PASSWORD};
            String selection = COL_USERNAME + "=?";
            String[] selectionArgs = {username};
            
            Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                String storedHash = cursor.getString(cursor.getColumnIndex(COL_PASSWORD));
                String storedUsername = cursor.getString(cursor.getColumnIndex(COL_USERNAME));
                cursor.close();
                
                return BCrypt.checkpw(password, storedHash) ? storedUsername : null;
            }
            return null;
        } finally {
            db.close();
        }
    }

    public boolean isUsernameAvailable(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String[] columns = {COL_USERNAME};
            String selection = COL_USERNAME + "=?";
            String[] selectionArgs = {username};
            
            Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
            boolean isAvailable = cursor == null || cursor.getCount() == 0;
            if (cursor != null) {
                cursor.close();
            }
            return isAvailable;
        } finally {
            db.close();
        }
    }
}
