
package com.yt.reader.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.yt.reader.MainActivity;
import com.yt.reader.R;
import com.yt.reader.YTReaderApplication;
import com.yt.reader.base.YTReaderActivity;
import com.yt.reader.database.DBSchema;
import com.yt.reader.database.OpenDataBase;
import com.yt.reader.model.Book;
import com.yt.reader.utils.BookFileTypeCompare;
import com.yt.reader.utils.BookNameCompare;
import com.yt.reader.utils.BookTimeCompare;
import com.yt.reader.utils.DateUtils;
import com.yt.reader.utils.FileUtils;
import com.yt.reader.utils.StringUtils;

public class BookCaseActivity extends YTReaderActivity {
    // 显示方式
    private PopupWindow showpop;

    // 排序方式
    private PopupWindow arraypop;

    private boolean isShowPop = false;

    private boolean isArraypop = false;

    private SharedPreferences settings;

    private View popupWindow_view = null;

    private View arrayPopView = null;

    private TextView showtype;

    private TextView arrayType;

    private TextView allbooks;// 显示多少条书籍信息

    // 用于显示每列5个Item项。
    private static int VIEW_COUNT = 5;

    // 用于显示页号的索引
    private static int index = 0;

    MoreAdapter ma;

    private TextView currentT;// 当前页面

    private TextView totalT;// 总页面

    private int totalBook;

    private ImageButton btnLeft;

    private ImageButton btnRight;

    private List<Book> booklist;// 存放查询结果的

    private ListView booksList;

    private String path;// 存放路径

    private ImageButton sdsel;// sd图片

    private ImageButton phonesel;// 手机内存图片

    private ImageView deleteImage;// 删除图标

    private static int horverstat;// 横竖屏状态，默认竖屏

    private AlertDialog.Builder ad;// 删除文件的对话框

    private AlertDialog.Builder noSelFiles;// 没有选择文件的对话框

    private AlertDialog.Builder delFilesFails;// 没有选择文件的对话框

    private ContentResolver resolver;

    private OpenDataBase odb;

    private LinearLayout uplevelLL;

    private static final int PROGRESS_DIALOG = 0;

    private ProgressDialog progressDialog;

    private ProgressThread progressThread;

    private String showtypeStr;

    private String arrayStr;

