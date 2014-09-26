
package com.yt.reader.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yt.reader.R;
import com.yt.reader.model.Book;
import com.yt.reader.utils.DateUtils;
import com.yt.reader.utils.StringUtils;

public class BookAdapter extends BaseAdapter {

    private List<Book> lists;

    private LayoutInflater mInflater;

    private int type;

    /***
     * @param type 1:表示首页只显示三个，2：表示更多记录
     * @param list
     * @param context
     */

    public BookAdapter(int type, List<Book> list, Context context) {
        this.type = type;
        // 前四个显示
        if (type == 2) {// 横屏
            if (list.size() > 4) {
                lists = new ArrayList<Book>();
                lists.add(list.get(0));
                lists.add(list.get(1));
                lists.add(list.get(2));
                lists.add(list.get(3));
            } else {
                this.lists = list;
            }
        } else if (type == 1) {// 竖屏
            if (list.size() > 3) {
                lists = new ArrayList<Book>();
                lists.add(list.get(0));
                lists.add(list.get(1));
                lists.add(list.get(2));
            } else {
                this.lists = list;
            }
        }
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return lists.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return lists.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) { // 装载布局文件
            convertView = mInflater.inflate(com.yt.reader.R.layout.booklist, null);
        }
        // 获取单个book
        Book book = lists.get(position);
        TextView bookname = (TextView) convertView.findViewById(R.id.bookname);
        if (type == 1) {// 横屏
            bookname.setText(StringUtils.subString(book.getName(), 7));
        } else if (type == 2) {
            bookname.setText(StringUtils.subString(book.getName(), 3));
        }

        TextView author = (TextView) convertView.findViewById(R.id.author);
        if (book.getAuthor() != null&&!"".equals(book.getAuthor())) {
            if (type == 1) {// 横屏
                author.setText(StringUtils.subString(book.getAuthor(), 4));
            } else if (type == 2) {
                author.setText(StringUtils.subString(book.getAuthor(), 3));
            }

        } else {
            Date date = DateUtils.getGreenwichDate(new Date(book.getLastReadingTime()));
            String moddate = DateUtils.dateToString(date, "yyyy-MM-dd");
            author.setText(moddate);
        }
        return convertView;
    }

}
