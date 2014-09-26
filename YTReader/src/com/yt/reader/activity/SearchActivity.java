
package com.yt.reader.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.yt.reader.R;
import com.yt.reader.base.YTReaderActivity;
import com.yt.reader.model.Book;
import com.yt.reader.utils.DateUtils;
import com.yt.reader.utils.FileUtils;
import com.yt.reader.utils.StringUtils;

public class SearchActivity extends YTReaderActivity {

    private ListView searchList;

    // 用于显示每列5个Item项。
    private static int VIEW_COUNT = 8;

    // 用于显示页号的索引
    private static int index = 0;

    private List<Book> listsBook; // 存放所有的book

    private List<Book> booklist;// 放置查询结果

    private SearchBookAdapter ma;

    private TextView currentT;// 当前页面

    private TextView totalT;// 总页面

    private int totalBook;

    private ImageButton btnLeft;

    private ImageButton btnRight;

    private ImageView searchdel;

    private static int horverstat;// 横竖屏状态，默认竖屏

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        // 判断横竖屏
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {// 横屏
            horverstat = 1;// 横屏为1
            VIEW_COUNT = 8;
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {// 竖屏
            horverstat = 2;
            VIEW_COUNT = 13;
        }
        index = 0;
        // 输入框
        EditText inputSearch = (EditText) findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextChangContent());
        searchList = (ListView) findViewById(R.id.searchList);

        FileUtils fileUtils = new FileUtils(new ArrayList<Book>());
        // 查找所有满足条件的书籍信息
        listsBook = fileUtils.getFiles(FileUtils.getSDPath().toString());

        currentT = (TextView) findViewById(R.id.currentPageSearch);
        totalT = (TextView) findViewById(R.id.totalPageSearch);

        btnLeft = (ImageButton) findViewById(R.id.searchleft);
        btnRight = (ImageButton) findViewById(R.id.searchright);
        // 初始化的时候显示所有的
        booklist = listsBook;
        setTotalAndPage();
        checkButton();
        btnLeft.setOnClickListener(new PageButton());
        btnRight.setOnClickListener(new PageButton());
        ma = new SearchBookAdapter(this, booklist);
        searchList.setAdapter(ma);
        searchList.setOnTouchListener(new MoreBooksListOnTounchListener());
        searchList.setOnItemClickListener(new BooksItemOnClickListener());
        searchdel = (ImageView) findViewById(R.id.searchdel);
        searchdel.setOnClickListener(new SearchDelOnClickListener(inputSearch));

        ImageButton searchBack = (ImageButton) findViewById(R.id.searchBack);
        searchBack.setOnClickListener(new SearchBackOnClickListener());

    }

    private class SearchBackOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            SearchActivity.this.finish();
            Intent intent = new Intent(SearchActivity.this, BookCaseActivity.class);
            SearchActivity.this.startActivity(intent);
        }

    }

    public class BooksItemOnClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            Book book = booklist.get(index * VIEW_COUNT + arg2);
            FileUtils.openBook(SearchActivity.this, book);
        }

    }

    public class SearchDelOnClickListener implements OnClickListener {
        private EditText inputSearch;

        public SearchDelOnClickListener(EditText inputSearch) {
            this.inputSearch = inputSearch;
        }

        @Override
        public void onClick(View v) {
            inputSearch.setText(null);
            searchdel.setVisibility(View.INVISIBLE);
        }

    }

    public class TextChangContent implements TextWatcher {

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            searchdel.setVisibility(View.VISIBLE);
            // 存放结果查询后的结果
            booklist = new ArrayList<Book>();
            String bookName;// 书名
            for (Book book : listsBook) {
                bookName = book.getName();
                if (bookName != null) {
                    if (bookName.contains(s.toString().trim().toLowerCase())) {
                        booklist.add(book);
                    }
                }
            }
            index = 0;
            setTotalAndPage();
            checkButton();
            ma = new SearchBookAdapter(SearchActivity.this, booklist);
            searchList.setAdapter(ma);
            searchList.setOnTouchListener(new MoreBooksListOnTounchListener());

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

    /***
     * 下面分页按钮的监听
     * 
     * @author sbp
     */
    private class PageButton implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.searchleft:
                    leftView();
                    break;

                case R.id.searchright:
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
     * @author sbp 查询的结果
     */
    public static class SearchBookAdapter extends BaseAdapter {
        List<Book> data;

        private LayoutInflater mInflater;

        public SearchBookAdapter(Context context, List<Book> data) {
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
                convertView = mInflater.inflate(com.yt.reader.R.layout.searchitem, null);
            }
            // TextView要显示的是当前的位置+前几页已经显示的位置个数的对应的位置上的值。
            Book book = data.get(position + index * VIEW_COUNT);
            ImageView booksInco = (ImageView) convertView.findViewById(R.id.morebooksIcon);
            TextView booksname = (TextView) convertView.findViewById(R.id.morebooksname);
            TextView authorOrTime = (TextView) convertView.findViewById(R.id.moreauthorOrTime);
            String name = book.getName();
            String suffix = name.substring(name.indexOf("."));
            if (horverstat == 0) {// 竖屏
                name = StringUtils.subString(name, 7);
            } else {// 横屏
                name = StringUtils.subString(name, 12);
            }
            if (suffix == null) {
                booksInco.setImageResource(R.drawable.icon_folder);
            } else {
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
            booksname.setText(name);
            if (book.getAuthor() == null) {
                Date date = DateUtils.getGreenwichDate(new Date(book.getAddedTime()));
                String moddate = DateUtils.dateToString(date, "yyyy-MM-dd");
                authorOrTime.setText(moddate);
            } else {
                authorOrTime.setText(book.getAuthor());
            }

            return convertView;
        }

    }

}
