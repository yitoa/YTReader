
package com.yt.reader.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

public class DBUtils {
    public static Cursor queryOrAdd(ContentResolver resolver, Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, sortOrder);

        return cursor;
    }
}
