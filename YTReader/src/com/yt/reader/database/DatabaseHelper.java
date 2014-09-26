
package com.yt.reader.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.yt.reader.utils.Constant;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, DBSchema.DB_NAME, null, DBSchema.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE  IF NOT EXISTS " + DBSchema.TABLE_BOOK + " (" + BaseColumns._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," 
        		+ DBSchema.COLUMN_BOOK_NAME + " TEXT,"
                + DBSchema.COLUMN_BOOK_REALNAME + " TEXT," 
                + DBSchema.COLUMN_BOOK_AUTHOR + " TEXT,"
                + DBSchema.COLUMN_BOOK_SIZE + " TEXT," 
                + DBSchema.COLUMN_BOOK_FILETYPE + " TEXT,"
                + DBSchema.COLUMN_BOOK_CURRENT_LOCATION + " INTEGER,"
                + DBSchema.COLUMN_BOOK_TOTAL_PAGE + " INTEGER,"
                + DBSchema.COLUMN_BOOK_LAST_MODIFY_TIME + " INTEGER,"
                + DBSchema.COLUMN_BOOK_LAST_READING_TIME + " INTEGER,"
                + DBSchema.COLUMN_BOOK_ADDED_TIME + " INTEGER," 
                + DBSchema.COLUMN_BOOK_PATH + " TEXT," 
                + DBSchema.COLUMN_BOOK_BITMAP + " BLOB," 
                + DBSchema.COLUMN_BOOK_COVER_PATH + " TEXT," 
                + DBSchema.COLUMN_BOOK_ISDRM + " INTEGER" + ");");

        db.execSQL("CREATE TABLE " + DBSchema.TABLE_BOOKMARK + " (" + BaseColumns._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + DBSchema.COLUMN_BOOKMARK_BOOKID
                + " INTEGER," + DBSchema.COLUMN_BOOKMARK_LOCATION + " INTEGER,"
                + DBSchema.COLUMN_BOOKMARK_DESCRIPTION + " TEXT" + ");");

        db.execSQL("CREATE TABLE " + DBSchema.TABLE_CHAPTER + " (" + BaseColumns._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + DBSchema.COLUMN_BOOKMARK_BOOKID
                + " INTEGER," + DBSchema.COLUMN_CHAPTER_LOCATION + " INTEGER,"
                + DBSchema.COLUMN_CHAPTER_TITLE + " TEXT" + ");");

        db.execSQL("CREATE TABLE " + DBSchema.TABLE_STYLE + " (" + BaseColumns._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + DBSchema.COLUMN_STYLE_TYPEFACE + " TEXT,"
                + DBSchema.COLUMN_STYLE_SIZE + " INTEGER," + DBSchema.COLUMN_STYLE_FONT_STYLE
                + " TEXT," + DBSchema.COLUMN_STYLE_LINE_SPACING + " INTEGER,"
                + DBSchema.COLUMN_STYLE_TEXT_COLOR + " INTEGER," + DBSchema.COLUMN_STYLE_BG_COLOR
                + " INTEGER," + DBSchema.COLUMN_STYLE_MARGIN_WIDTH + " INTEGER,"
                + DBSchema.COLUMN_STYLE_MARGIN_HEIGHT + " INTEGER,"
                + DBSchema.COLUMN_STYLE_IS_DEFAULT + " INTEGER" + ");");

        db.execSQL("INSERT INTO " + DBSchema.TABLE_STYLE + " VALUES(1," + "\""
                + Constant.STYLE_DEFAULT_TYPEFACE + "\"," + Constant.STYLE_DEFAULT_SIZE + ","
                + "\"" + Constant.STYLE_DEFAULT_FONT_STYLE + "\","
                + Constant.STYLE_DEFAULT_LINE_SPACING + "," + Constant.STYLE_DEFAULT_TEXT_COLOR
                + "," + Constant.STYLE_DEFAULT_BG_COLOR + "," + Constant.STYLE_DEFAULT_MARGIN_WIDTH
                + "," + Constant.STYLE_DEFAULT_MARGIN_HEIGHT + "," + "\""
                + Constant.STYLE_IS_DEFAULT + "\");");

        db.execSQL("CREATE TABLE " + DBSchema.TABLE_WIFI + " (" + BaseColumns._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + DBSchema.COLUMN_SSID + " TEXT,"
                + DBSchema.COLUMN_SECTYPE + " TEXT," + DBSchema.COLUMN_WIFIPASSWORD + " TEXT,"
                + DBSchema.COLUMN_ISCURRENTAPP + " INTEGER," + DBSchema.COLUMN_SIGNSTR + " INTEGER"
                + ");");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
    }

}