    public YTReaderApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        odb = new OpenDataBase();
        resolver = this.getContentResolver();
        init();
    }

    private void init() {
        setContentView(R.layout.bookcase);
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        application = (YTReaderApplication) this.getApplication();
        if (bundle == null) {
            index = application.getPageNo();
        } else {
            index = 0;
            application.setPageNo(index);
        }
        // 判断横竖屏
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {// 横屏
            horverstat = 1;// 横屏为1
            VIEW_COUNT = 5;
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {// 竖屏
            horverstat = 2;
            VIEW_COUNT = 8;
        }

        // 删除图标
        deleteImage = (ImageView) findViewById(R.id.delete);
        // 获取Preferences中存放的数据
        settings = getSharedPreferences("settings", 0);
        showtypeStr = settings.getString("showtype", getResources().getString(R.string.books));

        showtypeStr = changPar(showtypeStr);
        // 点击显示类型
        showtype = (TextView) findViewById(R.id.showtype);
        showtype.setText(showtypeStr);
        // 排列方式
        arrayStr = settings.getString("array", getResources().getString(R.string.mrecent));
        arrayStr = changeArrayPar(arrayStr);
        arrayType = (TextView) findViewById(R.id.arrange);
        arrayType.setText(arrayStr);

        // 刷新按钮
        ImageButton bookcaseBack = (ImageButton) findViewById(R.id.bookcaseBack);
        bookcaseBack.setOnClickListener(new FreshAndSearchOnClickListener());
        // 存储为sd卡的
        sdsel = (ImageButton) findViewById(R.id.sdsel);
        // 手机里的内存
        phonesel = (ImageButton) findViewById(R.id.phonesel);

        String store = settings.getString("store", "sd");
        if ("sd".equals(store)) {
            File file = FileUtils.getSDPath();
            if (file != null) {
                path = file.toString() + "/tflash";
            } else {
                Toast.makeText(this, this.getResources().getString(R.string.nostorage),
                        Toast.LENGTH_LONG).show();
                return;
            }
            sdsel.setBackgroundResource(R.drawable.sdcard_c);
        } else {// 手机内存路径
            File file = FileUtils.getSDPath();
            if (file != null) {
                path = file.toString();
            } else {
                Toast.makeText(this, this.getResources().getString(R.string.nosd),
                        Toast.LENGTH_LONG).show();
                return;
            }
            phonesel.setBackgroundResource(R.drawable.local_c);
        }
        sdsel.setOnClickListener(new StroreListener());
        phonesel.setOnClickListener(new StroreListener());
        showtype.setOnClickListener(new ShowTypeListenter(1));
        arrayType.setOnClickListener(new ShowTypeListenter(2));
        deleteImage.setOnClickListener(new DeleteImageListener());
        // 书籍的列表显示
        booksList = (ListView) findViewById(R.id.booklist);

        allbooks = (TextView) findViewById(R.id.allbooks);

        currentT = (TextView) findViewById(R.id.currentPageCase);
        totalT = (TextView) findViewById(R.id.totalPageCase);

        btnLeft = (ImageButton) findViewById(R.id.left);
        btnRight = (ImageButton) findViewById(R.id.right);

        btnLeft.setOnClickListener(new PageButton());
        btnRight.setOnClickListener(new PageButton());
        showDialog(PROGRESS_DIALOG);

        booksList.setOnItemClickListener(new BooksItemOnClickListener());
        // 滑动分页
        booksList.setOnTouchListener(new BooksListOnTounchListener());

        // 搜索按钮
        ImageButton searchB = (ImageButton) findViewById(R.id.search);
        searchB.setOnClickListener(new FreshAndSearchOnClickListener());

        // 初始化上一级的参数
        uplevelLL = (LinearLayout) findViewById(R.id.uplevelLL);
        uplevelLL.setOnClickListener(new UpLevelOnClickListener());
        openOptionsDialog();
        noSelFilesDialog();
        delFileOrDirFails();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        // 主线程暂停2秒
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        enable_ebook_click = true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                progressDialog = new ProgressDialog(BookCaseActivity.this);
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
            booklist = selectType(showtypeStr, arrayStr);
            Log.i("bookCase", "after selectType， Thread id = " + Thread.currentThread().getId());
            Message msg = mHandler.obtainMessage();
            msg.arg1 = 100;
            mHandler.sendMessage(msg);
            new Thread() {
                @Override
                public void run() {
                    try {
                        // 数据库中数据的同步
                        FileUtils fileUtiles = new FileUtils(new ArrayList<Book>());
                        // 获取sd下所有符合条件的书籍，也可在添加另外的路径
                        List<Book> list = fileUtiles.getFiles(FileUtils.getSDPath().toString());
                        Log.i("bookCase", "after getFiles, Thread id = "
                                + Thread.currentThread().getId());
                        odb.saveBooks(list, resolver);
                        Log.i("bookCase", "after saveBooks");
                    } catch (Error e) {
                        Log.e("bookCase", "update books error, shit");
                    }
                }
            }.run();
        }

    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            int total = msg.arg1;
            ma = new MoreAdapter(BookCaseActivity.this, booklist, new HashMap<Integer, Boolean>(),
                    horverstat);
            booksList.setAdapter(ma);
            setTotalAndPage();
            checkButton();
            progressDialog.setProgress(total);
            if (total >= 100) {
                dismissDialog(PROGRESS_DIALOG);
            }
        }
    };

    private class UpLevelOnClickListener implements OnClickListener {

        @Override
        public void onClick(View arg0) {
            String upPath = path.substring(0, path.lastIndexOf("/"));
            if (upPath != null) {
                if (upPath.equals(FileUtils.getSDPath().toString())
                        || upPath.equals(FileUtils.getSDPath() + "/tflash")) {
                    uplevelLL.setVisibility(View.GONE);
                }
            }
            reLoadData(upPath);

        }

    }

    /***
     * 打开删除文件的对话框
     */
    private void openOptionsDialog() {// 打开对话框
        ad = new AlertDialog.Builder(this);
        ad.setIcon(android.R.drawable.ic_dialog_info);
        ad.setTitle(R.string.delfile);// 设置对话框标题
        ad.setMessage(R.string.delfile_content);// 设置对话框内容
        ad.setPositiveButton(R.string.determine, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                // 删除文件
                Map map = MoreAdapter.isSelected;
                deleteFiles(map);

            }
        });

        ad.setNegativeButton(R.string.cancel, null);

    }

    /***
     * 打开没有选择文件的对话框
     */
    private void noSelFilesDialog() {// 打开对话框
        noSelFiles = new AlertDialog.Builder(this);
        noSelFiles.setIcon(android.R.drawable.ic_dialog_info);
        noSelFiles.setTitle(R.string.selfile);// 设置对话框标题
        noSelFiles.setMessage(R.string.selfile_content);// 设置对话框内容
        noSelFiles.setPositiveButton(R.string.determine, null);
        noSelFiles.setNegativeButton(R.string.cancel, null);

    }

    /*
     * 打开没有选择文件的对话框
     */
    private void delFileOrDirFails() {// 打开对话框
        delFilesFails = new AlertDialog.Builder(this);
        delFilesFails.setIcon(android.R.drawable.ic_dialog_info);
        delFilesFails.setTitle(R.string.delfile_fail);// 设置对话框标题
        delFilesFails.setMessage(R.string.delfile_failContent);// 设置对话框内容
        delFilesFails.setPositiveButton(R.string.determine, null);
        delFilesFails.setNegativeButton(R.string.cancel, null);

    }

    /***
     * 刷新和查找的监听
     * 
     * @author sbp
     */
    public class FreshAndSearchOnClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bookcaseBack:
                    BookCaseActivity.this.finish();
                    Intent inte = new Intent(BookCaseActivity.this, MainActivity.class);
                    BookCaseActivity.this.startActivity(inte);
                    break;
                case R.id.search:
                    Intent intent = new Intent(BookCaseActivity.this, SearchActivity.class);
                    BookCaseActivity.this.startActivity(intent);
                    break;

            }

        }

    }

    public class BooksListOnTounchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return mVfDetector.onTouchEvent(event);

        }

    }

    // 进行滑动分页
    private GestureDetector mVfDetector = new GestureDetector(new OnGestureListener() {
        // 手指在屏幕上移动距离小于此值不会被认为是手势
        private static final int SWIPE_MIN_DISTANCE = 80;

        // 手指在屏幕上移动速度小于此值不会被认为手势
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        // 手势识别函数，到此函数被系统回调时说明系统认为发生了手势事件，
        // 我们可以做进一步判定。
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // 如果第1个坐标点大于第二个坐标点，说明是向左滑动
            // 滑动距离以及滑动速度是额外判断，可根据实际情况修改。
            if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                // 下
                Log.i("GestureDemo", "ViewFlipper left");
                if (index + 1 < getPage(totalBook)) {
                    rightView();
                }
                return true;
            } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) { // 上
                Log.i("GestureDemo", "ViewFlipper right");
                if (index > 0) {
                    leftView();
                }
                return true;
            }

            return false;

        }

        @Override
        public boolean onDown(MotionEvent e) {

            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            return false;
        }
    });

    boolean enable_ebook_click = false;

    public class BooksItemOnClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            Book book = booklist.get(index * VIEW_COUNT + arg2);
            String path = book.getPath();
            path = path + "/" + book.getName();
            File file = new File(path);
            if (file.isFile() && enable_ebook_click) {// 文件
                // book.setPath(path.substring(0, path.lastIndexOf("/")));
                enable_ebook_click = false;
                FileUtils.openBook(BookCaseActivity.this, book);
            } else if (file.isDirectory()) {// 如果是文件夹进入文件夹内
                // 判断横竖屏
                uplevelLL.setVisibility(View.VISIBLE);
                reLoadData(path);
            }
        }

    }

    /***
     * 点击删除图标监听
     * 
     * @author sbp
     */
    private class DeleteImageListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            Map map = MoreAdapter.isSelected;
            if (map == null) {
                noSelFiles.show();
            } else {
                boolean bool = isTrue(map);
                if (bool) {
                    ad.show();
                } else {
                    noSelFiles.show();
                }

            }

        }

    }

    /***
     * 是否包含true
     * 
     * @param map
     * @return
     */
    private boolean isTrue(Map<Integer, Boolean> map) {
        if (map != null) {
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                Integer key = (Integer) entry.getKey();
                boolean value = (Boolean) entry.getValue();
                if (value) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 删除选定的文件
     * 
     * @param map
     */
    public void deleteFiles(Map<Integer, Boolean> map) {
        if (map != null) {
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                Integer key = (Integer) entry.getKey();
                boolean value = (Boolean) entry.getValue();
                if (value) {
                    Book book = booklist.get(key);
                    Cursor cursor = odb.getOrInsertBook(book, resolver);
                    if (!cursor.moveToFirst()) {
                        Toast.makeText(this, R.string.fileerror, Toast.LENGTH_SHORT).show();
                        this.finish();// 数据库操作异常，返回。
                    }
                    book.setId(cursor.getLong(cursor.getColumnIndex(BaseColumns._ID)));//
                    // 根据bookid删除章节的信息
                    resolver.delete(DBSchema.CONTENT_URI_CHAPTER, DBSchema.COLUMN_CHAPTER_BOOKID
                            + "=? ", new String[] {
                        String.valueOf(book.getId())
                    });
                    // 根据bookid删除书签的信息
                    resolver.delete(DBSchema.CONTENT_URI_BOOKMARK, DBSchema.COLUMN_BOOKMARK_BOOKID
                            + "=? ", new String[] {
                        String.valueOf(book.getId())
                    });

                    // 删除该文件的封面如果有
                    Cursor c = resolver.query(DBSchema.CONTENT_URI_BOOK, new String[] {
                        DBSchema.COLUMN_BOOK_COVER_PATH
                    }, "_id=?", new String[] {
                        String.valueOf(book.getId())
                    }, null);
                    if (c.moveToFirst()) {// 有封页面地址
                        String path = c
                                .getString(c.getColumnIndex(DBSchema.COLUMN_BOOK_COVER_PATH));
                        if (path != null) {
                            File file = new File(path);
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                    }
                    c.close();

                    // 根据bookid删除文件的信息
                    resolver.delete(DBSchema.CONTENT_URI, "_id=? ", new String[] {
                        String.valueOf(book.getId())
                    });

                    // 删除该文件
                    File file = new File(book.getPath() + "/" + book.getName());
                    if (file.exists()) {// 判断文件是否存在
                        if (file.isFile()) {// 是文件
                            boolean bool = deleteFile(book.getPath() + "/" + book.getName());
                            if (bool) {
                                booklist.remove(key);
                            } else {
                                delFilesFails.show();
                            }

                        } else {
                            boolean bool = deleteDirectory(book.getPath() + "/" + book.getName());
                            if (bool) {
                                booklist.remove(key);
                            } else {
                                delFilesFails.show();
                            }
                        }

                    }
                }
            }
            reLoadData(path);
        }

    }

    /**
     * 删除单个文件
     * 
     * @param sPath 被删除文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }

    /**
     * 删除目录（文件夹）以及目录下的文件
     * 
     * @param sPath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public boolean deleteDirectory(String sPath) {
        // 如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        // 删除文件夹下的所有文件(包括子目录)
        File[] files = dirFile.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                // 删除子文件
                if (files[i].isFile()) {
                    flag = deleteFile(files[i].getAbsolutePath());
                    if (!flag)
                        break;
                } // 删除子目录
                else {
                    flag = deleteDirectory(files[i].getAbsolutePath());
                    if (!flag)
                        break;
                }
            }
        }
        if (!flag)
            return false;
        // 删除当前目录
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

    /***
     * 点击不同存储方式的监听
     * 
     * @author sbp
     */
    private class StroreListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            Editor ed = settings.edit();

            switch (v.getId()) {
                case R.id.sdsel:
                    sdsel.setBackgroundResource(R.drawable.sdcard_c);
                    phonesel.setBackgroundResource(R.drawable.local);
                    ed.putString("store", "sd");
                    ed.commit();
                    break;

                case R.id.phonesel:
                    sdsel.setBackgroundResource(R.drawable.sdcard);
                    phonesel.setBackgroundResource(R.drawable.local_c);
                    ed.putString("store", "phone");
                    ed.commit();
                    break;
            }
            reLoadData(null);
        }

    }

    /***
     * 重新加载数据
     */
    public void reLoadData(String newpath) {
        String showtypeStr = settings.getString("showtype", getResources()
                .getString(R.string.books));
        String arrayStr = settings.getString("array", getResources().getString(R.string.mrecent));
        showtypeStr = changPar(showtypeStr);
        arrayStr = changeArrayPar(arrayStr);
        String store = settings.getString("store", "sd");
        if (newpath == null) {
            if ("sd".equals(store)) {
                path = FileUtils.getSDPath().toString() + "/tflash";
            } else {
                path = FileUtils.getSDPath().toString();

            }
        } else {
            path = newpath;
        }
        booklist = selectType(showtypeStr, arrayStr);
        ma = new MoreAdapter(BookCaseActivity.this, booklist, new HashMap<Integer, Boolean>(),
                horverstat);
        index = 0;
        setTotalAndPage();
        checkButton();
        booksList.setAdapter(ma);
    }

    /***
     * 设置总是和页面
     * 
     * @param list
     */
    public void setTotalAndPage() {
        if (booklist == null) {
            allbooks.setText(getResources().getString(R.string.allbooks) + "(0)");
            totalBook = 0;
        } else {
            allbooks.setText(getResources().getString(R.string.allbooks) + "(" + booklist.size()
                    + ")");
            totalBook = booklist.size();
        }
        String totalPage = String.valueOf(getPage(totalBook));
        if (totalBook == 0) {
            totalT.setText("0");
            currentT.setText("0");
        } else {
            totalT.setText(totalPage);
            String currentPage = String.valueOf(index + 1);
            currentT.setText(currentPage);
        }

    }

    /***
     * 下面分页按钮的监听
     * 
     * @author sbp
     */
    private class PageButton implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.left:
                    leftView();
                    break;

                case R.id.right:
                    rightView();
                    break;
            }

        }

    }

    // 点击左边的Button，表示向前翻页，索引值要减1.
    public void leftView() {
        index--;
        // 刷新ListView里面的数值。
        ma.notifyDataSetChanged();

        // 检查Button是否可用。
        checkButton();
        application.setPageNo(index);
        // 显示当前页
        String currentPage = String.valueOf(index + 1);
        currentT.setText(currentPage);

    }

    // 点击右边的Button，表示向后翻页，索引值要加1.
    public void rightView() {
        index++;

        // 刷新ListView里面的数值。
        ma.notifyDataSetChanged();

        // 检查Button是否可用。
        checkButton();
        application.setPageNo(index);
        // 显示当前页
        String currentPage = String.valueOf(index + 1);
        currentT.setText(currentPage);
    }

    /**
     * 左按钮失效
     */
    private void btnLeftEnabled() {
        btnLeft.setImageResource(R.drawable.arrow_1_d);
        btnLeft.setEnabled(false);
    }

    /**
     * 右按钮失效
     */
    private void btnRightEnabled() {
        btnRight.setImageResource(R.drawable.arrow_2_d);
        btnRight.setEnabled(false);
    }

    /***
     * 左按钮启用
     */
    private void btnLeftEnablTrue() {
        btnLeft.setImageResource(R.drawable.arrow_1);
        btnLeft.setEnabled(true);
    }

    /***
     * 右按钮启用
     */
    private void btnRightEnablTrue() {
        btnRight.setImageResource(R.drawable.arrow_2);
        btnRight.setEnabled(true);
    }

    /***
     * 检查两个按钮可能用
     */
    public void checkButton() {
        if (booklist == null) {
            btnLeftEnabled();
            btnRightEnabled();
        } else if (booklist.size() <= VIEW_COUNT) {
            btnLeftEnabled();
            btnRightEnabled();
        } else {
            // 索引值小于等于0，表示不能向前翻页了，以经到了第一页了。
            // 将向前翻页的按钮设为不可用。
            if (index <= 0) {
                btnLeftEnabled();
                btnRightEnablTrue();
            }
            // 值的长度减去前几页的长度，剩下的就是这一页的长度，如果这一页的长度比View_Count小，表示这是最后的一页了，后面在没有了。
            // 将向后翻页的按钮设为不可用。
            else if (booklist.size() - index * VIEW_COUNT <= VIEW_COUNT) {
                btnLeftEnablTrue();
                btnRightEnabled();
            }

            // 否则将2个按钮都设为可用的。
            else {
                btnLeftEnablTrue();
                btnRightEnablTrue();

            }
        }

    }

    /**
     * 获取页数
     * 
     * @param total
     * @return
     */
    private int getPage(int total) {
        int page = 0;
        if (total % VIEW_COUNT == 0) {
            page = total / VIEW_COUNT;
        } else {
            page = total / VIEW_COUNT + 1;
        }
        return page;
    }

    /***
     * @author sbp 组装书柜的列表
     */
    public static class MoreAdapter extends BaseAdapter {
        List<Book> data;

        private int type;

        private LayoutInflater mInflater;

        private static Map<Integer, Boolean> isSelected;

        public MoreAdapter(Context context, List<Book> data, HashMap<Integer, Boolean> hashMap,
                int type) {
            this.data = data;
            this.mInflater = LayoutInflater.from(context);
            this.isSelected = hashMap;
            this.type = type;

        }

        // 设置每一页的长度，默认的是View_Count的值。
        @Override
        public int getCount() {

            // ori表示到目前为止的前几页的总共的个数。
            int ori = VIEW_COUNT * index;

            // 值的总个数-前几页的个数就是这一页要显示的个数，如果比默认的值小，说明这是最后一页，只需显示这么多就可以了
            if (data == null) {
                return 0;
            }
            if (data.size() - ori < VIEW_COUNT) {
                return data.size() - ori;
            }
            // 如果比默认的值还要大，说明一页显示不完，还要用换一页显示，这一页用默认的值显示满就可以了。
            else {
                return VIEW_COUNT;
            }
        }

        @Override
        public Object getItem(int position) {

            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) { // 装载布局文件
                convertView = mInflater.inflate(com.yt.reader.R.layout.bookcaselist, null);
            }
            // TextView要显示的是当前的位置+前几页已经显示的位置个数的对应的位置上的值。
            Book book = null;
            if (position + index * VIEW_COUNT >= data.size()) {
                book = data.get(data.size() - 1);// 最后一个
            } else {
                book = data.get(position + index * VIEW_COUNT);
            }
            ImageView booksInco = (ImageView) convertView.findViewById(R.id.booksIcon);
            TextView booksname = (TextView) convertView.findViewById(R.id.booksname);
            TextView authorOrTime = (TextView) convertView.findViewById(R.id.authorOrTime);

            TextView booksize = (TextView) convertView.findViewById(R.id.booksize);

            String fileType = null;
            if (book.getName().lastIndexOf(".") >= 0) {
                fileType = null != book.getFileType() ? book.getFileType().toUpperCase() : book
                        .getName().substring(book.getName().lastIndexOf(".") + 1).toUpperCase();
            }
            
            boolean isfolder=false;

            if (fileType == null) {
                booksInco.setImageResource(R.drawable.icon_folder);
                isfolder=true;
            } else {
                if (fileType.contains("EPUB")) {
                    booksInco.setImageResource(R.drawable.icon_epub);
                } else if (fileType.contains("FB2")) {
                    booksInco.setImageResource(R.drawable.icon_fb2);
                } else if (fileType.contains("HTML") || fileType.contains("HTM")) {
                    booksInco.setImageResource(R.drawable.icon_html);
                } else if (fileType.contains("PDB")) {
                    booksInco.setImageResource(R.drawable.icon_pdb);
                } else if (fileType.contains("PDF")) {
                    booksInco.setImageResource(R.drawable.icon_pdf);
                } else if (fileType.contains("RTF")) {
                    booksInco.setImageResource(R.drawable.icon_rtf);
                } else if (fileType.contains("TXT")) {
                    booksInco.setImageResource(R.drawable.icon_txt);
                } else if (fileType.contains("DOC")) {
                    booksInco.setImageResource(R.drawable.icon_word);
                } else if (fileType.contains("MOBI")) {
                    booksInco.setImageResource(R.drawable.icon_mobi);
                } else {
                    booksInco.setImageResource(R.drawable.icon_folder);
                    isfolder=true;
                }
            }

            if (type == 1) {
                booksname.setText(StringUtils.subString(book.getName(), 20));
            } else {
                booksname.setText(StringUtils.subString(book.getName(), 7));
            }
            
            if(isfolder){
                booksname.setPadding(5, 10, 0, 0);
            }
            

            booksize.setText(book.getSize());

            if (book.getAuthor() == null || "".equals(book.getAuthor())) {
                Date date = DateUtils.getSystemDate(new Date(book.getAddedTime()));
                String moddate = DateUtils.dateToString(date, "yyyy-MM-dd");
                authorOrTime.setText(moddate);
            } else {
                authorOrTime.setText(book.getAuthor());
            }

            CheckBox cxb = (CheckBox) convertView.findViewById(R.id.check);
            // 获取该条信息整个记录集的位置
            boolean bool = false;
            if (isSelected.containsKey(index * VIEW_COUNT + position)) {
                bool = isSelected.get(index * VIEW_COUNT + position);
            }
            cxb.setOnClickListener(new CheckOnClickListener(cxb, position));
            if (bool) {
                cxb.setChecked(true);
            } else {
                cxb.setChecked(false);
            }

            return convertView;
        }

        private class CheckOnClickListener implements OnClickListener {

            private CheckBox checkBox;

            private int position;

            public CheckOnClickListener(CheckBox checkBox, int position) {
                this.checkBox = checkBox;
                this.position = position;
            }

            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()) {
                    checkBox.setChecked(true);
                    isSelected.put(index * VIEW_COUNT + position, true);
                } else {
                    checkBox.setChecked(false);
                    isSelected.put(index * VIEW_COUNT + position, false);
                }

            }

        }

    }

    /***
     * 根据选择方式获取list
     * 
     * @return
     */
    public List<Book> selectType(String showtypeStr, String arrayStr) {
        List<Book> listBook = null;
        // book方式most recent排序方式
        if (showtypeStr.equalsIgnoreCase(getResources().getString(R.string.books))
                && arrayStr.equalsIgnoreCase(getResources().getString(R.string.mrecent))) {
            FileUtils fileU = new FileUtils(new ArrayList<Book>());
            listBook = fileU.getFiles(path);
            BookTimeCompare comparator = new BookTimeCompare();
            Collections.sort(listBook, comparator);

        } else if (showtypeStr.equalsIgnoreCase(getResources().getString(R.string.books))
                && arrayStr.equalsIgnoreCase(getResources().getString(R.string.title))) {
            FileUtils fileU = new FileUtils(new ArrayList<Book>());
            listBook = fileU.getFiles(path);
            BookNameCompare comparator = new BookNameCompare();
            Collections.sort(listBook, comparator);

        } else if (showtypeStr.equalsIgnoreCase(getResources().getString(R.string.books))
                && arrayStr.equalsIgnoreCase(getResources().getString(R.string.filetype))) {
            FileUtils fileU = new FileUtils(new ArrayList<Book>());
            listBook = fileU.getFiles(path);
            BookFileTypeCompare comparator = new BookFileTypeCompare();
            Collections.sort(listBook, comparator);

        } else if (showtypeStr.equalsIgnoreCase(getResources().getString(R.string.files))
                && arrayStr.equalsIgnoreCase(getResources().getString(R.string.mrecent))) {
            Map map = FileUtils.getFilesAndFolder(path);
            listBook = getBookList(map, 1);

        } else if (showtypeStr.equalsIgnoreCase(getResources().getString(R.string.files))
                && arrayStr.equalsIgnoreCase(getResources().getString(R.string.title))) {
            Map map = FileUtils.getFilesAndFolder(path);
            listBook = getBookList(map, 2);

        } else if (showtypeStr.equalsIgnoreCase(getResources().getString(R.string.files))
                && arrayStr.equalsIgnoreCase(getResources().getString(R.string.filetype))) {
            Map map = FileUtils.getFilesAndFolder(path);
            listBook = getBookList(map, 3);

        }
        return listBook;

    }

    /***
     * 将map中存放的文件list和文件夹list合并一个list,前面放文件夹后面放文件
     * 
     * @param map
     * @type 1:时间比较器，2：文件名比较器，3：文件类型比较器
     * @return
     */
    public List<Book> getBookList(Map map, int type) {
        List<Book> fileList = null;
        if (map != null && map.containsKey("file")) {
            fileList = (List<Book>) map.get("file");
        }
        List<Book> folderList = null;
        if (map != null && map.containsKey("folder")) {
            folderList = (List<Book>) map.get("folder");
        }

        if (type == 1) {
            BookTimeCompare comparator = new BookTimeCompare();// 时间比较器
            if (fileList != null) {
                Collections.sort(fileList, comparator);
            }
            if (folderList != null) {
                Collections.sort(folderList, comparator);
            }
        } else if (type == 2) {
            BookNameCompare comparator = new BookNameCompare();// 名称比较器
            if (fileList != null) {
                Collections.sort(fileList, comparator);
            }
            if (folderList != null) {
                Collections.sort(folderList, comparator);
            }
        } else if (type == 3) {
            BookFileTypeCompare comparator = new BookFileTypeCompare();// 文件类型比较器
            if (fileList != null) {
                Collections.sort(fileList, comparator);
            }
        }
        if (fileList != null) {
            for (Book book : fileList) {
                folderList.add(book);
            }
        }
        return folderList;
    }

    private class ShowTypeListenter implements OnClickListener {

        int type;

        /***
         * 1:显示类型的监听 2：排列方式的监听
         * 
         * @param val
         */
        public ShowTypeListenter(int val) {
            this.type = val;
        }

        @Override
        public void onClick(View v) {
            int[] location = new int[2];
            v.getLocationInWindow(location); // 获取在当前窗口内的绝对坐标
            int left = location[0];
            int width = v.getWidth();
            int height = v.getHeight() + location[1] + 2;
            if (type == 1) {
                if (isShowPop) {
                    closePopupWindow();
                    isShowPop = false;
                } else {
                    showPopupWindow(width, left, height);
                    isShowPop = true;
                }
            } else if (type == 2) {
                if (isArraypop) {
                    closeArray();
                    isArraypop = false;
                } else {
                    arrayPopupWindow(width, left, height);
                    isArraypop = true;
                }
            }
        }

    }

    /***
     * 显示排列方式的下拉列表
     * 
     * @param width
     * @param left
     * @param height
     */
    private void arrayPopupWindow(int width, int left, int height) {
        if (arraypop == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this
                    .getSystemService(LAYOUT_INFLATER_SERVICE);
            arrayPopView = layoutInflater.inflate(R.layout.arraypop, null, false);
            arraypop = new PopupWindow(arrayPopView, width, LayoutParams.WRAP_CONTENT);// 创建PopupWindow实例
        }
        TextView mrecents = (TextView) arrayPopView.findViewById(R.id.mrecent);
        TextView titleV = (TextView) arrayPopView.findViewById(R.id.title);
        TextView filetype = (TextView) arrayPopView.findViewById(R.id.filetype);
        // TextView authorV = (TextView) arrayPopView.findViewById(R.id.author);
        String arraytypeStr = settings.getString("array", getResources()
                .getString(R.string.mrecent));
        arraytypeStr = changeArrayPar(arraytypeStr);
        if (arraytypeStr.equalsIgnoreCase((getResources().getString(R.string.mrecent)))) {
            mrecents.setBackgroundResource(R.drawable.line_h);
            mrecents.setTextColor(getResources().getColor(R.color.white));
            titleV.setBackgroundColor(getResources().getColor(R.color.white));
            titleV.setTextColor(getResources().getColor(R.color.black));
            filetype.setBackgroundColor(getResources().getColor(R.color.white));
            filetype.setTextColor(getResources().getColor(R.color.black));
        } else if (arraytypeStr.equalsIgnoreCase(getResources().getString(R.string.title))) {
            mrecents.setBackgroundColor(getResources().getColor(R.color.white));
            mrecents.setTextColor(getResources().getColor(R.color.black));
            titleV.setBackgroundResource(R.drawable.line_h);
            titleV.setTextColor(getResources().getColor(R.color.white));
            filetype.setBackgroundColor(getResources().getColor(R.color.white));
            filetype.setTextColor(getResources().getColor(R.color.black));
        } else if (arraytypeStr.equalsIgnoreCase(getResources().getString(R.string.filetype))) {
            mrecents.setBackgroundColor(getResources().getColor(R.color.white));
            mrecents.setTextColor(getResources().getColor(R.color.black));
            titleV.setBackgroundColor(getResources().getColor(R.color.white));
            titleV.setTextColor(getResources().getColor(R.color.black));
            filetype.setBackgroundResource(R.drawable.line_h);
            filetype.setTextColor(getResources().getColor(R.color.white));
        } else {
            // 防止 语言改变的时候，存储的是中文，获取的是英文导致出现中英文现象
            Editor ed = settings.edit();
            ed.putString("array", getResources().getString(R.string.mrecent));
            ed.commit();
            showPopupWindow(width, left, height);
        }
        arraypop.showAtLocation(this.findViewById(R.id.ss), Gravity.NO_GRAVITY, left, height);
        arraypop.update();

        mrecents.setOnClickListener(new DropDownListener("recents", 2));
        titleV.setOnClickListener(new DropDownListener("title", 2));
        filetype.setOnClickListener(new DropDownListener("filetype", 2));
        // authorV.setOnClickListener(new
        // DropDownListener(authorV.getText().toString(), 2));
    }

    /***
     * 显示类型下的pop
     * 
     * @param width
     * @param left
     * @param height
     */
    private void showPopupWindow(int width, int left, int height) {

        if (showpop == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this
                    .getSystemService(LAYOUT_INFLATER_SERVICE);
            popupWindow_view = layoutInflater.inflate(R.layout.showpop, null, false);
            showpop = new PopupWindow(popupWindow_view, width, LayoutParams.WRAP_CONTENT);// 创建PopupWindow实例
        }
        TextView popbooks = (TextView) popupWindow_view.findViewById(R.id.popbooks);
        TextView popFiles = (TextView) popupWindow_view.findViewById(R.id.popfiles);
        String showtypeStr = settings.getString("showtype", getResources()
                .getString(R.string.books));
        showtypeStr = changPar(showtypeStr);
        if (showtypeStr.equalsIgnoreCase((getResources().getString(R.string.books)))) {
            popbooks.setBackgroundResource(R.drawable.line_h);
            popbooks.setTextColor(getResources().getColor(R.color.white));
            popFiles.setBackgroundColor(getResources().getColor(R.color.white));
            popFiles.setTextColor(getResources().getColor(R.color.black));
        } else if (showtypeStr.equalsIgnoreCase(getResources().getString(R.string.files))) {
            popFiles.setBackgroundResource(R.drawable.line_h);
            popFiles.setTextColor(getResources().getColor(R.color.white));
            popbooks.setBackgroundColor(getResources().getColor(R.color.white));
            popbooks.setTextColor(getResources().getColor(R.color.black));
        } else {
            // 防止 语言改变的时候，存储的是中文，获取的是英文导致出现中英文现象
            Editor ed = settings.edit();
            ed.putString("showtype", getResources().getString(R.string.books));
            ed.commit();
            showPopupWindow(width, left, height);
        }
        showpop.showAtLocation(this.findViewById(R.id.ss), Gravity.NO_GRAVITY, left, height);
        showpop.update();

        popbooks.setOnClickListener(new DropDownListener("books", 1));
        popFiles.setOnClickListener(new DropDownListener("files", 1));

    }

    public String changPar(String showtypeStr) {
        if ("books".equals(showtypeStr)) {
            showtypeStr = getResources().getString(R.string.books);
        } else if ("files".equals(showtypeStr)) {
            showtypeStr = getResources().getString(R.string.files);
        }

        return showtypeStr;
    }

    public String changeArrayPar(String arraytypeStr) {
        if ("recents".equals(arraytypeStr)) {
            arraytypeStr = getResources().getString(R.string.mrecent);
        } else if ("title".equals(arraytypeStr)) {
            arraytypeStr = getResources().getString(R.string.title);
        } else if ("filetype".equals(arraytypeStr)) {
            arraytypeStr = getResources().getString(R.string.filetype);
        }

        return arraytypeStr;
    }

    /***
     * @author sbp 下拉列表中的点击
     */
    private class DropDownListener implements OnClickListener {
        private String val;

        private int type;// 1表示显示方式 2表示排列方式

        public DropDownListener(String va, int type) {
            this.val = va;
            this.type = type;
        }

        @Override
        public void onClick(View v) {
            // 显示方式列表
            Editor ed = settings.edit();
            if (type == 1) {
                ed.putString("showtype", val);
                val = changPar(val);
                showtype.setText(val);
                closePopupWindow();
                isShowPop = false;
            }
            // 排列方式
            if (type == 2) {
                ed.putString("array", val);
                val = changeArrayPar(val);
                arrayType.setText(val);
                closeArray();
                isArraypop = false;
            }
            ed.commit();
            reLoadData(path);

        }

    }

    /**
     * clos popup window if popup window is not null
     */
    private void closePopupWindow() {
        if (showpop != null) {
            showpop.dismiss();
        }
    }

    private void closeArray() {
        if (arraypop != null) {
            arraypop.dismiss();
        }
    }

}
