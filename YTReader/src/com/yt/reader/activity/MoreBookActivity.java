
package com.yt.reader.activity;

import java.io.File;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.yt.reader.MainActivity;
import com.yt.reader.R;
import com.yt.reader.base.YTReaderActivity;
import com.yt.reader.database.CurdDataBase;
import com.yt.reader.database.DBSchema;
import com.yt.reader.database.OpenDataBase;
import com.yt.reader.model.Book;
import com.yt.reader.utils.DateUtils;
import com.yt.reader.utils.FileUtils;
import com.yt.reader.utils.StringUtils;

public class MoreBookActivity extends YTReaderActivity {

    // 用于显示每列5个Item项。
    private static int VIEW_COUNT = 8;

    // 用于显示页号的索引
    private static int index = 0;

    private MoreBookAdapter ma;

    private TextView currentT;// 当前页面

    private TextView totalT;// 总页面

    private int totalBook;

    private ImageButton btnLeft;

    private ImageButton btnRight;

    private List<Book> booklist;// 存放查询结果的

    private ListView booksList;// 存放最近阅读书的结果

    private int landPortState;// 横竖屏的状态 1:横屏，2:竖屏

    private AlertDialog.Builder clearAllDialog;// 清除完成

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.morebooklist);
        // 判断横竖屏
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {// 横屏
            landPortState = 1;
            VIEW_COUNT = 6;
            index = 0;
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {// 竖屏
            landPortState = 2;
            VIEW_COUNT = 9;
            index = 0;
        }
        OpenDataBase opd = new OpenDataBase();
        ContentResolver resolver = this.getContentResolver();
        Cursor cursor = resolver.query(DBSchema.CONTENT_URI_BOOK, new String[] {
                DBSchema.COLUMN_BOOK_NAME, "_id", DBSchema.COLUMN_BOOK_AUTHOR,
                DBSchema.COLUMN_BOOK_LAST_READING_TIME, DBSchema.COLUMN_BOOK_SIZE,DBSchema.COLUMN_BOOK_ADDED_TIME,DBSchema.COLUMN_BOOK_PATH
        }, DBSchema.COLUMN_BOOK_LAST_READING_TIME + " is not null", null, "lastReadingTime desc");
        booklist = opd.geRecentlyBook(cursor);
        booksList = (ListView) findViewById(R.id.morebookslist);

        currentT = (TextView) findViewById(R.id.currentPagebook);
        totalT = (TextView) findViewById(R.id.totalPagebook);

        btnLeft = (ImageButton) findViewById(R.id.moreleft);
        btnRight = (ImageButton) findViewById(R.id.moreright);
        setTotalAndPage();
        checkButton();
        btnLeft.setOnClickListener(new PageButton());
        btnRight.setOnClickListener(new PageButton());
        ma = new MoreBookAdapter(this, booklist, landPortState);
        booksList.setAdapter(ma);
        booksList.setOnItemClickListener(new BooksItemOnClickListener());
        booksList.setOnTouchListener(new MoreBooksListOnTounchListener());

        // 返回按钮
        ImageButton back = (ImageButton) findViewById(R.id.morebookBack);
        back.setOnClickListener(new BackOnClickListener());

        Button clearAll = (Button) findViewById(R.id.clearAll);// 清除所有的阅读记录

        if (booklist == null || booklist.size() == 0) {
            clearAll.setVisibility(View.INVISIBLE);
        } else {
            clearAll.setVisibility(View.VISIBLE);
        }
        clearAll.setOnClickListener(new ClearAllOnClickListener());
        ClearAllDialog();

    }

    private class ClearAllOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            clearAllDialog.show();
        }

    }

    /***
     * 打开没有选择文件的对话框
     */
    private void ClearAllDialog() {// 打开对话框
        clearAllDialog = new AlertDialog.Builder(this);
        clearAllDialog.setIcon(android.R.drawable.ic_dialog_info);
        clearAllDialog.setTitle(R.string.removeFile);// 设置对话框标题
        clearAllDialog.setMessage(R.string.removeFileSucc);// 设置对话框内容
        clearAllDialog.setPositiveButton(R.string.determine, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                CurdDataBase cdb = new CurdDataBase(MoreBookActivity.this);
                for (Book book : booklist) {
                    cdb.updateBook(book);
                }
                Intent intent=new Intent(MoreBookActivity.this,MoreBookActivity.class);
                MoreBookActivity.this.startActivity(intent);
            }
        });
        clearAllDialog.setNegativeButton(R.string.cancel, null);

    }

    private class BackOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            // 返回到主界面
            MoreBookActivity.this.finish();
            Intent intent = new Intent(MoreBookActivity.this, MainActivity.class);
            MoreBookActivity.this.startActivity(intent);
        }

    }

    public class MoreBooksListOnTounchListener implements OnTouchListener {

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

    /***
     * 设置总是和页面
     * 
     * @param list
     */
    public void setTotalAndPage() {
        if (booklist == null) {
            totalBook = 0;
        } else {
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

    public class BooksItemOnClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            Book book = booklist.get(index * VIEW_COUNT + arg2);
            String path = book.getPath();
            path = path + "/" + book.getName();
            File file = new File(path);
            if (file.isFile()) {// 文件
                // book.setPath(path.substring(0, path.lastIndexOf("/")));
                FileUtils.openBook(MoreBookActivity.this, book);
            }
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
                case R.id.moreleft:
                    leftView();
                    break;

                case R.id.moreright:
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
    public static class MoreBookAdapter extends BaseAdapter {
        List<Book> data;

        private LayoutInflater mInflater;

        private int type;

        public MoreBookAdapter(Context context, List<Book> data, int type) {
            this.type = type;
            this.data = data;
            this.mInflater = LayoutInflater.from(context);

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
                convertView = mInflater.inflate(com.yt.reader.R.layout.morebooklistitem, null);
            }
            // TextView要显示的是当前的位置+前几页已经显示的位置个数的对应的位置上的值。
            if (data == null) {
                return null;
            }
            Book book = data.get(position + index * VIEW_COUNT);
            ImageView booksInco = (ImageView) convertView.findViewById(R.id.morebooksIcon);
            TextView booksname = (TextView) convertView.findViewById(R.id.morebooksname);
            TextView authorOrTime = (TextView) convertView.findViewById(R.id.moreauthorOrTime);
            
            TextView booksize=(TextView) convertView.findViewById(R.id.booksize);
            String name = book.getName();
            String suffix = name.substring(name.lastIndexOf("."));
            if (suffix == null) {
                booksInco.setImageResource(R.drawable.icon_folder);
            } else {
                suffix = suffix.toLowerCase();// 转换成小写的
                if (suffix.equals(".epub")) {
                    booksInco.setImageResource(R.drawable.icon_epub);
                } else if (suffix.equals(".fb2")) {
                    booksInco.setImageResource(R.drawable.icon_fb2);
                } else if (suffix.equals(".html") || suffix.equals(".htm")) {
                    booksInco.setImageResource(R.drawable.icon_html);
                } else if (suffix.equals(".pdb")) {
                    booksInco.setImageResource(R.drawable.icon_pdb);
                } else if (suffix.equals(".pdf")) {
                    booksInco.setImageResource(R.drawable.icon_pdf);
                } else if (suffix.equals(".rtf")) {
                    booksInco.setImageResource(R.drawable.icon_rtf);
                } else if (suffix.equals(".txt")) {
                    booksInco.setImageResource(R.drawable.icon_txt);
                } else if (suffix.equals(".doc")) {
                    booksInco.setImageResource(R.drawable.icon_word);
                } else if (suffix.equals(".mobi")) {
                    booksInco.setImageResource(R.drawable.icon_mobi);
                } else {
                    booksInco.setImageResource(R.drawable.icon_folder);
                }
            }
            if (type == 1) {// 横屏
                booksname.setText(StringUtils.subString(name, 25));
            } else if (type == 2) {
                booksname.setText(StringUtils.subString(name, 8));
            }
            booksize.setText(book.getSize());
            if (book.getAuthor() != null&&!"".equals(book.getAuthor())) {
                authorOrTime.setText(StringUtils.subString(book.getAuthor(), 9));

            } else {
                Date date = DateUtils.getGreenwichDate(new Date(book.getLastReadingTime()));
                String moddate = DateUtils.dateToString(date, "yyyy-MM-dd");
                authorOrTime.setText(moddate);
            }
           

            return convertView;
        }

    }

}
