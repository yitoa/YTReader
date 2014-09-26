
package com.yt.reader.utils;

import java.util.Comparator;

import com.yt.reader.model.Book;

public class BookTimeCompare implements Comparator {

    /***
     * 文件时间比较
     */
    @Override
    public int compare(Object arg0, Object arg1) {
        // TODO Auto-generated method stub
        Book book0 = (Book) arg0;
        Book book1 = (Book) arg1;
        return String.valueOf(book0.getAddedTime()).compareTo(String.valueOf(book1.getAddedTime()));
    }

}
