
package com.yt.reader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.yt.reader.activity.BookCaseActivity;
import com.yt.reader.activity.MoreBookActivity;
import com.yt.reader.activity.SettingsActivity;
import com.yt.reader.activity.WifiActivity;
import com.yt.reader.activity.WifiConnectActivity;
import com.yt.reader.adapter.BookAdapter;
import com.yt.reader.database.DBSchema;
import com.yt.reader.database.OpenDataBase;
import com.yt.reader.model.Book;
import com.yt.reader.utils.FileUtils;
import com.yt.reader.utils.HerVer;
import com.yt.reader.utils.StringUtils;

public class MainActivity extends Activity {
    // 获取当前页
    private TextView currentPage;

    // 获取总页面
    private TextView totalPage;

    private Book readingBook; // 正在阅读的书籍

    private List<Book> list; // 最近阅读的书籍

    private List<Book> newBookList; // 新添加的书籍

    private int landPortState; // 横竖屏的状态 1:横屏，2:竖屏

    private static final int PROGRESS_DIALOG = 0;

    private ProgressDialog progressDialog;

    private ProgressThread progressThread;

    private Bitmap bm = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 隐藏标题
    }

    // 启动或返回时调用
    @Override
    protected void onResume() {
        super.onResume();
        HerVer hv = new HerVer(this);// 横竖屏
        hv.setHerOrVer();
        SharedPreferences isherVerPre = getSharedPreferences("herver", -1);
        String state = isherVerPre.getString("isherver", "1");
        // 判断横竖屏
        if ("2".equals(state)) {// 横屏
            setContentView(R.layout.main);
            landPortState = 1;
        } else if ("1".equals(state)) {// 竖屏
            setContentView(R.layout.mainport);
            landPortState = 2;
        }
        // 获取书籍的封面
        TextView bookCover = (TextView) findViewById(R.id.bookCover);

        currentPage = (TextView) findViewById(R.id.currentPage);

        totalPage = (TextView) findViewById(R.id.totalPage);

        LinearLayout bookCoverLL = (LinearLayout) findViewById(R.id.bookCoverLL);

        OpenDataBase opd = new OpenDataBase();
        // 获取正在读书的信息

        ContentResolver resolver = this.getContentResolver();
        Cursor c = resolver.query(DBSchema.CONTENT_URI_BOOK, new String[] {
                DBSchema.COLUMN_BOOK_COVER_PATH, DBSchema.COLUMN_BOOK_PATH,
                DBSchema.COLUMN_BOOK_NAME, DBSchema.COLUMN_BOOK_FILETYPE,
                DBSchema.COLUMN_BOOK_CURRENT_LOCATION, DBSchema.COLUMN_BOOK_TOTAL_PAGE
        }, null, null, "lastReadingTime desc");
        readingBook = opd.getReaderingBook(c);
        if (readingBook == null) {
            bookCoverLL.setBackgroundResource(R.drawable.book1);
            bookCover.setText(R.string.untitled);
            currentPage.setText("0");
            totalPage.setText("/0");
        } else {
            if (readingBook.getCoverPath() == null) {
                bookCoverLL.setBackgroundResource(R.drawable.book1);
                bookCover.setText(StringUtils.subString(readingBook.getName(), 16));
            } else {
                // bookCoverLL.setBackgroundDrawable(BitmapDrawable(readingBook.getBitmap()));
                getCoverBitmap(readingBook.getCoverPath());
                if (bm == null) {
                    bookCoverLL.setBackgroundResource(R.drawable.book1);
                    bookCover.setText(StringUtils.subString(readingBook.getName(), 16));
                } else {
                    bookCoverLL.setBackgroundDrawable(new BitmapDrawable(bm));
                }

            }
            bookCoverLL.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    FileUtils.openBook(MainActivity.this, readingBook);

                }
            });

            setReadingPercent(readingBook);
        }
        c.close();

        Cursor cursor = resolver.query(DBSchema.CONTENT_URI_BOOK, new String[] {
                DBSchema.COLUMN_BOOK_NAME, "_id", DBSchema.COLUMN_BOOK_AUTHOR,
                DBSchema.COLUMN_BOOK_LAST_READING_TIME, DBSchema.COLUMN_BOOK_SIZE,
                DBSchema.COLUMN_BOOK_ADDED_TIME, DBSchema.COLUMN_BOOK_PATH
        }, DBSchema.COLUMN_BOOK_LAST_READING_TIME + " is not null", null,
                "lastReadingTime desc limit 0,4");
        list = opd.geRecentlyBook(cursor);
        cursor.close();
        // 获取最近读取书籍列表
        ListView recentlyReadList = (ListView) findViewById(R.id.recentlyReadList);
        recentlyReadList.setAdapter(new BookAdapter(landPortState, list, this));
        // 点击事件
        recentlyReadList.setOnItemClickListener(new recentlyReadItemOnClickListener());

        // 新加数目
        Cursor newbcursor = resolver.query(DBSchema.CONTENT_URI_BOOK, new String[] {
                DBSchema.COLUMN_BOOK_NAME, "_id", DBSchema.COLUMN_BOOK_PATH,
                DBSchema.COLUMN_BOOK_COVER_PATH
        }, null, null, DBSchema.COLUMN_BOOK_ADDED_TIME + "  desc limit 0,4");
        // 获取新添加的书籍信息
        newBookList = opd.getNewBookAdd(newbcursor);

        newbcursor.close();
        setNewBook(newBookList);

        // 进入书柜操作
        Button bookcase = (Button) findViewById(R.id.bookcase);
        bookcase.setOnClickListener(new bookcaseListener(bookcase));
        bookcase.setClickable(true);

        // 更多记录
        Button moreT = (Button) findViewById(R.id.more);
        moreT.setOnClickListener(new MoreBooksListener(moreT));
        moreT.setClickable(true);

        ImageView settingsV = (ImageView) findViewById(R.id.settings);
        settingsV.setOnClickListener(new SettingsListener());
    }

    private void getCoverBitmap(String path) {
        try {
            File file = new File(path);
            FileInputStream fis;
            fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // 调用下一个活动前释放资源
    @Override
    protected void onPause() {
        super.onPause();
        recycleBooks();
        System.gc();
    }

    private void recycleBooks() {
        // 正在阅读的书籍
        if (null != readingBook) {
            Bitmap ingBitmap = readingBook.getBitmap();
            if (null != ingBitmap && !ingBitmap.isRecycled()) {
                ingBitmap.recycle();
                ingBitmap = null;
            }
        }

        // 最近阅读书籍
        list.clear();

        // 新增书籍
        for (Book b : newBookList) {
            Bitmap bit = b.getBitmap();
            if (null != bit && !bit.isRecycled()) {
                bit.recycle();
                bit = null;
            }
        }
        newBookList.clear();
    }

    private class recentlyReadItemOnClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            Book book = list.get(arg2);
            String path = book.getPath();
            path = path + "/" + book.getName();
            File file = new File(path);
            if (file.isFile()) {// 文件
                FileUtils.openBook(MainActivity.this, book);
            }
        }

    }

    // 点击设置的监听
    public class SettingsListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            MainActivity.this.finish();
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(intent);
        }

    }

    // 点击更多的监听
    public class MoreBooksListener implements OnClickListener {
        private Button button;

        public MoreBooksListener(Button button) {
            this.button = button;
        }

        @Override
        public void onClick(View v) {
            button.setClickable(false);
            Intent intent = new Intent(MainActivity.this, MoreBookActivity.class);
            MainActivity.this.startActivity(intent);
        }

    }

    /***
     * 进入书柜的监听
     * 
     * @author sbp
     */
    private class bookcaseListener implements OnClickListener {

        private Button button;

        public bookcaseListener(Button button) {
            this.button = button;
        }

        @Override
        public void onClick(View arg0) {
            // showDialog(PROGRESS_DIALOG);
            button.setClickable(false);
            Intent intent = new Intent(MainActivity.this, BookCaseActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("state", "1");
            intent.putExtras(bundle);
            MainActivity.this.startActivity(intent);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage(getString(R.string.entering));
                return progressDialog;
            default:
                return null;
        }
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case PROGRESS_DIALOG:
                progressDialog.setProgress(0);
                progressThread = new ProgressThread(handler);
                progressThread.start();
        }
    }

    private class ProgressThread extends Thread {
        Handler mHandler;

        ProgressThread(Handler h) {
            mHandler = h;
        }

        public void run() {
            Intent intent = new Intent(MainActivity.this, BookCaseActivity.class);
            MainActivity.this.startActivity(intent);
            Message msg = mHandler.obtainMessage();
            msg.arg1 = 100;
            mHandler.sendMessage(msg);
        }

    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            int total = msg.arg1;
            progressDialog.setProgress(total);
            if (total >= 100) {
                dismissDialog(PROGRESS_DIALOG);
            }
        }
    };

    /***
     * 设置添加最新书籍
     * 
     * @param newBookList
     */
    public void setNewBook(List<Book> newBookList) {
        String addNewBook1 = null;
        String addNewBook2 = null;
        String addNewBook3 = null;
        String addNewBook4 = null;
        if (newBookList.size() >= 4) {
            addNewBook1 = newBookList.get(0).getName();
            addNewBook2 = newBookList.get(1).getName();
            addNewBook3 = newBookList.get(2).getName();
            addNewBook4 = newBookList.get(3).getName();
        } else if (newBookList.size() == 3) {
            addNewBook1 = newBookList.get(0).getName();
            addNewBook2 = newBookList.get(1).getName();
            addNewBook3 = newBookList.get(2).getName();
        } else if (newBookList.size() == 2) {
            addNewBook1 = newBookList.get(0).getName();
            addNewBook2 = newBookList.get(1).getName();
        } else if (newBookList.size() == 1) {
            addNewBook1 = newBookList.get(0).getName();
        }
        TextView newadd1 = (TextView) findViewById(R.id.newadd1);
        TextView newadd2 = (TextView) findViewById(R.id.newadd2);
        TextView newadd3 = (TextView) findViewById(R.id.newadd3);
        TextView newadd4 = (TextView) findViewById(R.id.newadd4);

        if (addNewBook1 != null) {
            newadd1.setVisibility(View.VISIBLE);
            /*
             * if (newBookList.get(0).getCoverPath()!= null) {
             * getCoverBitmap(newBookList.get(0).getCoverPath());
             * newadd1.setBackgroundDrawable(new BitmapDrawable(bm)); } else {
             */
            newadd1.setBackgroundResource(R.drawable.book2);
            if (landPortState == 1) {
                newadd1.setText(StringUtils.subString(addNewBook1, 8));
            } else if (landPortState == 2) {
                newadd1.setText(StringUtils.subString(addNewBook1, 3));
            }
            // }

            newadd1.setOnClickListener(new NewAddOnClickListener(newBookList.get(0)));

        }

        if (addNewBook2 != null) {
            newadd2.setVisibility(View.VISIBLE);
            /*
             * if (newBookList.get(1).getCoverPath() != null) {
             * getCoverBitmap(newBookList.get(1).getCoverPath());
             * newadd2.setBackgroundDrawable(new BitmapDrawable(bm)); } else {
             */
            newadd2.setBackgroundResource(R.drawable.book2);
            if (landPortState == 1) {
                newadd2.setText(StringUtils.subString(addNewBook2, 8));
            } else if (landPortState == 2) {
                newadd2.setText(StringUtils.subString(addNewBook2, 3));
            }
            // }
            newadd2.setOnClickListener(new NewAddOnClickListener(newBookList.get(1)));

        }
        if (addNewBook3 != null) {
            newadd3.setVisibility(View.VISIBLE);
            /*
             * if (newBookList.get(2).getCoverPath() != null) {
             * getCoverBitmap(newBookList.get(2).getCoverPath());
             * newadd3.setBackgroundDrawable(new BitmapDrawable(bm)); } else {
             */
            newadd3.setBackgroundResource(R.drawable.book2);
            if (landPortState == 1) {
                newadd3.setText(StringUtils.subString(addNewBook3, 8));
            } else if (landPortState == 2) {
                newadd3.setText(StringUtils.subString(addNewBook3, 3));
            }
            // }
            newadd3.setOnClickListener(new NewAddOnClickListener(newBookList.get(2)));

        }
        if (addNewBook4 != null) {
            newadd4.setVisibility(View.VISIBLE);
            /*
             * if (newBookList.get(3).getCoverPath() != null) {
             * getCoverBitmap(newBookList.get(3).getCoverPath());
             * newadd4.setBackgroundDrawable(new BitmapDrawable(bm)); } else {
             */
            newadd4.setBackgroundResource(R.drawable.book2);
            if (landPortState == 1) {
                newadd4.setText(StringUtils.subString(addNewBook4, 8));
            } else if (landPortState == 2) {
                newadd4.setText(StringUtils.subString(addNewBook4, 3));
            }
            // }
            newadd4.setOnClickListener(new NewAddOnClickListener(newBookList.get(3)));

        }

    }

    public class NewAddOnClickListener implements OnClickListener {
        private Book book;

        public NewAddOnClickListener(Book book) {
            this.book = book;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.newadd1:
                    FileUtils.openBook(MainActivity.this, book);
                    break;
                case R.id.newadd2:
                    FileUtils.openBook(MainActivity.this, book);
                    break;
                case R.id.newadd3:
                    FileUtils.openBook(MainActivity.this, book);
                    break;
                case R.id.newadd4:
                    FileUtils.openBook(MainActivity.this, book);
                    break;
            }

        }

    }

    /**
     * 设置该书阅读的进度
     * 
     * @param book
     */
    private void setReadingPercent(Book book) {
        String fileType = book.getFileType();
        if (null == fileType && null != book.getName()) {
            fileType = FileUtils.getFileType(book.getName());
        }
        if (null == fileType)
            return;
        if (fileType.equalsIgnoreCase("PDF")) {// TODO 根据需要的格式添加
            currentPage.setText((book.getCurrentLocation() + 1) + "");
            totalPage.setText("/" + book.getTotalPage());
        } else {// 没有页码概念的其他格式
            if (0 > book.getTotalPage()) {
                currentPage.setText("-/-");
            } else {
                DecimalFormat df = new DecimalFormat("#0.0");
                float percent = (float) (book.getCurrentLocation() * 1.0 / book.getTotalPage());
                currentPage.setText(df.format(percent * 100) + "%");
            }
            totalPage.setText("");
        }
    }
}
