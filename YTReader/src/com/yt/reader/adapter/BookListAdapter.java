
package com.yt.reader.adapter;

import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import com.yt.reader.DocumentActivity;
import com.yt.reader.R;
import com.yt.reader.database.DBSchema;
import com.yt.reader.model.Book;
import com.yt.reader.utils.DateUtils;

public class BookListAdapter extends SimpleCursorTreeAdapter {
    private DocumentActivity context;

    private static final String[] PROJECTION = new String[] {
            BaseColumns._ID, DBSchema.COLUMN_BOOK_NAME, DBSchema.COLUMN_BOOK_SIZE,
            DBSchema.COLUMN_BOOK_LAST_MODIFY_TIME, DBSchema.COLUMN_BOOK_PATH
    };

    public BookListAdapter(DocumentActivity context, Cursor cursor, int groupLayout,
            String[] groupFrom, int[] groupTo, int childLayout, String[] childFrom, int[] childTo) {
        super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
        this.context = context;
    }

    @Override
    public View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
        View v = super.newGroupView(context, cursor, isExpanded, parent);
        return v;
    }

    @Override
    public long getGroupId(int groupPosition) {// TODO
        return groupPosition;
    }

    @Override
    public void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
        isExpanded = false;
        String s = cursor.getString(cursor.getColumnIndexOrThrow(DBSchema.COLUMN_BOOK_PATH));
        TextView tv = (TextView) view.findViewById(R.id.path);
        tv.setText(s);
    }

    @Override
    public View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {
        View v = super.newChildView(context, cursor, isLastChild, parent);
        return v;
    }

    @Override
    public void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        Book book = new Book();
        book.setId(cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID)));
        book.setPath(cursor.getString(cursor.getColumnIndexOrThrow(DBSchema.COLUMN_BOOK_PATH)));
        TextView tv = (TextView) view.findViewById(R.id.line1);
        String s = cursor.getString(cursor.getColumnIndexOrThrow(DBSchema.COLUMN_BOOK_NAME));
        book.setName(s);
        tv.setText(s);
        tv = (TextView) view.findViewById(R.id.line2);
        s = cursor.getString(cursor.getColumnIndexOrThrow(DBSchema.COLUMN_BOOK_SIZE));
        long time = cursor.getLong(cursor
                .getColumnIndexOrThrow(DBSchema.COLUMN_BOOK_LAST_MODIFY_TIME));
        Date date = DateUtils.getSystemDate(new Date(time));
        s = s + "    " + DateUtils.dateToString(date, "MMM dd, yyyy");
        tv.setText(s);
        view.setTag(book);
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        String path = groupCursor.getString(groupCursor
                .getColumnIndexOrThrow(DBSchema.COLUMN_BOOK_PATH));
        return context.managedQuery(context.getIntent().getData(), PROJECTION,
                DBSchema.COLUMN_BOOK_PATH + "=?", new String[] {
                    path
                }, DBSchema.COLUMN_BOOK_NAME);
    }
}
