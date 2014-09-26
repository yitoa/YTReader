
package com.yt.reader.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class ReaderProvider extends ContentProvider {
    private DatabaseHelper mOpenHelper;

    private SQLiteDatabase db;

    private static final UriMatcher sUriMatcher;

    private static final int BOOKS = 1;

    private static final int BOOK_ID = 2;

    private static final int BOOK_PATH = 3;

    private static final int BOOKMARK = 4;

    private static final int CHAPTER = 5;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(DBSchema.AUTHORITY, DBSchema.TABLE_BOOK, BOOKS);
        sUriMatcher.addURI(DBSchema.AUTHORITY, DBSchema.TABLE_BOOK + "/"
                + DBSchema.COLUMN_BOOK_PATH, BOOK_PATH);
        sUriMatcher.addURI(DBSchema.AUTHORITY, DBSchema.TABLE_BOOK + "/#", BOOK_ID);
        sUriMatcher.addURI(DBSchema.AUTHORITY, DBSchema.TABLE_BOOKMARK, BOOKMARK);
        sUriMatcher.addURI(DBSchema.AUTHORITY, DBSchema.TABLE_CHAPTER, CHAPTER);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        db = mOpenHelper.getReadableDatabase();
        Cursor c;
        String where;
        switch (sUriMatcher.match(uri)) {
            case BOOKS:
                c = db.query(DBSchema.TABLE_BOOK, projection, selection, selectionArgs, null, null,
                        sortOrder);
                break;
            case BOOK_PATH:
                c = db.query(true, DBSchema.TABLE_BOOK, projection, selection, selectionArgs, null,
                        null, null, sortOrder);
                break;
            case BOOK_ID:
                long id = ContentUris.parseId(uri);
                where = BaseColumns._ID + "=" + id;
                where += !TextUtils.isEmpty(selection) ? " and (" + selection + ")" : "";
                c = db.query(DBSchema.TABLE_BOOK, projection, where, selectionArgs, null, null,
                        sortOrder);
                break;
            case BOOKMARK:
                c = db.query(DBSchema.TABLE_BOOKMARK, projection, selection, selectionArgs, null,
                        null, sortOrder);
                break;
            case CHAPTER:
                c = db.query(DBSchema.TABLE_CHAPTER, projection, selection, selectionArgs, null,
                        null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case BOOKS:
                count = db.delete(DBSchema.TABLE_BOOK, selection, selectionArgs);
                break;
            case BOOK_ID:
                long id = ContentUris.parseId(uri);
                String where = BaseColumns._ID + "=" + id;
                where += !TextUtils.isEmpty(selection) ? " and (" + selection + ")" : "";
                count = db.delete(DBSchema.TABLE_BOOK, where, selectionArgs);
                break;
            case BOOKMARK:
                count = db.delete(DBSchema.TABLE_BOOKMARK, selection, selectionArgs);
                break;
            case CHAPTER:
                count = db.delete(DBSchema.TABLE_CHAPTER, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case BOOKS:
                return DBSchema.BOOKS_TYPE;
            case BOOK_PATH:
                return DBSchema.BOOKS_TYPE;
            case BOOK_ID:
                return DBSchema.BOOKS_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        db = mOpenHelper.getWritableDatabase();
        long id = 0;
        switch (sUriMatcher.match(uri)) {
            case BOOKS:
                id = db.insert(DBSchema.TABLE_BOOK, "name", values);
                return ContentUris.withAppendedId(uri, id);
            case BOOK_ID:
                id = db.insert(DBSchema.TABLE_BOOK, "name", values);
                String path = uri.toString();
                return Uri.parse(path.substring(0, path.lastIndexOf("/")) + id);
            case BOOKMARK:
                id = db.insert(DBSchema.TABLE_BOOKMARK, null, values);
                return ContentUris.withAppendedId(uri, id);
            case CHAPTER:
                id = db.insert(DBSchema.TABLE_CHAPTER, null, values);
                return ContentUris.withAppendedId(uri, id);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case BOOKS:
                count = db.update(DBSchema.TABLE_BOOK, values, selection, selectionArgs);
                break;
            case BOOK_ID:
                long id = ContentUris.parseId(uri);
                String where = BaseColumns._ID + "=" + id;
                where += !TextUtils.isEmpty(selection) ? " and (" + selection + ")" : "";
                count = db.update(DBSchema.TABLE_BOOK, values, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return count;
    }

}
