
package com.yt.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;

import com.yt.reader.adapter.BookListAdapter;
import com.yt.reader.database.DBSchema;
import com.yt.reader.model.Book;
import com.yt.reader.utils.DateUtils;
import com.yt.reader.utils.FileUtils;

public class DocumentActivity extends ExpandableListActivity {
    private ProgressDialog dialog;

    private static final String TAG = "DocumentActivity";

    private SimpleCursorTreeAdapter adapter;

    private static final int PROGRESS_DIALOG = 0;

    private Uri mUri;

    private static final String[] PROJECTION = new String[] {
            BaseColumns._ID, DBSchema.COLUMN_BOOK_NAME, DBSchema.COLUMN_BOOK_REALNAME,
            DBSchema.COLUMN_BOOK_SIZE, DBSchema.COLUMN_BOOK_ADDED_TIME,
            DBSchema.COLUMN_BOOK_LAST_MODIFY_TIME, DBSchema.COLUMN_BOOK_ADDED_TIME,
            DBSchema.COLUMN_BOOK_PATH, DBSchema.COLUMN_BOOK_FILETYPE
    };

    private static final String[] PROJECTION2 = new String[] {
            BaseColumns._ID, DBSchema.COLUMN_BOOK_NAME, DBSchema.COLUMN_BOOK_SIZE,
            DBSchema.COLUMN_BOOK_LAST_MODIFY_TIME
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.document);
        final Intent intent = getIntent();
        if (null == intent.getData()) {
            intent.setData(DBSchema.CONTENT_URI_BOOK);
        }
        final String action = intent.getAction();
        if ((Intent.ACTION_DEFAULT).equals(action)) {
            mUri = intent.getData();
        } else if (Intent.ACTION_EDIT.equals(action)) {
            // Requested to edit: set that state, and the data being edited.
            mUri = intent.getData();
        } else if (Intent.ACTION_INSERT.equals(action)) {
            mUri = getContentResolver().insert(intent.getData(), null);
            if (mUri == null) {
                Log.e(TAG, "Failed to insert new note into " + getIntent().getData());
                finish();
                return;
            }
        }
        showDialog(PROGRESS_DIALOG);
        new TravelTask().execute(Environment.getExternalStorageDirectory());
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                dialog = new ProgressDialog(DocumentActivity.this);
                // dialog.setMessage(getString(R.string.loading));
                dialog.setMessage("Loading data....");
                return dialog;
            default:
                return null;
        }
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case PROGRESS_DIALOG:
        }

    }

    private class TravelTask extends AsyncTask<File, Void, List<String>> {
        private List<String> fNames = new ArrayList<String>();

        /**
         * The system calls this to perform work in a worker thread and delivers
         * it the parameters given to AsyncTask.execute()
         */
        protected List<String> doInBackground(File... files) {
            Cursor cursor = managedQuery(getIntent().getData(), PROJECTION, null, null, null);
            if (null != cursor && cursor.getCount() == 0) {
                travelFile(files[0]);
            }
            return fNames;
        }

        /**
         * The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground()
         */
        protected void onPostExecute(List<String> result) {

            // adapter = new ArrayAdapter<String>(DocumentActivity.this,
            // android.R.layout.simple_list_item_1, result);
            Uri uri = Uri.parse(getIntent().getData().toString() + "/" + DBSchema.COLUMN_BOOK_PATH);
            Cursor cursor = managedQuery(uri, new String[] {
                DBSchema.COLUMN_BOOK_PATH
            }, null, null, null);

            // Used to map notes entries from the database to views
            adapter = new BookListAdapter(DocumentActivity.this,
                    cursor, // cursor
                    R.layout.book_list_item_group, new String[] {}, new int[] {},
                    R.layout.book_list_item_child, new String[] {}, new int[] {});
            setListAdapter(adapter);
            dismissDialog(PROGRESS_DIALOG);
        }

        void travelFile(File f) {
            if (!f.exists())
                return;
            String fileType;
            if (f.isFile() && null != (fileType = FileUtils.getFileType(f.getAbsolutePath()))) {
                Book book = new Book();
                book.setName(f.getName());
                book.setRealName(FileUtils.getRealName(f));
                book.setSize(FileUtils.getFilesize(f));
                book.setLastModifyTime(DateUtils.getGreenwichDate(new Date(f.lastModified()))
                        .getTime());
                book.setAddedTime(DateUtils.getGreenwichDate(null).getTime());
                book.setPath(f.getParent());
                book.setFileType(fileType);
                // Log.v("saveBook",book.toString());
                saveBook(book);
            } else if (f.isDirectory()) {
                File[] mFiles = f.listFiles();
                if (null == mFiles)
                    return;
                for (File f1 : mFiles) {
                    travelFile(f1);
                }
            }
        }

        void saveBook(Book book) {
            Cursor cursor = managedQuery(getIntent().getData(), PROJECTION2, null, null, null);
            if (cursor != null) {
                // Get out updates into the provider.
                ContentValues values = new ContentValues();
                values.put(DBSchema.COLUMN_BOOK_NAME, book.getName());
                values.put(DBSchema.COLUMN_BOOK_REALNAME, book.getRealName());
                values.put(DBSchema.COLUMN_BOOK_LAST_MODIFY_TIME, book.getLastModifyTime());
                values.put(DBSchema.COLUMN_BOOK_ADDED_TIME, book.getAddedTime());
                values.put(DBSchema.COLUMN_BOOK_SIZE, book.getSize());
                values.put(DBSchema.COLUMN_BOOK_PATH, book.getPath());
                values.put(DBSchema.COLUMN_BOOK_FILETYPE, book.getFileType());
                try {
                    getContentResolver().insert(mUri, values);
                } catch (NullPointerException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
            int childPosition, long id) {
        Book book = (Book) v.getTag();
        FileUtils.openBook(this, book);
        return true;
    }

}
