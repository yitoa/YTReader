
package com.yt.reader.utils;

import java.util.Comparator;

import com.yt.reader.model.Book;

public class BookNameCompare implements Comparator {

    /***
     * 文件名比较
     */
    @Override
    public int compare(Object arg0, Object arg1) {
        // TODO Auto-generated method stub
        Book book0 = (Book) arg0;
        Book book1 = (Book) arg1;
        return book0.getName().compareTo(book1.getName());
    }

}
